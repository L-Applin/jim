package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Utils.__METHOD__;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.ast.Type.GenericType;
import static ca.applin.jim.ast.Type.SimpleType;
import static ca.applin.jim.ast.Type.StructElem;
import static ca.applin.jim.ast.Type.StructType;
import static ca.applin.jim.ast.Type.SumType;
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
import static ca.applin.jim.parser.Keywords.CASE;
import static ca.applin.jim.parser.Keywords.FOR;
import static ca.applin.jim.parser.Keywords.IF;
import static ca.applin.jim.parser.Keywords.WHILE;
import static ca.applin.jim.parser.ParserUtils.Logger.log;
import static ca.applin.jim.parser.ParserUtils.endOfFile;
import static ca.applin.jim.parser.ParserUtils.expect;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.Ast.Atom;
import ca.applin.jim.ast.Ast.CodeBlock;
import ca.applin.jim.ast.Decl;
import ca.applin.jim.ast.Decl.FunctionDecl;
import ca.applin.jim.ast.Decl.TypeDecl;
import ca.applin.jim.ast.Decl.VarAssign;
import ca.applin.jim.ast.Decl.VarDecl;
import ca.applin.jim.ast.Expr;
import ca.applin.jim.ast.Stmt.ForStmt;
import ca.applin.jim.ast.Type;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.parser.ParserUtils.ParserException;
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
        this.exprParser.setDeclParser(this);
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
        LexerToken afterName = lexer.peek().orElseThrow(endOfFile(name.location()));
        return switch (afterName.type()) {
            case EQ -> {
                Ast ast = exprParser.parse()
                        .orElseThrow(new ParserException("error while parsing expresion: " + name));
                yield just(new VarAssign(name.location(), new Atom(name.str()), (Expr) ast));
            }

            case SYM -> {
                lexer.next();
                List<String> genericVar = new ArrayList<>();
                LexerToken curr = afterName;
                while (!(COLON.equals(curr.type()) || DOUBLE_COLON.equals(curr.type()))) {
                    genericVar.add(curr.str());
                    curr = lexer.maybeNext().orElseThrow(endOfFile(name.location()));
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
        LexerToken afterColon = lexer.peek().orElseThrow(endOfFile(declName.location()));
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
                    // todo infered function types:
                    lexer.next();
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
                        ? parseFunctionDecl(declName, jType.elem())
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
        Ast ast = exprParser.parse().orElseThrow(endOfFile(declName.location()));
        return just (new VarDecl(declName.location(), new Atom(declName.str()), isConst, mType, (Expr) ast));
    }

    private Maybe<Decl> parseMarco() {
        return todo("parse Macros not yet implemented");
    }

    private Maybe<Decl> parseFunctionDecl(LexerToken location, Type type) {
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
        Maybe<CodeBlock> codeBlock = parseCodeBlock(location);
        final FunctionDecl funcDecl = new FunctionDecl(location.location(),
                new Atom(location.str()),
                just(type),
                args,
                codeBlock.orElseThrow(new RuntimeException("error while parsing function body")));
        return just(funcDecl);
    }


    public Maybe<CodeBlock> parseCodeBlock(LexerToken location) {
        // function body can only be Var Decl, procedure call or control flow
        Maybe<CodeBlock> mCodeBlock = nothing();
        while (lexer.peek().test(tok -> tok.type() != CLOSE_CURLY)) {
            LexerToken peek = lexer.peek().orElseThrow(endOfFile(location));
            switch (Keywords.fromString(peek.str())) {
                case IF -> todo("parse IF statements");
                case WHILE -> todo("parse WHILE statements");
                case CASE -> todo("parse CASE statements");

                case FOR -> {
                    lexer.next(); // eat FOR keyword
                    Ast iterator = exprParser.parse()
                            .orElseThrow(new ParserUtils.ParserException("Malformed iterator expression"));
                    Maybe<CodeBlock> mForCodeBlock;
                    if (lexer.peek().orElseThrow(endOfFile(location)).type() == OPEN_CURLY) {
                        lexer.next();
                        mForCodeBlock = parseCodeBlock(lexer.current());
                    } else {
                        mForCodeBlock = exprParser.parse().map(CodeBlock::new);
                    }
                    CodeBlock forCodeBlocl = mForCodeBlock.orElseThrow(new ParserException("Error parsing FOR code block", peek));
                    if (lexer.peek().orElseThrow(endOfFile(location)).type() == CLOSE_CURLY) {
                        lexer.next();
                    }
                    ForStmt forStmt = new ForStmt(peek.location(), (Expr) iterator, forCodeBlocl);
                    if (mCodeBlock.isNothing()) {
                        mCodeBlock = just(new CodeBlock(forStmt));
                    } else {
                        mCodeBlock.ifPresent(c -> c.append(forStmt));
                    }
                }

                case UNKNOWN, RETURN, THIS -> {
                    Maybe<Ast> mAst = exprParser.parse();
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

                // not allowed here
                case TYPE, CLASS, IMPLEMENTATION, STATIC ->
                    throw new ParserUtils.ParserException("[ERROR] expected any of " +
                            List.of(IF, WHILE, CASE, FOR) + " but got " + peek);
            }
        }
        return mCodeBlock;
    }

}
