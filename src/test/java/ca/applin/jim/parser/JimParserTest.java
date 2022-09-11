package ca.applin.jim.parser;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jim.parser.TestUtils.litteral;
import static org.junit.jupiter.api.Assertions.*;

import ca.applin.jib.utils.Just;
import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.Ast.CodeBlock;
import ca.applin.jim.lexer.Lexer;
import org.junit.jupiter.api.Test;

class JimParserTest {

    @Test
    public void testJimParser() {
        String toParse =
         """
         @import static java.lang.System.out.*;
        
         const_a :: Int = 42;
         const_b :: Int = 69;
         
         my_fun :: Int -> Int = index -> {
             var: Int = index + 1;
             return var.value + 2;
         }

         my_fun_2 :: (Int, Int, Int) -> Int = (index, it, another) -> {
             var: Int = i + j;
             return var + 2;
         }

         main :: [String] -> Void = args -> {
            index  :: Int = my_fun(42);
            index2 :: Int = my_fun(69);
            for [0, 1, 2, 3, 4, 5] {
              println(it + index + index2);
            }
            my_array: [Int] = [10, 20, 30, 40, 50];
            for my_array {
                println(it);
            }
         }
         """;
        System.out.println(toParse);
        Maybe<Ast> mAst = new JimParser(Lexer.fromString(toParse)).parse();
        assertTrue(mAst.isJust());
        Ast ast = ((Just<Ast>)mAst).elem();
        System.out.println(ast);
    }

    @Test
    void testAppend() {
        Maybe<CodeBlock> cb1 = just(new CodeBlock(litteral(1), null));
        Ast.append(cb1, just(litteral(2)));
        Ast.append(cb1, just(litteral(3)));
        Ast.append(cb1, just(litteral(4)));
        Ast.append(cb1, just(litteral(5)));
        System.out.println(cb1);
    }
}