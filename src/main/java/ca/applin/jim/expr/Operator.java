package ca.applin.jim.expr;


import ca.applin.jim.lexer.LexerToken;

public enum Operator {
    PLUS(0, "+"),
    MINUS(0, "-"),
    TIMES(0, "*"),
    DIV(0, "/"),
    MOD(0, "%"),
    LOGICAL_OR(0, "||"),
    LOGICAL_AND(0, "&&"),
    LOGICAL_XOR(0, "^"),
    EQ(0, "=="),
    NEQ(0, "!="),
    BIT_SHIFT_LEFT(0, "<<"),
    BIT_SHIFT_RIGHT(0, ">>"),
    UNARY_PLUS(0, "++"),
    UNARY_MINUS(0, "--"),
    ACCESSOR(0, ".")
    ;

    public final int precedence;
    public final String str;
    Operator(int precedence, String str) {
        this.precedence = precedence;
        this.str = str;
    }

    public static Operator fromToken(LexerToken tokenType) {
        return switch (tokenType.type()) {
            case PLUS -> PLUS;
            case MINUS -> MINUS;
            case TIMES -> TIMES;
            case F_SLASH -> DIV;
            case PERCENT -> MOD;
            case DOUBLE_PLUS -> UNARY_PLUS;
            case DOUBLE_MINUS -> UNARY_MINUS;
            case DOT -> ACCESSOR;
            default -> throw new IllegalArgumentException("cannot create Operatior from token '"
                    + tokenType.type() + "' located at " + tokenType.location());
        };
    }

    public boolean isUnary() {
        return this == UNARY_MINUS
            || this == UNARY_PLUS;
    }

}
