package ca.applin.jim.examples;

import static java.util.Collections.singletonList;

import java.io.File;
import java.io.StringWriter;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class JimCompiler {

    static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public static void main(String[] args) throws Exception {
        JavaSourceClass jsc = new JavaSourceClass(
                 "ca.applin.jim","TestClass", true,
                """
                   int member = 42;
                   public static void test(String arg) {
                     System.out.println("Hello, World: " + arg); 
                   }
                   public int inst(int i) {
                     return this.member + i; 
                   }
                    """
        );
        JavaSourceFromString jsfs = new JavaSourceFromString(jsc);
        System.out.println(jsfs);
        Iterable<JavaFileObject> fileObjects = singletonList(jsfs);
        StringWriter output = new StringWriter();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        List<String> options = new ArrayList<>();
        options.add("-d");
        String compilationPath = "/Users/Applin/Documents/develop/jim/target/classes";
        options.add(compilationPath);

        CompilationTask task = compiler.getTask(output, null, diagnostics, options, null, fileObjects);
        boolean res = task.call();
        if(res) {
            System.out.println("Class has been successfully compiled");
        } else {
            diagnostics.getDiagnostics().forEach(System.out::println);
            throw new RuntimeException("Compilation failed :" + output);
        }

        Class.forName(jsc.fullName()).getDeclaredMethod("test", String.class)
                .invoke(null, "from another workd");

        Object target = Class.forName(jsc.fullName()).getConstructor().newInstance();
        Method method = Class.forName(jsc.fullName()).getMethod("inst", int.class);
        Integer i = (Integer) method.invoke(target, 69);
        System.out.println(i);

    }

    public static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        public JavaSourceFromString(JavaSourceClass source) {
            super(URI.create("string:///"
                            + source.fullName().replace('.','/')
                            + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.code = source.format();
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    record JavaSourceClass(
            String packageName,
            String className,
            boolean isPublic,
            String code)  {
        public String format() {
           return "package " + packageName() + ";\n"
                   + (isPublic() ? "public " : "")
                   + "class "
                   + className() + " {\n"
                   + code()
                   + "\n}";

       }
       public String fullName() {
            return packageName() + "." + className();
       }
    }
}
