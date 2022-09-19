package ca.applin.jim.compiler;

import static ca.applin.jim.parser.ParserUtils.Logger.log;

import ca.applin.jib.utils.Maybe;
import ca.applin.jib.utils.Pair;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.bytecode.gen.ClassBytecodeGenerator;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.parser.JimParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class JimCompiler {
    public void compile(String... files) {
        compile(Arrays.asList(files));
    }

    public void compile(List<String> files) {
        log.info("Starting compilation");
        Instant compileStart = Instant.now();
        List<Pair<String, Maybe<Ast>>> asts = files.stream()
                .map(file -> new Pair<>(file, new JimParser(Lexer.fromFile(file)).parse()))
                .toList();
        // todo complete
        log.info("Done parsing");
        final Instant parsingEnd = Instant.now();
        log.info("Starting bytecode generation");
        Instant bytecodeStart = Instant.now();
        asts.forEach(pair -> pair.snd().ifPresent(ast -> generateByteCode(ast, pair.fst())));
        log.info("Done generating bytecode");
        log.info("============");
        log.info("Parsing took: %.4f ms",
                Duration.between(compileStart, parsingEnd).getNano() / 1000000.);
        log.info("Bytecode generation took: %.4f ms",
                Duration.between(bytecodeStart, Instant.now()).getNano() / 1000000.);
    }

    void generateByteCode(Ast ast, String filename) {
        System.out.println(ast);
        final Path path = Paths.get(filename);
        String file = removeExtenstion(path.getFileName().toString());
        String _package = "jim";
        log.info("    generating: " + filename);
        ClassBytecodeGenerator gen = new ClassBytecodeGenerator(_package, file);
        byte[] content = gen.generate(ast);
        try {
            Path baseDir = Paths.get("target", "generated-test-sources", "classes",  _package);
            Files.createDirectories(baseDir);
            final Path savePath = Paths.get(baseDir.toString(), file + ".class");
            FileOutputStream fos = new FileOutputStream(savePath.toFile());
            log.info("    saving: " + savePath);
            fos.write(content);
            fos.flush();
        } catch (IOException ioe ) {
            log.error("Error while printing file: " + ioe);
            ioe.printStackTrace();
        }

    }

    private static String removeExtenstion(String filepath) {
        if (filepath != null && filepath.contains(".")) {
            final int index = filepath.lastIndexOf('.');
            if (index > -1) {
                return filepath.substring(0, index);
            }
        }
        return filepath;
    }

}
