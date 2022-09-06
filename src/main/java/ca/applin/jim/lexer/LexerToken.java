package ca.applin.jim.lexer;

public record LexerToken(TokenType type, String str, Location location) {
    public enum TokenType {
        SYM,
        OPEN_PAREN, CLOSE_PAREN,
        OPEN_SQ, CLOSE_SQ,
        OPEN_CURLY, CLOSE_CURLY,
        OPEN_CHEV, CLOSE_CHEV,
        EQ, NEQ, PLUS, MINUS, TIMES, F_SLASH, PERCENT,
        CARET, PIPE, AMPERSAND, COMMA, DOT,
        BANG, AT, POUND, DOLLAR, TILDE, BACKTICK, QUESTION,
        COLON, SEMICOLON,
        QUOTE, DOUBLE_QUOTE,
        BACKSLASH,
        NEW_LINE,
        ARROW,
        DOUBLE_COLON, DOUBLE_LT, DOUBLE_GT, DOUBLE_PLUS, DOUBLE_MINUS, DOUBLE_EQ,
        DOUBLE_PIPE, DOUBLE_AMP,
        WS,
        EOF
    }

    public record Location(String filename, int line, int col) {
        @Override
        public String toString() {
            return "%s @ (%d,%d)".formatted(filename(), line(), col());
        }
    }

    public String debugStr() {
        return "LexerToken[%s='%s']".formatted(type(), str());
    }

}
