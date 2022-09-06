package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Maybe.to;
import static ca.applin.jib.utils.Utils.__FULL_METHOD_NAME__;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.lexer.LexerToken.TokenType.BACKSLASH;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_SQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.COMMA;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOT;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOUBLE_COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOUBLE_QUOTE;
import static ca.applin.jim.lexer.LexerToken.TokenType.OPEN_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.OPEN_SQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.SEMICOLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.SYM;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.expr.Ast.Atom;
import ca.applin.jim.expr.Expr;
import ca.applin.jim.expr.Expr.ArrayLitteral;
import ca.applin.jim.expr.Expr.Binop;
import ca.applin.jim.expr.Expr.DeRef;
import ca.applin.jim.expr.Expr.FloatLitteral;
import ca.applin.jim.expr.Expr.FunctionCall;
import ca.applin.jim.expr.Expr.IntegerLitteral;
import ca.applin.jim.expr.Expr.Litteral;
import ca.applin.jim.expr.Expr.PExpr;
import ca.applin.jim.expr.Expr.ReturnExpr;
import ca.applin.jim.expr.Expr.StringLitteral;
import ca.applin.jim.expr.Expr.Unop;
import ca.applin.jim.expr.Operator;
import ca.applin.jim.expr.Type;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExprParser implements Parser<Expr> {
    private Lexer<LexerToken> lexer;

    public ExprParser(Lexer<LexerToken> lexer) {
        this.lexer = lexer;
    }

    @Override
    public Maybe<Expr> parse() {
        Maybe<Expr> mExrp = parseBinop(nothing());
        if (mExrp instanceof Just<Expr> je && je.elem() instanceof PExpr expr) {
            return just (expr.unpack());
        }
        return mExrp;
    }

    public Maybe<Expr> parseWithPeek() {
        return parseBinop(nothing());
    }

    public Maybe<Expr> parseBinop(Maybe<Expr> mLeft) {
        if (mLeft.isNothing()) {
            return parseBinop();
        }

        Maybe<LexerToken> maybeOp = lexer.maybeNext();
        Expr left = ((Just<Expr>) mLeft).elem();
        if (maybeOp.test(tok -> tok.type() == DOT)) {
            Maybe<LexerToken> mRight = lexer.maybeNext();
            if (mRight.isNothing()) {
                return todo("report malformed dot deref:" + left);
            }
            LexerToken right = ((Just<LexerToken>)mRight).elem();
            Expr newLeft = new DeRef(left, new Atom(right.str()));
            return parseBinop(just(newLeft));
        }
        if (maybeOp.test(this::isBinopExpr)) {
            return parseBinopRhs(left);
        }
        return mLeft;
    }

    public Maybe<Expr> parseBinop() {
        Maybe<Expr> mLeft = parseExprPrimary();
        if (! (mLeft instanceof Just<Expr> jLeft)) {
            todo("report malformed Expr");
            return nothing();
        }
        Maybe<LexerToken> maybeOp = lexer.peek();
        if (maybeOp.test(tok -> tok.type() == SEMICOLON)) {
            lexer.next(); // eat SEMI
            return mLeft;
        }
        if (maybeOp.test(tok -> tok.type() == DOT)) {
            lexer.next();// eat the dot
            LexerToken ref = lexer.next();
            if (ref.type() != SYM) {
                return todo("report malformed dot dereference. current: " + ref);
            }
            if (lexer.peek().test(tok -> tok.type() == OPEN_PAREN)) {
                return todo("method call");
            }
            return parseBinop(just (new DeRef(jLeft.elem(), new Atom(ref.str()))));
        }

        if (maybeOp.test(this::isBinopExpr)) {
            lexer.next();
            return parseBinopRhs(jLeft.elem());
        }
        return mLeft;
    }

    private Maybe<Expr> parseBinopRhs(Expr left) {
        LexerToken op = lexer.current();
        Maybe<Expr> mNext = parseBinop();
        if (!(mNext instanceof Just<Expr> right)) {
            todo("report malformed Expr");
            return nothing();
        }
        // todo fix precedence
        return just(new Binop(left, right.elem(),
                Operator.fromToken(op)));
    }

    private boolean isBinopExpr(LexerToken tok) {
        return switch (tok.type()) {
            case PLUS, MINUS, TIMES, F_SLASH, PIPE, AMPERSAND, PERCENT,
                    DOUBLE_PIPE, DOUBLE_AMP, DOUBLE_GT, DOUBLE_LT,
                    DOUBLE_EQ, NEQ -> true;
            default -> false;
        };
    }

    // parse stuf until it reaches an operator or the end
    public Maybe<Expr> parseExprPrimary() {
        Maybe<LexerToken> mNext = lexer.maybeNext();
        if (!(mNext instanceof Just<LexerToken> tok)) {
            return nothing();
        }
        return switch (tok.elem().type()) {

            case SYM -> parseSimpleExpr(tok.elem());

            case MINUS, DOUBLE_PLUS, DOUBLE_MINUS -> {
                Maybe<Expr> unop = parseUnop(tok.elem());
                // unwrap negative number litterals
                if (unop instanceof Just<Expr> j
                        && j.elem() instanceof Unop inner
                        && inner.expr().isLitteral()
                        && inner.operator() == Operator.MINUS) {
                    unop = just (((Litteral<?>) inner.expr()).flipSign());
                }
                yield unop;
            }

            // todo tuple litteral
            case OPEN_PAREN -> {
                Maybe<Expr> mInner = parseBinop();
                assert lexer.peek().map(LexerToken::type).eq(CLOSE_PAREN) :
                        "Expected close parenthesis but got " + lexer.peek().toString();
                lexer.next();
                if (!(mInner instanceof Just<Expr> inner)) {
                    todo("report malformed expression");
                    yield nothing();
                }
                yield just (new PExpr(inner.elem()));
            }

            case OPEN_SQ -> {
                if (lexer.peek().test(t -> t.type() == CLOSE_SQ)) {
                    yield just(Expr.EMPTY_ARRAY);
                }

                List<Maybe<Expr>> elems = new ArrayList<>();
                while (lexer.current().type() != CLOSE_SQ) {
                    elems.add(parseBinop());
                    if (lexer.peek().test(t -> t.type() == COMMA || t.type() == CLOSE_SQ)) {
                        lexer.next();
                    }
                }
                if (elems.isEmpty()) {
                    yield just (Expr.EMPTY_ARRAY);
                }
                List<Expr> exprs = elems.stream()
                        .filter(Maybe::isJust)
                        .map(mExpr -> (Just<Expr>) mExpr)
                        .map(Just::elem)
                    .toList();
                Maybe<Type> mType = Maybe.fromOptional(exprs.stream().map(Expr::type).findFirst());
                if (!(mType instanceof Just<Type> jType)) {
                    yield todo("report malformed Array Litteral: " + tok.elem());
                }
                Type baseType = jType.elem();

                yield just (new ArrayLitteral(exprs, baseType));
            }

            case DOUBLE_QUOTE -> {
                LexerToken next = lexer.next(); // eat double-quote
                if (next.type() == SYM) {
                    lexer.next();
                    yield just(new StringLitteral(next.str()));
                }
                if (next.type() == DOUBLE_QUOTE) {
                    yield just (new StringLitteral(""));
                }
                todo("report malformed String litteral: " + tok.elem().location());
                yield nothing();
            }

            default -> todo("received " + tok.elem().debugStr());
        };
    }

    private Maybe<Expr> parseSimpleExpr(LexerToken head) {
        LexerToken first = lexer.current();
        if (first == null || (first.type() != TokenType.SYM && first.type() != TokenType.MINUS)) {
            return todo("report malformed Expr");
        }
        boolean isMinus = first.type() == TokenType.MINUS;
        if (isMinus) lexer.next();
        Maybe<Integer> maybeInt = maybeInteger(lexer.current().str());

        // number
        if (maybeInt instanceof Just<Integer> i) {
            if (lexer.peek().test(tok -> tok.type() == DOT)) {
                String num = lexer.current().str();
                lexer.next(); // eat the dot
                String decimal = lexer.nextIf(tok -> tok.type() == SYM && isInteger(tok.str()))
                        .map(LexerToken::str).orElse("");
                double d = Double.parseDouble(num + "." + decimal);
                return just (new FloatLitteral(d));
            }
            return just(new IntegerLitteral(isMinus ? -i.elem() : i.elem()));
        }

        // function call
        if (lexer.peek().test(tok -> tok.type() == OPEN_PAREN)) {
            lexer.next(); // eat paren
            List<Expr> args = new ArrayList<>();
            while (lexer.peek().test(tok -> tok.type() != CLOSE_PAREN)) {
                Maybe<Expr> mArg = parseBinop();
                if (mArg instanceof Just<Expr> jArg) {
                    args.add(jArg.elem());
                }
                if (lexer.peek().test(tok -> tok.type() == COMMA)) {
                    lexer.next();
                }
            }
            lexer.next(); // eat close paren
            return just (new FunctionCall(new Atom(first.str()), args));
        }

        // return Expr
        if (lexer.peek().test(tok -> tok.type() == SYM && Keywords.fromString(tok.str()) == Keywords.RETURN)) {
            lexer.next(); // eat return keyword
        }
        final LexerToken potentialReturn = lexer.current();
        if (potentialReturn.type() == SYM && Keywords.fromString(potentialReturn.str()) == Keywords.RETURN) {
            Maybe<Expr> mRetExpr = parseBinop();
            if (!(mRetExpr instanceof Just<Expr> jRetExpr)) {
                return todo("report error parsing return expression");
            }
            return just(new ReturnExpr(jRetExpr.elem()));
        }

        // if we see a double quote / single quote, it means we should be parsing declaration instead !!!!
        if (lexer.peek().test(tok -> tok.type() == COLON || tok.type() == DOUBLE_COLON)) {
            throw new ParsingDeclException(head);
        }

        // is there anything?
        return just(new Expr.Ref(new Atom(first.str())));

    }

    public Maybe<Expr> parseUnop(LexerToken operator) {
        return parseBinop().map(expr -> new Unop(expr, Operator.fromToken(operator), expr.type()));
    }


    private static boolean isInteger(String s) {
        return maybeInteger(s) instanceof Just<Integer>;
    }

    private static Maybe<Integer> maybeInteger(String s) {
        if (s == null || s.isEmpty()) {
            return nothing();
        }
        try {
            return just (Integer.parseInt(s));

        } catch (NumberFormatException nfe) {
            return nothing();
        }
    }
}
