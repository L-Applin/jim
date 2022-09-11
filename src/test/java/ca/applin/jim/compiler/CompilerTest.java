package ca.applin.jim.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.applin.jim.compiler.JimCompiler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CompilerTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/resources/simple.jim",
            "src/test/resources/test-parse.jim"

    })
    public void testParseFile(String filepath) {
        JimCompiler compiler = new JimCompiler();
        compiler.compile(filepath);
    }

}
