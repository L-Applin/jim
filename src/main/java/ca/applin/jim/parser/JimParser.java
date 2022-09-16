package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.lexer.LexerToken.TokenType.SEMICOLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.SYM;
import static ca.applin.jim.parser.Keywords.STATIC;
import static ca.applin.jim.parser.ParserUtils.Logger.log;
import static ca.applin.jim.parser.ParserUtils.expect;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.Ast.CodeBlock;
import ca.applin.jim.ast.Stmt.ImportStmt;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.Location;
import ca.applin.jim.lexer.LexerToken.TokenType;

// .jim file parsing main entrypoint
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
        log.info("    parsing: %s", lexer.filename);
        Maybe<CodeBlock> head = nothing();
        while (lexer.hasNext()) {
            // top level for a file
            // main loop
            LexerToken token = lexer.next();
            switch (token.type()) {
                case NEW_LINE: continue;
                case AT: {
                    head = Ast.append(head, parseCommands(token.location()));
                    break;
                }
                case SYM: {
                    final Maybe<Ast> mDecl = (Maybe) declParser.parseNamed(token);
                    if (head.isNothing()) {
                        head = mDecl.map(CodeBlock::new);
                    } else {
                        Ast.append(head, mDecl);
                    }
                }
            }
        }
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
                final ImportStmt importStmt = new ImportStmt(location, fullImport.toString(), isStatic);
                yield just (importStmt);
            }

            default -> todo("report not supported @ directive '" + stmt.elem() + "'");
        };

    }

}
