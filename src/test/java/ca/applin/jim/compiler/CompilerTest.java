package ca.applin.jim.compiler;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CompilerTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "src/test/resources/Simple.jim"
    })
    public void testParseFile(String filepath) throws Exception {
        JimCompiler compiler = new JimCompiler();
        compiler.compile(filepath);
        try (URLClassLoader urlClassLoader = new URLClassLoader(
            new URL[]{ new File("target/generated-test-sources/classes/").toURI().toURL()})) {
            urlClassLoader.loadClass("jim.Simple").getMethod("main", String[].class)
                    .invoke(null, (Object) new String[0]);
        } catch (ClassNotFoundException cnfe) {
            fail(cnfe.getMessage(), cnfe);
        }
    }

}
