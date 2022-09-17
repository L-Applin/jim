package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jim.parser.TestUtils.*;
import static ca.applin.jim.lexer.LexerToken.TokenType.SYM;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Decl;
import ca.applin.jim.ast.Decl.TypeDecl;
import ca.applin.jim.ast.Decl.VarDecl;
import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.StructElem;
import ca.applin.jim.ast.Type.StructType;
import ca.applin.jim.ast.Type.SumType;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.Location;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class DeclTest {

    private static final Location TEST_LOCATION =
            new Location("<from string>", 0, 0);

    @ParameterizedTest
    @ArgumentsSource(TypeDeclArguments.class)
    public void testTypeDecl(String toParse, Decl expectedAst) {
        System.out.println("-----");
        System.out.printf("parsing '%s': ", toParse);
        Lexer<LexerToken> lexer = Lexer.fromString(toParse);
        DeclParser parser = new DeclParser(lexer);
        Maybe<Decl> actual = parser.parse();
        assertEquals(just(expectedAst), actual);
        System.out.print(actual);
    }

    static class TypeDeclArguments implements ArgumentsProvider {
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    arguments("foo :: Type = (Int, String) ;",
                            new TypeDecl(testTok("foo"), emptyList(), tuple(Type.INTEGER, Type.STRING))),
                    arguments("parser A :: Type = String -> (Maybe A, String) ;",
                            new TypeDecl(testTok("parser"), List.of("A"),
                                    fun(Type.STRING, tuple(maybe("A"), Type.STRING)))),
                    arguments("fun A B :: Type = A -> B ;",
                        new TypeDecl(testTok("fun"), List.of("A", "B"),
                                fun(type("A"), type("B")))),
                    arguments("Either A B :: Type = Left A | Right B ;",
                            new TypeDecl(testTok("Either"), List.of("A", "B"),
                                    new SumType(Map.of("Left", List.of(type("A")), "Right", List.of(type("B")))))
                    ),
                    arguments("Either A B C :: Type = Left A | Middle B | Right C;",
                            new TypeDecl(testTok("Either"), List.of("A", "B", "C"),
                                    new SumType(Map.of("Left", List.of(type("A")), "Middle", List.of(type("B")), "Right", List.of(type("C")))))
                    ),
                    // clumsy to have to specify the line nomber of the token...
                    arguments("Pair :: Type = { first: Int; second: String; }",
                            new TypeDecl(testTok("Pair"), emptyList(), new StructType(
                                Map.of("first", new StructElem(testTok("first", 0, 17), false, Type.INTEGER),
                                        "second", new StructElem(testTok("second", 0, 29), false, Type.STRING))))
                    ),
                    arguments("Pair A B :: Type = { first: A; second: B; }",
                            new TypeDecl(testTok("Pair"), List.of("A", "B"), new StructType(
                                    Map.of("first", new StructElem(testTok("first", 0, 21), false, type("A")),
                                            "second", new StructElem(testTok("second", 0, 31), false, type("B")))))
                    ),
                    arguments("Pair A B :: Type = { first :: A; second: B; }",
                            new TypeDecl(testTok("Pair"), List.of("A", "B"), new StructType(
                                    Map.of("first", new StructElem(testTok("first", 0, 21), true, type("A")),
                                            "second", new StructElem(testTok("second", 0, 33), false, type("B")))))
                    ),
                    arguments("Single A::Type={a:A;};",
                        new TypeDecl(
                            testTok("Single"),
                            List.of("A"),
                            new StructType(Map.of("a", new StructElem(testTok("a", 0, 16), false, type("A")))))),

                    // variabel decl
                    arguments("my_var :: Int = 42 ;", new VarDecl(testTok("my_var", 0, 0), true, just(Type.INTEGER), litteral(42))),
                    arguments("my_var :: Maybe Int = 42 ;", new VarDecl(testTok("my_var", 0, 0), true,
                            just(generic("Maybe", Type.INTEGER)),
                            litteral(42)))
            );
        }
    }

    static LexerToken testTok(String str) {
        return new LexerToken(SYM, str, TEST_LOCATION);
    }

    static LexerToken testTok(String str, int line, int col) {
        return new LexerToken(SYM, str, new Location("<from string>", line, col));
    }

}
