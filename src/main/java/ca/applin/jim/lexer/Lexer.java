package ca.applin.jim.lexer;

import static ca.applin.jim.lexer.LexerToken.TokenType.*;
import static ca.applin.jim.lexer.SpecialChars.DEFAULT_SPECIAL_CHARS;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jib.utils.Peekable;
import ca.applin.jib.utils.Utils;
import ca.applin.jim.lexer.LexerToken.Location;
import ca.applin.jim.lexer.LexerToken.TokenType;
import ca.applin.jim.parser.ParserUtils;
import ca.applin.jim.parser.ParserUtils.ParserException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.function.Supplier;

// todo: double symbol? like ==, ++, +=, -> etc?
public class Lexer<TOKEN extends LexerToken> implements Iterable<TOKEN>, Peekable<TOKEN> {

    private final Peekable<Character> chars;
    private final SpecialChars specialChars;
    private final LexerTokenFactory<TOKEN> tokenFactory;
    public final String filename;

    private final Peekable<TOKEN> iter;

    private int line, col;

    private boolean withinStringLitteral = false;

    public Lexer(Peekable<Character> chars,
            LexerTokenFactory<TOKEN> tokenFactory,
            SpecialChars specialChars,
            String filename) {
        this.chars = chars;
        this.filename = filename;
        this.specialChars = specialChars;
        this.line = 0; this.col = 0;
        this.tokenFactory = tokenFactory;
        this.iter = iterator();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Maybe<TOKEN> peek() {
        return iter.peek();
    }

    @Override
    public TOKEN current() {
        return iter.current();
    }

    @Override
    public Maybe<TOKEN> maybeNext() {
        return iter.maybeNext();
    }

    @Override
    public Maybe<TOKEN> nextIf(Predicate<TOKEN> predicate) {
        return iter.nextIf(predicate);
    }

    @Override
    public TOKEN next() {
        return iter.next();
    }


    @Override
    public Peekable<TOKEN> iterator() {
        return Peekable.fromIterator(new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (withinStringLitteral) {
                    return chars.hasNext();
                }
                Maybe<Character> peeked = chars.peek();
                if (!(peeked instanceof Just<Character> j)) {
                    return false;
                }
                if (j.test(specialChars::isSkippable)) {
                    while (specialChars.isSkippable(chars.next())) {
                        col++;
                        if (chars.peek().isNothing()) {
                            return false;
                        }
                        if (!chars.peek().test(specialChars::isSkippable)) {
                            break;
                        }
                    }
                }
                return chars.hasNext();
            }

            @Override
            public TOKEN next() {
                StringBuilder sb = new StringBuilder();
                Character current;
                int currentSymLen = 0;
                while (chars.hasNext()) {
                    current = chars.next();
                    // string litterals
                    if (withinStringLitteral) {
                        if (current == '\n') {
                            throw new LexerException("no multiline Strings: " + new Location(filename, line, col));
                        }
                        if (current == '\\') {
                            col++;
                            currentSymLen++;
                            char c = chars.next();
                            switch (c) {
                                // todo other escape chars?
                                case 'n' -> sb.append("\n");
                                case 't' -> sb.append("\t");
                                case '\\' -> sb.append("\\");
                                case '"' -> sb.append("\"");
                                default -> throw new LexerException("Illegal escape character '%s' [%s]"
                                        .formatted(c + "", new Location(filename, line, col).toString()));
                            }
                            continue;
                        }
                        if (current == '"') {
                            withinStringLitteral = false;
                            col++;
                            return tokenFactory.makeToken(DOUBLE_QUOTE, "\"", new Location(filename, line, col));
                        }
                        sb.append(current);
                        col++;
                        currentSymLen++;
                        if (chars.peek().test(c -> c == '"')) {
                            // todo location
                            return tokenFactory.makeToken(SYM, sb.toString(), new Location(filename, line,col - (currentSymLen - 1)));
                        }
                        continue;
                    }

                    // regular lexing
                    if (current == null) {
                        throw new NoSuchElementException();
                    }

                    if (current == '"') {
                        withinStringLitteral = true;
                        return tokenFactory.makeToken(DOUBLE_QUOTE, "\"", new Location(filename, line, col));
                    }

                    if (specialChars.isSkippable(current)) {
                        col++;
                        currentSymLen++;
                        continue;
                    }

                    if (specialChars.isSpecial(current)) {
                        // todo generelize 2 char tokens
                        if (current == '-' && chars.peek().eq('>')) {
                            return makeDoubleCharToken(ARROW, "->");
                        }

                        if (current == ':' && chars.peek().eq(':')) {
                            return makeDoubleCharToken(DOUBLE_COLON, "::");
                        }

                        if (current == '!' && chars.peek().eq('=')) {
                            return makeDoubleCharToken(NEQ, "!=");
                        }

                        if (current == '<' && chars.peek().eq('<')) {
                            return makeDoubleCharToken(DOUBLE_LT, "<<");
                        }

                        if (current == '>' && chars.peek().eq('>')) {
                            return makeDoubleCharToken(DOUBLE_GT, ">>");
                        }

                        if (current == '+' && chars.peek().eq('+')) {
                            return makeDoubleCharToken(DOUBLE_PLUS, "++");
                        }

                        if (current == '>' && chars.peek().eq('>')) {
                            return makeDoubleCharToken(DOUBLE_MINUS, ">>");
                        }

                        if (current == '|' && chars.peek().eq('|')) {
                            return makeDoubleCharToken(DOUBLE_PIPE, "||");
                        }

                        if (current == '&' && chars.peek().eq('&')) {
                            return makeDoubleCharToken(DOUBLE_AMP, "&&");
                        }

                        // todo triple chars???

                        // todo handle comments more generic?
                        if (current == '/' && chars.peek().eq('/')) {
                            while (chars.hasNext() && chars.next() != '\n') { }
                            line++; col = 0;
                            continue;
                        }

                        // todo fix multiline comments
                        if (current == '/' && chars.peek().eq('*')) {
                            while (chars.hasNext() && chars.next() != '*' && !chars.peek().eq('/')) {
                                col++;
                                if(chars.current() == '\n') {
                                    line++; col = 0;
                                }
                            }
                            continue;
                        }
                        TOKEN token = tokenFactory.makeToken(current, new Location(filename, line, col));
                        if (current == '\n') {
                            line++;
                            col = 0;
                            continue;
                        } else {
                            col++;
                        }
                        return token;
                    }
                    sb.append(current);
                    if (!chars.hasNext()) {
                        TOKEN token = tokenFactory.makeToken(SYM, sb.toString(),
                                new Location(filename, line, col - currentSymLen));
                        col++;
                        return token;
                    }
                    Maybe<Character> next = chars.peek();

                    if ((next.isNothing() || next.eq(' ') || next.test(specialChars::isSpecial) || next.test(specialChars::isSkippable))) {
                        String tokenName = sb.toString();
                        TOKEN token = tokenFactory.makeToken(SYM, tokenName,
                                new Location(filename, line, col - currentSymLen));
                        col++;
                        return token;
                    }
                    col++;
                    currentSymLen++;
                }
                // todo handle EOF correctly
                return tokenFactory.makeToken(EOF, "<EOF>", new Location(filename, line, col));
            }
        });
    }

    private TOKEN makeDoubleCharToken(TokenType tokenType, String str) {
        chars.next();
        TOKEN token = tokenFactory.makeToken(tokenType, str, new Location(filename, line, col));
        col+=2;
        return token;
    }

    public TOKEN nextOr(Supplier<TOKEN> onNoToken) {
        if ( !(maybeNext() instanceof Just<TOKEN> token)) {
            return onNoToken.get();
        }
        return token.elem();
    }

    public static Lexer<LexerToken> fromString(String str) {
        Iterator<Character> strIter = Utils.characters(str).iterator();
        return new Lexer<>(
                Peekable.fromIterator(strIter),
                LexerTokenFactory.DEFAULT,
                DEFAULT_SPECIAL_CHARS,
                "<from string>");
    }

    public static Lexer<LexerToken> fromFile(String filepath) {
        try {
            // default charset of the JVM is UTF-8, so that is what is supportedj
            BufferedReader buff = new BufferedReader(new FileReader(filepath, Charset.defaultCharset()));
            Lexer<LexerToken> lexer = new Lexer<>(
                Peekable.fromIterator(new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        try {
                            return buff.ready();
                        } catch (IOException e) {
                            return false;
                        }
                    }
                    @Override
                    public Character next() {
                        try {
                            return (char) buff.read();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }),
                LexerTokenFactory.DEFAULT,
                DEFAULT_SPECIAL_CHARS,
                filepath
            );
            lexer.line = 1;
            return lexer;
        } catch (IOException e) {
            throw new ParserException("Cannot open file " + filepath);
        }
    }
}
