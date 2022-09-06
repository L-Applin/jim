package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Utils.__METHOD__;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.expr.Type.GenericType;
import static ca.applin.jim.expr.Type.SimpleType;
import static ca.applin.jim.expr.Type.StructElem;
import static ca.applin.jim.expr.Type.StructType;
import static ca.applin.jim.expr.Type.SumType;
import static ca.applin.jim.lexer.LexerToken.TokenType.ARROW;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_CURLY;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.COMMA;
import static ca.applin.jim.lexer.LexerToken.TokenType.DOUBLE_COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.EQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.OPEN_CURLY;
import static ca.applin.jim.lexer.LexerToken.TokenType.OPEN_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.PIPE;
import static ca.applin.jim.lexer.LexerToken.TokenType.SEMICOLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.SYM;
import static ca.applin.jim.parser.ParserUtils.Logger.log;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.expr.Ast;
import ca.applin.jim.expr.Ast.Atom;
import ca.applin.jim.expr.Ast.CodeBlock;
import ca.applin.jim.expr.Decl;
import ca.applin.jim.expr.Decl.FunctionDecl;
import ca.applin.jim.expr.Decl.TypeDecl;
import ca.applin.jim.expr.Decl.VarDecl;
import ca.applin.jim.expr.Expr;
import ca.applin.jim.expr.Type;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeclParser implements Parser<Decl> {

    private Lexer<LexerToken> lexer;
    private Parser<Type> typeParser;
    private ExprParser exprParser;
    private JimParser jimParser;

    DeclParser(Lexer<LexerToken> lexer,
            Parser<Type> typeParser, ExprParser exprParser) {
        this.lexer = lexer;
        this.typeParser = typeParser;
        this.exprParser = exprParser;
    }

    public DeclParser(Lexer<LexerToken> lexer) {
        this(lexer, new TypeParser(lexer), new ExprParser(lexer));
    }

    public void setJimParser(JimParser jimParser) {
        this.jimParser = jimParser;
    }

    @Override
    public Maybe<Decl> parse() {
        return parseNamed(lexer.current());
    }

    public Maybe<Decl> parseNamed(LexerToken name) {
        if (name == null) {
            name = lexer.next();
        }
//        Maybe<LexerToken> next = lexer.maybeNext();
        Maybe<LexerToken> next = lexer.peek();
        if (!(next instanceof Just<LexerToken> jNext)) {
            todo("report malformed delaration");
            return nothing ();
        }
        final LexerToken afterName = jNext.elem();
        return switch (afterName.type()) {
            case SYM -> {
                lexer.next();
                List<String> genericVar = new ArrayList<>();
                Maybe<LexerToken> curr = jNext;
                while (!curr.test(tok -> COLON.equals(tok.type()) || DOUBLE_COLON.equals(tok.type()))) {
                    genericVar.add(((Just<LexerToken>) curr).elem().str());
                    curr = lexer.maybeNext();
                }
                yield fromColon(name, lexer.current().type() == DOUBLE_COLON, genericVar);
            }
            case COLON -> {
                lexer.next();
                yield fromColon(name, false, new ArrayList<>());
            }
            case DOUBLE_COLON -> {
                lexer.next();
                yield fromColon(name, true, new ArrayList<>());
            }
            case BANG -> parseMarco();
            default ->  nothing();
        };
    }

    public Maybe<Decl> fromColon(LexerToken declName, boolean isDoubleColon, List<String> genericsVar) {
        Maybe<LexerToken> mKeyword = lexer.peek();
        if (!(mKeyword instanceof Just<LexerToken> jKeyword)){
            return todo("report malformed declaration. Current=" + lexer.current());
        }
        LexerToken afterColon = jKeyword.elem();
        return switch (Keywords.fromString(afterColon.str())) {

            // todo type requirements (Applicative F :: Class <F: Functor> { ...
            case TYPE -> {
                if (!isDoubleColon) {
                    todo("report Type Decl must be final: " + lexer.current());
                }
                lexer.next();
                yield parseTypeDecl(declName, genericsVar);
            }

            case CLASS -> {
                todo("report Class Decl must be final: " + lexer.current());
                lexer.next();
                yield parseClassDecl();
            }

            case IMPLEMENTATION -> {
                todo("report Class Decl must be final");
                lexer.next();
                yield parseImplementationDecl();
            }

            default -> {
                if (afterColon.type() == EQ) {
                    // infered type variable
                    // todo infered function types
                    yield parseVarDecl(declName, isDoubleColon, nothing());
                }
                Maybe<Type> mType = typeParser.parse();
                if (! (mType instanceof Just<Type> jType)) {
                    yield todo("report malformed type");
                }
                if (lexer.peek().test(tok -> tok.type() != EQ)) {
                    todo("report missing `EQ` but got" + lexer.peek());
                }
                lexer.next(); // eat EQ
                yield jType.elem().isFunctionType()
                        ? parseFunctionDecl(declName)
                        : parseVarDecl(declName, isDoubleColon, jType);
            }
        };
    }

    public Maybe<Decl> parseTypeDecl(LexerToken declName, List<String> genericsVar) {
        Maybe<LexerToken> next = lexer.maybeNext();
        if (!(next instanceof Just<LexerToken> jt && jt.elem().type() == EQ )) {
            todo("report malformed type decl :: expected '=' but got " + next);
        }
        // parse struct
        if (lexer.peek().test(tok -> tok.type() == OPEN_CURLY)) {
            lexer.next();
            Map<String, StructElem> elems = new HashMap<>();
            while (lexer.peek().test(tok -> !CLOSE_CURLY.equals(tok.type()))) {
                // parse struct elem
                LexerToken name = lexer.next();
                if (!SYM.equals(name.type())) {
                    todo("report malformed Strcut elem :: exepect struct elem name but got " + name);
                }
                LexerToken colon = lexer.next();
                if (!(COLON.equals(colon.type()) || DOUBLE_COLON.equals(colon.type()))) {
                    todo("report malformed Strcut elem, expected ':'");
                }
                Maybe<Type> type = typeParser.parse();
                if (!(type instanceof Just<Type> jt)) {
                    todo("report malformed Strcut elem, invalid type");
                    return nothing();
                }
                elems.put(name.str(), new StructElem(name.location(), new Atom(name.str()), colon.type() == DOUBLE_COLON, jt.elem()));
                if (lexer.peek().test(tok -> tok.type().equals(SEMICOLON))) {
                    lexer.next();
                }
            }
            return just (new TypeDecl(declName.location(), new Atom(declName.str()), genericsVar, new StructType(elems)));
        }
        Maybe<Type> head = typeParser.parse();
        if (!(head instanceof Just<Type> justHead)) {
            todo("report malformed type");
            return nothing();
        }
        LexerToken seperator = lexer.next();
        if (SEMICOLON.equals(seperator.type())) {
            return just(new TypeDecl(declName.location(), new Atom(declName.str()), genericsVar, justHead.elem()));
        }
        // parse Sum types
        if (PIPE.equals(seperator.type())) {
            List<Type> algebraicTypes = new ArrayList<>();
            algebraicTypes.add(justHead.elem());
            LexerToken curr = seperator;
            while (!SEMICOLON.equals(curr.type())) {
                Maybe<Type> type = typeParser.parse();
                type.ifPresent(algebraicTypes::add);
                Maybe<LexerToken> maybeNext = lexer.maybeNext();
                if (!(maybeNext instanceof Just<LexerToken> jNext)) {
                    todo("report malformed type decl, probably missing ';'");
                    return nothing ();
                }
                curr = jNext.elem();
            }
            Map<String, List<Type>> constructors = algebraicTypes.stream()
                    .collect(Collectors.toMap(this::typeToConstructorName, this::typeToConstructorArgs));
            return just (new TypeDecl(declName.location(), new Atom(declName.str()), genericsVar, new SumType(constructors)));
        }
        todo("report malformed type decl, not a '|' or ';'");
        return nothing();
    }

    private String typeToConstructorName(Type type) {
        if (type instanceof SimpleType simpleType) {
            return simpleType.name();
        }
        if (type instanceof GenericType gt) {
            return gt.name();
        }
        return todo("Malformed type::" + type.toString() + " requiresx constructor");
    }

    private List<Type> typeToConstructorArgs(Type type) {
        if (type instanceof SimpleType) {
            return new ArrayList<>();
        }
        if (type instanceof GenericType gt) {
            return gt.generics();
        }
        todo("Malformed type::" + type.toString() + " requires constructor");
        return new ArrayList<>();
    }


    public Maybe<Decl> parseClassDecl() {
        return todo("parse Class decl not implemented yet");
    }

    public Maybe<Decl> parseImplementationDecl() {
        return todo("parse Class decl not implemented yet");
    }

    // must be after the EQ token
    public Maybe<Decl> parseVarDecl(LexerToken declName, boolean isConst, Maybe<Type> mType) {
        Maybe<Expr> mExpr = exprParser.parse();
        if ( !(mExpr instanceof Just<Expr> jExp)) {
            todo("report malformed expr");
            return nothing();
        }
        return just (new VarDecl(declName.location(), new Atom(declName.str()), isConst, mType, jExp.elem()));
    }

    private Maybe<Decl> parseMarco() {
        return todo("parse Macros not yet implemented");
    }

    private Maybe<Decl> parseFunctionDecl(LexerToken location) {
        assert jimParser != null;
        assert lexer.current().type() == EQ : "Lexer not at EQ symbol beofre parsing function body";

        List<Atom> args = new ArrayList<>();

        // single arg functions may not have parenthesis
        if (lexer.maybeNext().test(tok -> tok.type() != OPEN_PAREN)) {
            if (lexer.current().type() != SYM) {
                return todo("report malformed function arguments, current:" + lexer.current());
            }
            args.add(new Atom(lexer.current().str()));

        } else {
            // multiple arg fucntion, we at the OPEN_PAREN
            while (lexer.maybeNext().test(tok -> tok.type() != ARROW)) {
                args.add(new Atom(lexer.current().str()));
                // eat comma
                final Maybe<LexerToken> maybeCommaOrCloseParen = lexer.peek();
                if (maybeCommaOrCloseParen.test(tok -> tok.type() != COMMA && tok.type() != CLOSE_PAREN)) {
                    todo("repost missing comma or close paren in function arguments");
                }
                lexer.next();// eat comma or close paren
            }
        }
        if (args.size() == 1 && lexer.current().type() != ARROW) {
            lexer.next();
        }
        if (lexer.current().type() != ARROW) {
            ParserUtils.expect(ARROW, lexer.current(), location);
        }

        // todo no curly required when body is single expr
        Maybe<LexerToken> mCurly = lexer.maybeNext();
        if (!mCurly.test(tok -> tok.type() == OPEN_CURLY)) {
            todo("report missing open curly in function declaration: " + mCurly);
        }
        Maybe<CodeBlock> codeBlock = jimParser.parseFunctionBody(location);
        final FunctionDecl funcDecl = new FunctionDecl(location.location(),
                new Atom(location.str()),
                args,
                codeBlock.orElseThrow(new RuntimeException("error while parsing function body")));
        log.debug("parsed: %s", funcDecl);
        return just(funcDecl);
    }

}
