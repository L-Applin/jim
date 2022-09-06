package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jim.expr.Operator.DIV;
import static ca.applin.jim.expr.Operator.MOD;
import static ca.applin.jim.expr.Operator.PLUS;
import static ca.applin.jim.expr.Operator.TIMES;
import static ca.applin.jim.parser.TestUtils.INT_TYPE;
import static ca.applin.jim.parser.TestUtils.array;
import static ca.applin.jim.parser.TestUtils.binop;
import static ca.applin.jim.parser.TestUtils.fcall;
import static ca.applin.jim.parser.TestUtils.litteral;
import static ca.applin.jim.parser.TestUtils.p;
import static ca.applin.jim.parser.TestUtils.var;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ca.applin.jim.expr.Expr;
import ca.applin.jim.expr.Expr.FloatLitteral;
import ca.applin.jim.expr.Expr.IntegerLitteral;
import ca.applin.jim.expr.Expr.Ref;
import ca.applin.jim.expr.Expr.StringLitteral;
import ca.applin.jim.expr.Expr.Unop;
import ca.applin.jim.expr.Operator;
import ca.applin.jim.expr.Type;
import ca.applin.jim.lexer.Lexer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class ExprParserTest {

    @ParameterizedTest
    @ArgumentsSource(ExprParserTestProvider.class)
    public void testParseExpr(String toParse, Expr expected) {
        ExprParser exprParser = new ExprParser(Lexer.fromString(toParse));
        assertEquals(just(expected), exprParser.parse());
    }

    static class ExprParserTestProvider implements ArgumentsProvider {
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                // number litterals
                arguments("1", litteral(1)),
                arguments("10", litteral(10)),
                arguments("010", litteral(10)),
                arguments("00100", litteral(100)),
                arguments("2147483647", litteral(2147483647)),
                arguments("-1", litteral(-1)),
                arguments("-10", litteral(-10)),
                arguments("-01", litteral(-1)),
                arguments("-010", litteral(-10)),
                arguments("-00100", litteral(-100)),
                arguments("1.5", litteral(1.5)),
                arguments("0.9999999", litteral(0.9999999)),
                arguments("-1.5", litteral(-1.5)),
                arguments("my_var", var("my_var")),

                // binop
                arguments("1 + 2", binop(PLUS, litteral(1), litteral(2))),
                arguments("1 + 2 * 3", binop(PLUS, litteral(1), binop(TIMES, litteral(2), litteral(3)))), // todo fix wrong precedence
                arguments("1 + 2 * 3 / 4 % 5", // todo fix wrong precedence
                   binop(PLUS, litteral(1),
                       binop(TIMES, litteral(2),
                           binop(DIV, litteral(3),
                               binop(MOD, litteral(4), litteral(5)))))
                ),
                arguments("(1 + 2)", binop(PLUS, litteral(1), litteral(2))),
                arguments("1 + (2 + 3)",
                    binop(PLUS, litteral(1), p(binop(PLUS, litteral(2), litteral(3))))
                ),
                arguments("(1 + 2) + 3",
                    binop(PLUS, p(binop(PLUS, litteral(1), litteral(2))), litteral(3))
                ),

                // function call
                arguments("f()", fcall("f")),
                arguments("f(g())", fcall("f", fcall("g"))),
                arguments("fun(1)", fcall("fun", litteral(1))),
                arguments("fun(my_var)", fcall("fun", var("my_var"))),
                arguments("fun(1, 2)", fcall("fun", litteral(1), litteral(2))),
                arguments("fun(1 + 2)", fcall("fun", binop(PLUS, litteral(1), litteral(2)))),
                arguments("fun(1, 2, -3.5)", fcall("fun", litteral(1), litteral(2), litteral(-3.5))),
                arguments("fun(1 + 2 + 3)", fcall("fun", binop(PLUS, litteral(1), binop(PLUS, litteral(2), litteral(3))))),
                arguments("1 + fun(2 * 3, 4) + var5",
                        binop(PLUS, litteral(1), binop(PLUS, fcall("fun", binop(TIMES, litteral(2), litteral(3)), litteral(4)), var("var5")))
                ),
                arguments("fun(g(1), h(2+3))",
                    fcall("fun", fcall("g", litteral(1)), fcall("h", binop(PLUS, litteral(2), litteral(3))))
                ),

                // string litterals
                arguments("\"this is a string litteral\"", new StringLitteral("this is a string litteral")),
                    arguments("\"\"", new StringLitteral("")),
                arguments("1 + \"2\"", binop(PLUS, litteral(1), litteral("2"))),
                    arguments("1 + \"   002\"", binop(PLUS, litteral(1), litteral("   002"))),

                // Array Litteral
                arguments("[1, 2, 3, 4, 5]",
                    array(Type.INTEGER, Stream.of(1, 2, 3, 4, 5).map(i -> (Expr) new IntegerLitteral(i)).toList())
                ),
                arguments("[]", array(Type.UNKNOWN, new ArrayList<>())),
                arguments("[1 + 2, 3, 4, 5]",
                        array(Type.UNKNOWN, List.of(binop(PLUS, litteral(1), litteral(2)), litteral(3), litteral(4), litteral(5)))
                ),
                arguments("[\"one\", \"two\", \"three\"]",
                    array(Type.STRING, Stream.of("one", "two", "three").map(s -> (Expr) new StringLitteral(s)).toList())),
                arguments("[\"\", \"\", \"\"]",
                        array(Type.STRING, Stream.of("", "", "").map(s -> (Expr) new StringLitteral(s)).toList()))
            );
        }
    }
}