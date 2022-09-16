package ca.applin.jim.bytecode;

import static ca.applin.jim.bytecode.Attribute_Info.code;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MAJOR_VERION;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MINOR_VERION;
import static ca.applin.jim.bytecode.Constant_Pool_Info.class_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.methodRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.nameAndType_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.utf8_info;
import static ca.applin.jim.bytecode.Instruction.aload_0;
import static ca.applin.jim.bytecode.Instruction.bytecode;
import static ca.applin.jim.bytecode.Instruction.iadd;
import static ca.applin.jim.bytecode.Instruction.iload_1;
import static ca.applin.jim.bytecode.Instruction.iload_2;
import static ca.applin.jim.bytecode.Instruction.invokespecial;
import static ca.applin.jim.bytecode.Instruction.invokevirtual;
import static ca.applin.jim.bytecode.Instruction.ireturn;
import static ca.applin.jim.bytecode.Instruction.return_void;
import static org.junit.jupiter.api.Assertions.*;

import ca.applin.jim.bytecode.Attribute_Info.Exception_Table;
import ca.applin.jim.bytecode.Class_File.Flag;
import ca.applin.jim.utils.DirectByteClassLoader;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BytecodeTest {

    /** <pre>
     *  package jim;
     *  public class TestAuto {
     *      public int add(int i, int j) {
     *          return i + j;
     *      }
     *  }
     * </pre>
     */
    @Test
    public void add_method() throws Exception {
        Constant_Pool_Info[] infos = new Constant_Pool_Info[]{
                // Constant_Pool is indexed from 1
                methodRef_info((short) 2, (short) 3),               // #1
                class_info((short) 4),                              // #2
                nameAndType_info((short) 5, (short) 6),             // #3
                utf8_info("java/lang/Object"),                      // #4
                utf8_info("<init>"),                                // #5
                utf8_info("()V"),                                   // #6
                class_info((short) 8),                              // #7
                utf8_info("jim/TestAuto"),                          // #8
                utf8_info("add"),                                   // #9
                utf8_info("(II)I"),                                 // #10
                nameAndType_info((short) 9,(short) 10),             // #11
                methodRef_info((short) 7, (short) 11),              // #12
                utf8_info("Code")                                   // #13
        };

        // local var: 0=this, 1=i, 2=j
        byte[] add_code = bytecode(
                iload_1(),
                iload_2(),
                iadd(),
                ireturn()
        );
        Attribute_Info[] add_attributes = new Attribute_Info[] {
                code(13, (short) 2, (short) 3, add_code, new Exception_Table[0], new Attribute_Info[0])
        };
        Method add_method = new Method(Method.FLAG_ACC_PUBLIC, 9, 10, add_attributes.length, add_attributes);

        // <init> required method
        byte[] init_code = bytecode(
                aload_0(), // load `this`
                invokevirtual((byte) 0x00, (byte) 0x01, (short) 0),
                return_void()
        );
        Attribute_Info[] init_code_attr = new Attribute_Info[] {
                code(13, (short) 1, (short) 1, init_code, new Exception_Table[0], new Attribute_Info[0])
        };
        Method init_method = new Method(Method.FLAG_ACC_PUBLIC, 5, 6, init_code_attr.length, init_code_attr);

        Class_File test_class = new Class_File(
                JAVA_11_CLASS_MINOR_VERION,
                JAVA_11_CLASS_MAJOR_VERION,
                infos,
                (short) (Flag.ACC_PUBLIC | Flag.ACC_SUPER),
                (short) 7,
                (short) 2,
                new Interface[0],
                new Field[0],
                new Method[] { init_method, add_method },
                new Attribute_Info[0]
        );
        final byte[] content = test_class.get_content();
        Files.createDirectories(Path.of("target/generated-test-sources/classes/jim/"));
        FileOutputStream fos = new FileOutputStream("target/generated-test-sources/classes/jim/TestAuto.class");
        fos.write(content);
        fos.flush();
        Class<?> clz = new DirectByteClassLoader(content).findClass("jim.TestAuto");
        Object o = clz.getConstructor().newInstance(); // an instance of jim.BytecodeTestAdd
        int res = (int) clz.getMethod("add", int.class, int.class).invoke(o, 69, 42);
        assertEquals(111, res);
    }


}