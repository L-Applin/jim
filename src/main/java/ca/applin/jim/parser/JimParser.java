package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Maybe.to;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_CURLY;
import static ca.applin.jim.lexer.LexerToken.TokenType.COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOUBLE_COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.SEMICOLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.SYM;
import static ca.applin.jim.parser.Keywords.RETURN;
import static ca.applin.jim.parser.Keywords.STATIC;
import static ca.applin.jim.parser.ParserUtils.Logger.log;
import static ca.applin.jim.parser.ParserUtils.expect;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.expr.Ast;
import ca.applin.jim.expr.Ast.CodeBlock;
import ca.applin.jim.expr.Expr;
import ca.applin.jim.expr.Stmt.ImportStmt;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.Location;
import ca.applin.jim.lexer.LexerToken.TokenType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JimParser implements Parser<Ast>  {

    Lexer<LexerToken> lexer;
    ExprParser exprParser;
    TypeParser typeParser;
    DeclParser declParser;


    public JimParser(Lexer<LexerToken> lexer) {
        this.lexer = lexer;
        this.exprParser = new ExprParser(lexer);
        this.typeParser = new TypeParser(lexer);
        this.declParser = new DeclParser(lexer);
        declParser.setJimParser(this);
    }

    @Override
    public Maybe<Ast> parse() {
        Instant start = Instant.now();
        Maybe<CodeBlock> head = nothing();
        while (lexer.hasNext()) {
            // top level for a file
            // main loop
            LexerToken token = lexer.next();
            switch (token.type()) {
                case NEW_LINE: continue;
                case AT: {
                    head = append(head, parseCommands(token.location()));
                    break;
                }
                case SYM: {
                    if (Keywords.fromString(token.str()) == RETURN) {
                        Maybe<Expr> mRetrunExpr = exprParser.parse();
                        append(head, just (new Expr.ReturnExpr(mRetrunExpr
                                .orElseThrow(new RuntimeException("Error parsing return Expr")))));
                        break;
                    }
                    final Maybe<Ast> mDecl = (Maybe) declParser.parseNamed(token);
                    if (head.isNothing()) {
                        head = mDecl.map(CodeBlock::new);
                    } else {
                        append(head, mDecl);
                    }
                }
            }
        }
        Instant end = Instant.now();
        log.info("Compilation took %.4f ms", Duration.between(start, end).getNano() / 1000000.);
        return (Maybe) head;
    }

    public Maybe<Ast> parseCommands(Location location) {
        if (!(lexer.current().type() == TokenType.AT)) {
            return todo("report malformed codeblock");
        }
        if (!(lexer.maybeNext() instanceof Just<LexerToken> stmt && stmt.elem().type() == SYM)) {
            return todo("report malformed codeblock");
        }

        final String stmtStr = stmt.elem().str();
        return switch (AtKeyword.fromString(stmtStr)) {

            case IMPORT -> {
                boolean isStatic = false;
                if (lexer.peek().test(tok -> tok.type() == SYM && STATIC.value.equals(tok.str()))) {
                    lexer.next();
                    isStatic = true;
                }
                StringBuilder fullImport = new StringBuilder();
                while (lexer.peek().test(tok -> tok.type() != SEMICOLON)) {
                    fullImport.append(lexer.next().str());
                }
                lexer.next(); // eat semicolon
                yield just (new ImportStmt(location, fullImport.toString(), isStatic));
            }

            default -> todo("report not supported @ directive '" + stmt.elem() + "'");
        };

    }

    public static Maybe<CodeBlock> append(Maybe<CodeBlock> mBlock, Maybe<Ast> mNext) {
        if (mBlock.isNothing()) {
            if (mNext instanceof Just<Ast> jNext) {
                return just(new CodeBlock(jNext.elem()));
            }
            return nothing();
        }
        if (mNext.isNothing()) {
            return mBlock;
        }
        CodeBlock cb = ((Just<CodeBlock>) mBlock).elem();
        Ast next = ((Just<Ast>) mNext).elem();
        cb.append(next);
        return mBlock;
    }

    public Maybe<CodeBlock> parseFunctionBody(LexerToken location) {
        // function body can only be Var Decl, procedure call or control flow
        Maybe<LexerToken> mPeek = lexer.peek();
        Maybe<CodeBlock> mCodeBlock = nothing();
        if (!(mPeek instanceof Just<LexerToken> jPeek)) {
            return todo("malformed function body " + location);
        }
        while (lexer.peek().test(tok -> tok.type() != CLOSE_CURLY)) {
            switch (Keywords.fromString(jPeek.elem().str())) {
                case IF -> todo("parse IF statements");
                case WHILE -> todo("parse WHILE statements");
                case CASE -> todo("parse CASE statements");
                case FOR -> todo("parse FOR statements");
                default -> {
                    Maybe<Ast> mAst = parseEitherVarDeclOrExpr();
                    if (mCodeBlock.isNothing()) {
                        if (mAst instanceof Just<Ast> jAst) {
                            mCodeBlock = just (new CodeBlock(jAst.elem()));
                        }
                    } else {
                        if (mAst instanceof Just<Ast> jAst) {
                            mCodeBlock.ifPresent(c -> c.append(jAst.elem()));
                        }
                    }
                }

            }
        }
        return mCodeBlock;
    }

    private Maybe<Ast> parseEitherVarDeclOrExpr() {
        try {
            Maybe<Expr> mExpr = exprParser.parse();
            log.debug("parsed: %s", mExpr);
            return mExpr.map(e -> e);
        } catch (ParsingDeclException pde) {
            if (lexer.maybeNext().test(colonType -> colonType.type() != COLON && colonType.type() != DOUBLE_COLON)) {
                expect(List.of(COLON, DOUBLE_COLON), lexer.peek(), pde.head);
            }
            Maybe<Ast> mAst = declParser.fromColon(pde.head,
                    lexer.peek().test(tok -> tok.type() == DOUBLE_COLON),
                    new ArrayList<>())
                .map(e -> e);
            log.debug("parsed: %s", mAst);
            return mAst;
        }
    }
}
