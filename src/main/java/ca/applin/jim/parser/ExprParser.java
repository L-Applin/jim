package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Utils.__FULL_METHOD_NAME__;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_SQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.COMMA;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOT;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOUBLE_COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOUBLE_QUOTE;
import static ca.applin.jim.lexer.LexerToken.TokenType.EQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.OPEN_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.SEMICOLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.SYM;
import static ca.applin.jim.parser.ParserUtils.expect;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jib.utils.Nothing;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.Ast.Atom;
import ca.applin.jim.ast.Decl.VarAssign;
import ca.applin.jim.ast.Expr;
import ca.applin.jim.ast.Expr.ArrayLitteral;
import ca.applin.jim.ast.Expr.Binop;
import ca.applin.jim.ast.Expr.DeRef;
import ca.applin.jim.ast.Expr.DoubleLitteral;
import ca.applin.jim.ast.Expr.FunctionCall;
import ca.applin.jim.ast.Expr.IntegerLitteral;
import ca.applin.jim.ast.Expr.Litteral;
import ca.applin.jim.ast.Expr.PExpr;
import ca.applin.jim.ast.Expr.ReturnExpr;
import ca.applin.jim.ast.Expr.StringLitteral;
import ca.applin.jim.ast.Expr.Unop;
import ca.applin.jim.ast.Intrinsic;
import ca.applin.jim.ast.Operator;
import ca.applin.jim.ast.Type;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.TokenType;
import ca.applin.jim.parser.ParserUtils.ParserException;
import java.util.ArrayList;
import java.util.List;

public class ExprParser implements Parser<Ast> {
    private Lexer<LexerToken> lexer;
    private DeclParser declParser;

    public ExprParser(Lexer<LexerToken> lexer) {
        this.lexer = lexer;
    }

    public void setDeclParser(DeclParser declParser) {
        this.declParser = declParser;
    }

    @Override
    public Maybe<Ast> parse() {
        return parseBinop(nothing()).map(Ast::unpack);
    }

    public Maybe<Ast> parseWithPeek() {
        return parseBinop(nothing());
    }

    public Maybe<Ast> parseBinop(Maybe<Expr> mLeft) {
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
        return mLeft.map(l -> l);
    }

    public Maybe<Ast> parseBinop() {
        Maybe<Ast> mLeft = parseExprPrimary();
        if (! (mLeft instanceof Just<Ast> jLeft)) {
            todo("report malformed Expr");
            return nothing();
        }
        Maybe<LexerToken> maybeOp = lexer.peek();
        if (maybeOp.test(tok -> tok.type() == SEMICOLON)) {
            lexer.next(); // eat SEMI
            return mLeft.map(a -> (Expr) a);
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
            return parseBinop(just(new DeRef((Expr) jLeft.elem(), new Atom(ref.str()))));
        }

        if (maybeOp.test(this::isBinopExpr)) {
            lexer.next();
            return parseBinopRhs((Expr) jLeft.elem());
        }
        return mLeft;
    }

    private Maybe<Ast> parseBinopRhs(Expr left) {
        LexerToken op = lexer.current();
        Ast mNext = parseBinop()
                .orElseThrow(new ParserException("error parsiong exception"));
        if ( !(mNext instanceof Expr right)) {
            todo("report malformed Expr");
            return nothing();
        }
        // todo fix precedence
        return just(new Binop(left, right, Operator.fromToken(op)));
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
    public Maybe<Ast> parseExprPrimary() {
        Maybe<LexerToken> mTok = lexer.maybeNext();
        if (mTok instanceof Nothing) {
            return nothing();
        }
        LexerToken tok = mTok.orElseThrow(ParserUtils.endOfFile(lexer.current().location()));

        return switch (tok.type()) {

            case SYM -> parseSimpleExpr(tok);

            case MINUS, DOUBLE_PLUS, DOUBLE_MINUS -> {
                Maybe<Expr> unop = parseUnop(tok);
                // unwrap negative number litterals
                if (unop instanceof Just<Expr> j
                        && j.elem() instanceof Unop inner
                        && inner.expr().isLitteral()
                        && inner.operator() == Operator.MINUS) {
                    unop = just (((Litteral<?>) inner.expr()).flipSign());
                }
                yield unop.map(u -> u);
            }

            // todo tuple litteral
            case OPEN_PAREN -> {
                Ast aInner = parseBinop()
                        .orElseThrow(new ParserException("error parsing expr"));
                assert lexer.peek().map(LexerToken::type).eq(CLOSE_PAREN) :
                        "Expected close parenthesis but got " + lexer.peek().toString();
                lexer.next();
                if (!(aInner instanceof Expr inner)) {
                    todo("report malformed expression");
                    yield nothing();
                }
                yield just (new PExpr(inner));
            }

            case OPEN_SQ -> {
                if (lexer.peek().test(t -> t.type() == CLOSE_SQ)) {
                    yield just(Expr.EMPTY_ARRAY);
                }

                List<Maybe<Ast>> elems = new ArrayList<>();
                while (lexer.current().type() != CLOSE_SQ) {
                    elems.add(parseBinop());
                    if (lexer.peek().test(t -> t.type() == COMMA || t.type() == CLOSE_SQ)) {
                        lexer.next();
                    }
                }
                if (elems.isEmpty()) {
                    yield just (Expr.EMPTY_ARRAY);
                }
                // toco catch class cast exception Ast -> Expr
                List<Expr> exprs = elems.stream()
                        .filter(Maybe::isJust)
                        .map(mExpr -> (Just<Ast>) mExpr)
                        .map(Just::elem)
                        .map(ast -> (Expr) ast)
                    .toList();
                Maybe<Type> mType = Maybe.fromOptional(exprs.stream().map(Expr::type).findFirst());
                if (!(mType instanceof Just<Type> jType)) {
                    yield todo("report malformed Array Litteral: " + tok);
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
                yield todo("report malformed String litteral: " + tok.location());
            }

            default -> todo("received " + tok);
        };
    }

    private Maybe<Ast> parseSimpleExpr(LexerToken head) {
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
                return just (new DoubleLitteral(d));
            }
            return just(new IntegerLitteral(isMinus ? -i.elem() : i.elem()));
        }

        // function call
        if (lexer.peek().test(tok -> tok.type() == OPEN_PAREN)) {
            lexer.next(); // eat paren
            List<Expr> args = new ArrayList<>();
            while (lexer.peek().test(tok -> tok.type() != CLOSE_PAREN)) {
                Ast mArg = parseBinop().orElseThrow(new ParserException("error parsing expr: " + head));
                if (mArg instanceof Expr expr) {
                    args.add(expr);
                }
                if (lexer.peek().test(tok -> tok.type() == COMMA)) {
                    lexer.next();
                }
            }
            lexer.next(); // eat close paren
            // check intrinsics
            Atom atom = new Atom(first.str());
            if (Intrinsic.intrinsics.contains(atom)) {
                return just (Intrinsic.from(atom, args));
            }
            return just (new FunctionCall(first.location(), atom, args));
        }

        // return Expr
        if (lexer.peek().test(tok -> tok.type() == SYM && Keywords.fromString(tok.str()) == Keywords.RETURN)) {
            lexer.next(); // eat return keyword
        }
        final LexerToken potentialReturn = lexer.current();
        if (potentialReturn.type() == SYM && Keywords.fromString(potentialReturn.str()) == Keywords.RETURN) {
            if (!(parseBinop().orElseThrow(new ParserException("error parsing expr: " + head))
                    instanceof Expr retExpr)) {
                return todo("report error parsing return expression");
            }
            return just(new ReturnExpr(retExpr));
        }

        // if we see a double quote / single quote, it means we should be parsing declaration instead !!!!
        if (lexer.peek().test(tok -> tok.type() == COLON || tok.type() == DOUBLE_COLON)) {
            lexer.next();
            return declParser.fromColon(head,
                    lexer.peek().test(tok -> tok.type() == DOUBLE_COLON),
                    new ArrayList<>())
                    .map(e -> e);
        }

        // if we see an EQ, it means we should be parsing var assign
        if (lexer.peek().test(tok -> tok.type() == EQ)){
            lexer.next(); // eat EQ
            Ast ast = parseBinop().orElseThrow(new ParserException("error parsing expr for var assignement " +
                    head));
            return just(new VarAssign(head.location(), new Atom(head.str()), (Expr) ast));
        }

        // is there anything?
        return just(new Expr.Ref(new Atom(first.str())));

    }

    public Maybe<Expr> parseUnop(LexerToken operator) {
        return parseBinop().flatMap(ast ->
            ast instanceof Expr expr
                ? just(new Unop(expr, Operator.fromToken(operator), expr.type()))
                : nothing());
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
