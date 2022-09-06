package ca.applin.jim.parser;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.expr.Type;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.TokenType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;
import static ca.applin.jib.utils.Maybe.to;
import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.expr.Type.*;
import static ca.applin.jim.lexer.LexerToken.TokenType.ARROW;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_PAREN;
import static ca.applin.jim.lexer.LexerToken.TokenType.CLOSE_SQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.COLON;
import static ca.applin.jim.lexer.LexerToken.TokenType.COMMA;
import static ca.applin.jim.lexer.LexerToken.TokenType.EOF;
import static ca.applin.jim.lexer.LexerToken.TokenType.EQ;
import static ca.applin.jim.lexer.LexerToken.TokenType.OPEN_CURLY;
import static ca.applin.jim.lexer.LexerToken.TokenType.PIPE;
import static ca.applin.jim.lexer.LexerToken.TokenType.SEMICOLON;

public class TypeParser implements Parser<Type> {

    private final Lexer<LexerToken> lexer;

    public TypeParser(Lexer<LexerToken> lexer) {
        this.lexer = lexer;
    }

    public Maybe<Type> parse() {
        Maybe<Type> mType = parseFunctionType();
        if (mType instanceof Just<Type> jt && jt.elem() instanceof PType pt) {
            return just (pt.unpack());
        }
        return mType;
    }

    public Maybe<Type> parseTypePrimary() {
        Maybe<LexerToken> mTok = lexer.maybeNext();
        if (!(mTok instanceof Just<LexerToken> jTok)) {
            return nothing();
        }
        final LexerToken tok = jTok.elem();
        return switch (tok.type()) {

            case SYM -> parseSimpleType(tok);

            case OPEN_PAREN -> {
                Maybe<Type> type = parseTupleType();
                assert lexer.peek().map(LexerToken::type).eq(CLOSE_PAREN) :
                        "Expected close parenthesis but got " + lexer.peek().toString();
                lexer.next();
                yield type;
            }

            case OPEN_SQ -> {
                Maybe<Type> type = parseArrayType();
                assert lexer.peek().map(LexerToken::type).eq(CLOSE_SQ) :
                        "Expected close square bracket but got " + lexer.peek().toString();
                lexer.next();
                yield type;
            }

            default -> throw new NoSuchElementException("Reached end of file while trying to parse inner :: current=" + tok);
        };
    }

    private Maybe<Type> parseArrayType() {
        Maybe<Type> arrayType = parseFunctionType();
        if (arrayType.isNothing()) {
            return nothing();
        }
        return arrayType.map(ArrayType::new);
    }

    public Maybe<Type> parseFunctionType() {
        Maybe<Type> result = parseTypePrimary();
        if (!(result instanceof Just<Type> t)) return nothing();
        Maybe<LexerToken> peeked = lexer.peek();
        if (peeked instanceof Just<LexerToken> j) {
            if (j.elem().type() == ARROW) {
                lexer.next();
                Maybe<Type> mReturnType = parseFunctionType();
                if (mReturnType.isNothing()) {
                    todo("Report error functional inner");
                }
                Type returnType = ((Just<Type>) mReturnType).elem().unpack();
                if (t.elem() instanceof PType pt) {
                    return just (new FunctionType(Arrays.asList(pt.inner()), returnType));
                }
                if (t.elem() instanceof TupleType tt) {
                    List<Type> types = tt.types();
                    return just(new FunctionType(types, returnType));
                }
                List<Type> types = Arrays.asList(t.elem());
                return just(new FunctionType(types, returnType));
            }
        }
        return result;
    }

    public Maybe<Type> parseTupleType() {
        Maybe<LexerToken> maybeNext = lexer.peek();
        List<Type> types = new ArrayList<>();
        while (maybeNext.test(tok -> tok.type() != CLOSE_PAREN)) {
            Maybe<Type> tupleElem = parseFunctionType();
            if (tupleElem instanceof Just<Type> t) {
                if (lexer.peek() instanceof Just<LexerToken> j) {
                    if (j.elem().type() == COMMA) {
                        lexer.maybeNext();
                    } else if (j.elem().type() == CLOSE_PAREN) {
                        types.add(t.elem());
                        break;
                    }
                } else {
                    todo("report error tuple");
                }
                types.add(t.elem());
            }
        }

        if (types.size() == 0) {
            return just (UNIT);
        }

        // this is important for funtion -> operator to be parsed as right associative
        if (types.size() == 1) {
            return PType.from(types.get(0));
        }
        return just (new TupleType(types));
    }


    public Maybe<Type> parseSimpleType(LexerToken current) {
        if (current.type() != TokenType.SYM) {
            return nothing();
        }
        Maybe<LexerToken> peeked = lexer.peek();
        if (peeked.isNothing() || peeked.test(this::isEndOfSimpleType)) {
            return just (new SimpleType(current.str()));
        }
        List<Type> generics = new ArrayList<>();
        Maybe<Type> type;
        while (!lexer.peek().test(this::isEndOfSimpleType)) {
            type = parseTypePrimary();
            if (type.isNothing()) break;
            Just<Type> jt = (Just<Type>) type;
            if (jt.elem() instanceof PType pt) {
                generics.add(pt.unpack());
            } else if (jt.elem() instanceof GenericType gt){
                List<Type> unpacked = flattenGenericTypes(gt);
                generics.addAll(unpacked);
                break;
            } else {
                generics.add(((Just<Type>)type).elem());
            }
        }
        return just (new GenericType(current.str(), generics));
    }

    // transform Either (A B) => Either A B
    public List<Type> flattenGenericTypes(GenericType gt) {
        List<Type> unpacked = new ArrayList<>();
        unpacked.add(new SimpleType(gt.name()));
        unpacked.addAll(gt.generics());
        return unpacked;
    }

    private boolean isEndOfSimpleType(LexerToken token) {
        TokenType type = token.type();
        return type == EQ ||
                type == COLON ||
                type == SEMICOLON ||
                type == PIPE ||
                type == COMMA ||
                type == ARROW ||
                type == CLOSE_PAREN ||
                type == CLOSE_SQ ||
                type == OPEN_CURLY ||
                type == EOF;
    }

}
