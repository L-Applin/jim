package ca.applin.jim.parser;


import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Type;
import ca.applin.jim.lexer.Lexer;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jim.parser.TestUtils.*;
import static ca.applin.jim.parser.TestUtils.type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TypeParserTest {

    @ParameterizedTest
    @ArgumentsSource(SimpleParserTestInput.class)
    public void testParser(String typeToParse, Type expected) {
        System.out.println("-----");
        System.out.printf("parsing '%s': ", typeToParse);
        TypeParser parser = new TypeParser(Lexer.fromString(typeToParse));
        Maybe<Type> actual = parser.parse();
        assertEquals(just(expected), actual);
        System.out.print(((Just<Type>)actual).elem() + "\n");
    }

    static class SimpleParserTestInput implements ArgumentsProvider {
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    // Simple types
                    arguments("A", type("A")),
                    arguments("Int", type("Int")),
                    arguments("String", type("String")),
                    arguments("List", type("List")),

                    // Generic
                    arguments("Maybe A", generic("Maybe", type("A"))),
                    arguments("Either A B",
                            generic("Either", type("A"), type("B"))),
                    arguments("Either (A) (B)",
                            generic("Either", type("A"), type("B"))),
                    arguments("Result Value Err Next",
                            generic("Result", type("Value"), type("Err"), type("Next"))),
                    arguments("Either (A -> B) B",
                            generic("Either",
                                fun(List.of(type("A")), type("B")),
                                type("B"))),
                    arguments("Either A (A -> B)",
                            generic("Either",
                                    type("A"),
                                    fun(List.of(type("A")), type("B")))),
                    arguments("Maybe (A -> B)",
                        generic("Maybe", fun(List.of(type("A")), type("B")))),
                    arguments("Cons A (List A)",
                            generic("Cons", type("A"), generic("List", type("A")))),

                    // Tuples
                    arguments("(String)", STRING_TYPE),
                    arguments("(String, Int)", tuple(STRING_TYPE, INT_TYPE)),
                    arguments("(F A, F B)", tuple(
                            generic("F", type("A")),
                            generic("F", type("B")))),
                    arguments("(String, Int, Maybe A)", tuple(
                            STRING_TYPE,
                            INT_TYPE,
                            generic("Maybe", type("A"))
                    )),
                    arguments("((String, Int), (Maybe A, String), (Error Val Msg, Int, String))",
                        tuple(
                            tuple(STRING_TYPE, INT_TYPE),
                            tuple(
                                generic("Maybe", type("A")),
                                STRING_TYPE),
                            tuple(
                                generic("Error", type("Val"), type("Msg")),
                                INT_TYPE,
                                STRING_TYPE))
                    ),

                    // Function
                    arguments("A -> B", fun(List.of(type("A")), type("B"))),
                    arguments("(A) -> B", fun(List.of(type("A")), type("B"))),
                    arguments("(A) -> (B)", fun(List.of(type("A")), type("B"))),
                    arguments("(A, B) -> C", fun(List.of(type("A"), type("B")), type("C"))),
                    arguments("(A, B) -> (C, D)", fun(List.of(type("A"), type("B")), tuple("C", "D"))),
                    arguments("(Maybe A) -> B", fun(List.of(generic("Maybe", type("A"))), type("B"))),
                    arguments("Maybe A -> B", fun(List.of(generic("Maybe", type("A"))), type("B"))),
                    arguments("(A -> B, F A)", tuple(fun(type("A"), type("B")), generic("F", type("A")))),
                    arguments("(A -> B, F A) -> F B", fun(
                            List.of(fun(type("A"), type("B")), generic("F", type("A"))),
                            generic("F", type("B")))
                    ),
                    arguments("(A -> B, F A) -> F A B", fun(
                            List.of(fun(type("A"), type("B")), generic("F", type("A"))),
                            generic("F", type("A"), type("B")))
                    ),

                    // Array
                    arguments("[A]", array(type("A"))),
                    arguments("[Maybe A]", array(generic("Maybe", type("A")))),
                    arguments("[(A, B)]", array(tuple(type("A"), type("B")))),
                    arguments("[A -> B]", array(fun(List.of(type("A")), type("B")))),
                    arguments("[A] -> B", fun(List.of(array(type("A"))), type("B"))),
                    arguments("A -> [B]", fun(List.of(type("A")), array(type("B")))),
                    arguments("[A] -> [B]", fun(List.of(array(type("A"))), array(type("B")))),
                    arguments("[Maybe (A -> B)]", array(generic("Maybe", fun(List.of(type("A")), type("B"))))),
                    arguments("[[A]]", array(array(type("A")))),
                    arguments("(A, [B])", tuple(type("A"), array(type("B")))),

                    // lets try to break it
                    arguments("A -> B -> C -> D -> E -> F",
                            fun(type("A"), fun(type("B"), fun(type("C"), fun(type("D"), fun(type("E"), type("F"))))))
                    ),
                    arguments("Maybe A -> B -> Either C D -> E",
                        fun(generic("Maybe", type("A")), fun(type("B"), fun(generic("Either", type("C"), type("D")), type("E"))))
                    ),
                    arguments("Either (Maybe A) (Either B C)",
                        generic("Either", generic("Maybe", type("A")), generic("Either", type("B"), type("C")))
                    )

            );
        }
    }

}