package ca.applin.jim.parser;

import static ca.applin.jib.utils.Utils.__FULL_METHOD_NAME__;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.TokenType;
import java.util.List;

public class ParserUtils {

    public static <T> T expect(List<TokenType> expected, LexerToken instead, LexerToken root) {
        String msg = "[ERROR] Expected any of '%s' but got '%s' in %s"
                .formatted(expected.toString(), instead, root);
        throw new ParserException(msg);
    }
    public static <T> T expect(TokenType expected, LexerToken instead, LexerToken root) {
        String msg = "[ERROR] Expected '%s' but got '%s' in %s"
                .formatted(expected, instead, root);
        throw new ParserException(msg);
    }

    public static <T> T expect(List<TokenType> expected, Maybe<LexerToken> mInstead, LexerToken root) {
        String msg = "[ERROR] Expected any of '%s' but got '%s' in %s".formatted(
                expected.toString(),
                mInstead instanceof Just<LexerToken> jIndtead
                        ? jIndtead.elem() : mInstead,
                root);
        throw new ParserException(msg);
    }

    public static <T> T expect(TokenType expected, Maybe<LexerToken> mInstead, LexerToken root) {
        String msg = "[ERROR] Expected '%s' but got '%s' in %s".formatted(
                expected,
                mInstead instanceof Just<LexerToken> jIndtead
                    ? jIndtead.elem() : mInstead,
                root);
        throw new ParserException(msg);
    }

    private static class ParserException extends RuntimeException {

        public ParserException(String message) {
            super(message);
        }
    }

    public static class Logger {

        public static final int TRACE = 0;
        public static final int DEBUG = 1;
        public static final int INFO  = 2;
        public static final int ERROR = 3;

        public static final Logger log = new Logger(DEBUG);

        public int level;

        public Logger(int level) {
            this.level = level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void log(int level, String msg, Object... fmt) {
            if (this.level <= level) {
                System.out.printf( getLevel(level) + " " + (msg) + "%n", fmt);
            }
        }

        private String getLevel(int level) {
            return switch (level) {
                case 0 -> "[TRACE]";
                case 1 -> "[DEBUG]";
                case 2 -> "[INFO]";
                case 3 -> "[ERROR]";
                default -> "";
            };
        }

        public void trace(String msg, Object... fmt) {
            log(TRACE, msg, fmt);
        }

        public void debug(String msg, Object... fmt) {
            log(DEBUG, msg, fmt);
        }

        public void info(String msg, Object... fmt) {
            log(INFO, msg, fmt);
        }

        public void error(String msg, Object... fmt) {
            System.err.printf((msg) + "%n", fmt);
        }
    }
}
