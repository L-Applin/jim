package ca.applin.jim.compiler;

import static ca.applin.jim.parser.ParserUtils.Logger.log;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.interp.JimInterpreter;
import ca.applin.jim.lexer.Lexer;
import ca.applin.jim.parser.JimParser;
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
        List<JimParser> parser =
                files.stream().map(file -> new JimParser(Lexer.fromFile(file))).toList();
        List<Maybe<Ast>> asts = parser.stream().map(JimParser::parse).toList();
        // todo complete
        log.info("Parsing took %.4f ms",
                Duration.between(compileStart, Instant.now()).getNano() / 1000000.);
        log.info("Starting Interp");
        Instant interpStart = Instant.now();
        asts.forEach(mAst -> mAst.ifPresent(ast -> new JimInterpreter().interp(ast)));
        log.info("Interp took %.4f ms",
                Duration.between(interpStart, Instant.now()).getNano() / 1000000.);
    }
}
