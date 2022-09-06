package ca.applin.jim.lexer;

import static ca.applin.jim.lexer.LexerToken.TokenType.*;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ca.applin.jim.lexer.LexerToken.Location;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class LexerTest {

    @ParameterizedTest
    @ArgumentsSource(LexerTestInput.class)
    public void testLexer(String str, List<LexerToken> tokens) {
        Lexer<LexerToken> lexer = Lexer.fromString(str);
        assertEquals(asList(tokens.iterator()), asList(lexer.iterator()));
        System.out.println("-------");
        System.out.println(str);
        Lexer.fromString(str).forEach(System.out::println);
    }

    static class LexerTestInput implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments("Int", List.of(tok(SYM, "Int", 0, 0))),
                arguments("    Int    ", List.of(tok(SYM, "Int", 0, 4))),
                arguments("(Int)",
                    List.of(
                        tok(OPEN_PAREN, "(", 0, 0),
                        tok(SYM, "Int", 0, 1),
                        tok(CLOSE_PAREN, ")", 0, 4)
                )),
                arguments("(Int, Int)",
                    List.of(
                        tok(OPEN_PAREN, "(", 0, 0),
                        tok(SYM, "Int", 0, 1),
                        tok(COMMA, ",", 0, 4),
                        tok(SYM, "Int", 0, 6),
                        tok(CLOSE_PAREN, ")", 0, 9)
                )),
                arguments("[ Maybe A ]",
                    List.of(
                        tok(OPEN_SQ, "[", 0, 0),
                        tok(SYM, "Maybe", 0, 2),
                        tok(SYM, "A", 0, 8),
                        tok(CLOSE_SQ, "]", 0, 10)
                )),
                arguments("String\nInt",
                    List.of(
                        tok(SYM, "String", 0, 0),
                        tok(SYM, "Int", 1, 0)
                )),
                    arguments("\tString",
                   List.of(
                        tok(SYM, "String", 0, 1)
                   )),
                    arguments("String -> Int", List.of(
                        tok(SYM, "String", 0, 0),
                        tok(ARROW, "->", 0, 7),
                        tok(SYM, "Int", 0, 10)
                )),
                arguments("`~!@#$%^&*()-=+[]\\{}|;':,./<>?::||&&>><<!=", List.of(
                        tok(BACKTICK, "`", 0, 0),
                        tok(TILDE, "~", 0, 1),
                        tok(BANG, "!", 0, 2),
                        tok(AT, "@", 0, 3),
                        tok(POUND, "#", 0, 4),
                        tok(DOLLAR, "$", 0, 5),
                        tok(PERCENT, "%", 0, 6),
                        tok(CARET, "^", 0, 7),
                        tok(AMPERSAND, "&", 0, 8),
                        tok(TIMES, "*", 0, 9),
                        tok(OPEN_PAREN, "(", 0, 10),
                        tok(CLOSE_PAREN, ")", 0, 11),
                        tok(MINUS, "-", 0, 12),
                        tok(EQ, "=", 0, 13),
                        tok(PLUS, "+", 0, 14),
                        tok(OPEN_SQ, "[", 0, 15),
                        tok(CLOSE_SQ, "]", 0, 16),
                        tok(BACKSLASH, "\\", 0, 17),
                        tok(OPEN_CURLY, "{", 0, 18),
                        tok(CLOSE_CURLY, "}", 0, 19),
                        tok(PIPE, "|", 0, 20),
                        tok(SEMICOLON, ";", 0, 21),
                        tok(QUOTE, "'", 0, 22),
                        tok(COLON, ":", 0, 23),
                        tok(COMMA, ",", 0, 24),
                        tok(DOT, ".", 0, 25),
                        tok(F_SLASH, "/", 0, 26),
                        tok(OPEN_CHEV, "<", 0, 27),
                        tok(CLOSE_CHEV, ">", 0, 28),
                        tok(QUESTION, "?", 0, 29),
                        tok(DOUBLE_COLON, "::", 0, 30),
                        tok(DOUBLE_PIPE, "||", 0, 32),
                        tok(DOUBLE_AMP, "&&", 0, 34),
                        tok(DOUBLE_GT, ">>", 0, 36),
                        tok(DOUBLE_LT, "<<", 0, 38),
                        tok(NEQ, "!=", 0, 40)
                )),
                arguments("//should be empty as this is a comment", List.of(
                    tok(EOF, "<EOF>", 1, 0)
                )),
                arguments("String\n//should be empty as this is a comment\nInt", List.of(
                    tok(SYM, "String", 0, 0),
                    tok(SYM, "Int", 2, 0)
                )),
                arguments("\"this is a String litteral\"", List.of(
                        tok(DOUBLE_QUOTE, "\"", 0, 0),
                        tok(SYM, "this is a String litteral", 0, 1),
                        tok(DOUBLE_QUOTE, "\"", 0, 26)
                )),
                arguments("\"    this   is a String litteral      \"", List.of(
                        tok(DOUBLE_QUOTE, "\"", 0, 0),
                        tok(SYM, "    this   is a String litteral      ", 0, 1),
                        tok(DOUBLE_QUOTE, "\"", 0, 38)
                )),
                arguments("\"this is a\\nString litteral\"", List.of(
                    tok(DOUBLE_QUOTE, "\"", 0, 0),
                    tok(SYM, "this is a\nString litteral", 0, 1),
                    tok(DOUBLE_QUOTE, "\"", 0, 26)
                ))
            );
        }
    }

    private static LexerToken tok(LexerToken.TokenType type, String str, int line, int col) {
        return new LexerToken(type, str, new Location("<from string>", line, col));
    }

    private static <T> List<T> asList(Iterator<T> iter) {
        return stream(((Iterable<T>)() -> iter).spliterator(), false).toList();
    }

}