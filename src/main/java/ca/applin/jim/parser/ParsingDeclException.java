package ca.applin.jim.parser;

import ca.applin.jim.lexer.LexerToken;

public class ParsingDeclException extends RuntimeException {
    public final LexerToken head;

    public ParsingDeclException(LexerToken head) {
        this.head = head;
    }
}
