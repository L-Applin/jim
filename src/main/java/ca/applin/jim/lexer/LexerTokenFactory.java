package ca.applin.jim.lexer;

import ca.applin.jim.lexer.LexerToken.Location;
import ca.applin.jim.lexer.LexerToken.TokenType;

public interface LexerTokenFactory<TOKEN extends LexerToken> {

    LexerTokenFactory<LexerToken> DEFAULT = new DefaultLexerTokenFactory();

    TOKEN makeToken(char c, Location location);

    TOKEN makeToken(TokenType type, String str, Location location);

    class DefaultLexerTokenFactory implements LexerTokenFactory<LexerToken> {

        @Override
        public LexerToken makeToken(TokenType type, String str, Location location) {
            return new LexerToken(type, str, location);
        }

        public LexerToken makeToken(char c, Location location) {
            return switch (c) {
                case '(' -> new LexerToken(TokenType.OPEN_PAREN, "(", location);
                case ')' -> new LexerToken(TokenType.CLOSE_PAREN, ")", location);
                case '[' -> new LexerToken(TokenType.OPEN_SQ, "[", location);
                case ']' -> new LexerToken(TokenType.CLOSE_SQ, "]", location);
                case '{' -> new LexerToken(TokenType.OPEN_CURLY, "{", location);
                case '}' -> new LexerToken(TokenType.CLOSE_CURLY, "}", location);
                case '=' -> new LexerToken(TokenType.EQ, "=", location);
                case '+' -> new LexerToken(TokenType.PLUS, "+", location);
                case '-' -> new LexerToken(TokenType.MINUS, "-", location);
                case '*' -> new LexerToken(TokenType.TIMES, "*", location);
                case '/' -> new LexerToken(TokenType.F_SLASH, "/", location);
                case '%' -> new LexerToken(TokenType.PERCENT, "%", location);
                case '<' -> new LexerToken(TokenType.OPEN_CHEV, "<", location);
                case '>' -> new LexerToken(TokenType.CLOSE_CHEV, ">", location);
                case '^' -> new LexerToken(TokenType.CARET, "^", location);
                case '|' -> new LexerToken(TokenType.PIPE, "|", location);
                case '&' -> new LexerToken(TokenType.AMPERSAND, "&", location);
                case ',' -> new LexerToken(TokenType.COMMA, ",", location);
                case '.' -> new LexerToken(TokenType.DOT, ".", location);
                case '!' -> new LexerToken(TokenType.BANG, "!", location);
                case '@' -> new LexerToken(TokenType.AT, "@", location);
                case '#' -> new LexerToken(TokenType.POUND, "#", location);
                case '$' -> new LexerToken(TokenType.DOLLAR, "$", location);
                case '~' -> new LexerToken(TokenType.TILDE, "~", location);
                case '`' -> new LexerToken(TokenType.BACKTICK, "`", location);
                case '?' -> new LexerToken(TokenType.QUESTION, "?", location);
                case ':' -> new LexerToken(TokenType.COLON, ":", location);
                case ';' -> new LexerToken(TokenType.SEMICOLON, ";", location);
                case '\'' -> new LexerToken(TokenType.QUOTE, "'", location);
                case '"' -> new LexerToken(TokenType.DOUBLE_QUOTE, "\"", location);
                case '\\' -> new LexerToken(TokenType.BACKSLASH, "\\", location);
                case '\n' -> new LexerToken(TokenType.NEW_LINE, "\\n", location);
                default -> throw new IllegalArgumentException(
                        "expoected a special char but received '" + c + "' ");
            };
        }
    }
}
