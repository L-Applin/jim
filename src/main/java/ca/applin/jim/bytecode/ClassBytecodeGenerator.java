package ca.applin.jim.bytecode;


import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.bytecode.Attribute_Info.code;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MAJOR_VERION;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MINOR_VERION;
import static ca.applin.jim.bytecode.Constant_Pool_Info.class_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.fieldRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.methodRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.nameAndType_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.string_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.utf8_info;
import static ca.applin.jim.bytecode.Instruction.aload_0;
import static ca.applin.jim.bytecode.Instruction.bytecode;
import static ca.applin.jim.bytecode.Instruction.getstatic;
import static ca.applin.jim.bytecode.Instruction.invokespecial;
import static ca.applin.jim.bytecode.Instruction.invokevirtual;
import static ca.applin.jim.bytecode.Instruction.ldc;
import static ca.applin.jim.bytecode.Instruction.return_void;

import ca.applin.jib.utils.Just;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.AstVisitorAdapter;
import ca.applin.jim.ast.Decl.FunctionDecl;
import ca.applin.jim.ast.Expr;
import ca.applin.jim.ast.Expr.IntegerLitteral;
import ca.applin.jim.ast.Expr.ReturnExpr;
import ca.applin.jim.ast.Expr.StringLitteral;
import ca.applin.jim.ast.Intrinsic.Print;
import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.IntegerType;
import ca.applin.jim.ast.Type.StringType;
import ca.applin.jim.ast.Type.Void;
import ca.applin.jim.bytecode.Attribute_Info.Exception_Table;
import ca.applin.jim.bytecode.Class_File.Flag;
import ca.applin.jim.parser.ParserUtils.ParserException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/*
Example: System.out.prinln("Hello world")
constant_pool:
        ...
    #7 = Fieldref            #8.#9          // java/lang/System.out:Ljava/io/PrintStream;
    #8 = Class               #10            // java/lang/System
    #9 = NameAndType         #11:#12        // out:Ljava/io/PrintStream;
    #10 = Utf8               java/lang/System
    #11 = Utf8               out
    #12 = Utf8               Ljava/io/PrintStream;
    #13 = String             #14            // Hello, World!
    #14 = Utf8               Hello, World!
    #15 = Methodref          #16.#17        // java/io/PrintStream.println:(Ljava/lang/String;)V
    #16 = Class              #18            // java/io/PrintStream
    #17 = NameAndType        #19:#20        // println:(Ljava/lang/String;)V
    #18 = Utf8               java/io/PrintStream
    #19 = Utf8               println
    #20 = Utf8               (Ljava/lang/String;)V
code:
    getstatic     #7                        // Field java/lang/System.out:Ljava/io/PrintStream;
    ldc           #13                       // String Hello, World!
    invokevirtual #15                       // Method java/io/PrintStream.println:(Ljava/lang/String;)V
 */
public class ClassBytecodeGenerator extends AstVisitorAdapter {

    public static final FieldRef SYSTEM_OUT_FIELD_REF = new FieldRef(
            "java/lang/System",
            "out",
            "Ljava/io/PrintStream;");

    public static final MethodRef PRINT_LN_VOID_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "()V"
    );

    public static final MethodRef PRINT_LN_STR_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V"
    );

    public static final MethodRef PRINT_LN_INT_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "(I)V"
    );

    public static final Constant_Pool_Info UTF8_CODE_CONSTANT_POOL_INFO = utf8_info("Code");
    public static final short UTF8_CODE_CONSTANT_POOL_INFO_INDEX = 7;

    static List<Constant_Pool_Info> SUPER_OBJECT = List.of(
            methodRef_info((short) 2, (short) 3),
            class_info((short) 4),
            nameAndType_info((short) 5, (short) 6),
            utf8_info("java/lang/Object"),
            utf8_info("<init>"),
            utf8_info("()V")
    );

    private String _package, file;

    public ClassBytecodeGenerator(String _package, String file) {
        this._package = _package;
        this.file = file;
    }

    private final Map<FieldRef, Integer> fieldRefs  = new HashMap<>();
    private final Map<MethodRef, Integer> methodRefs = new HashMap<>();
    private final Map<FunctionDecl, List<Instruction>> instructions = new HashMap<>();
    private final Stack<FunctionDecl> funcDecls = new Stack<>();

    private short max_stack = 0, current_stack = 0, max_locals = 0, current_locals = 0;

    private final TypeDescriptorMapper typeDescriptorMapper = new TypeDescriptorMapper();
    private final List<Constant_Pool_Info> constantPoolInfos = new ArrayList<>() {{
        addAll(SUPER_OBJECT);
        add(UTF8_CODE_CONSTANT_POOL_INFO); // index #7
    }};

    private List<Method> methods = new ArrayList<>();

    // (Ljava/util/List<Ljava/lang/String;>;)V
    // (Ljava/util/List<Ljava/lang/String;>;)V
    public byte[] generate(Ast ast) {
        byte[] init_code = bytecode(
                aload_0(), // load `this`
                invokespecial((byte) 0x00, (byte) 0x01, (short) 0), // call Object.<init>
                return_void()
        );
        Attribute_Info[] init_code_attr = new Attribute_Info[] {
                code(UTF8_CODE_CONSTANT_POOL_INFO_INDEX, (short) 1, (short) 1, init_code, new Exception_Table[0], new Attribute_Info[0])
        };
        Method init_method = new Method(Method.FLAG_ACC_PUBLIC, 5, 6, init_code_attr.length, init_code_attr);
        methods.add(init_method);
        ast.visit(this);
        constantPoolInfos.add(class_info(addUtf8ToConstantPool(_package + "/" + file)));
        int this_class = constantPoolInfos.size();
        Class_File test_class = new Class_File(
                JAVA_11_CLASS_MINOR_VERION,
                JAVA_11_CLASS_MAJOR_VERION,
                constantPoolInfos.toArray(new Constant_Pool_Info[0]),
                (short) (Flag.ACC_PUBLIC | Flag.ACC_SUPER),
                (short) this_class,
                (short) 2,
                new Interface[0],
                new Field[0],
                methods.toArray(new Method[0]),
                new Attribute_Info[0]
        );
        return test_class.get_content();
    }

    @Override
    public void visit(FunctionDecl funDecl) {
        max_stack = 0;
        max_locals = (short) (funDecl.args().size() + 1);
        funcDecls.push(funDecl);
        instructions.put(funDecl, new ArrayList<>());
        Type type = funDecl.type()
                .orElseThrow(new ParserException("Unknown type: " + funDecl.type()));
        String typeDescr = typeDescriptorMapper.typeToDescriptor(type);
        int typeDescriptorIndex = addUtf8ToConstantPool(typeDescr);
        int name_index = addUtf8ToConstantPool(funDecl.name().value());
        funDecl.body().visit(this); // will generate the Instructions in instructions map
        List<Instruction> code = instructions.get(funDecl);
        Type funType = funDecl.type().orElseThrow(new ParserException("Type unknown for funtion +" + funDecl.name() + ": " + funDecl.location()));
        if (!(funType instanceof FunctionType ft)) {
            todo("report non function type");
            return;
        }

        if (ft.returnType() instanceof Void) {
            code.add(return_void());
        }

        Attribute_Info code_info = Attribute_Info.code(
            UTF8_CODE_CONSTANT_POOL_INFO_INDEX,
                max_stack,
                max_locals,
            bytecode(code),
            new Exception_Table[0],
            new Attribute_Info[0]
        );
        Method method = new Method(
                Method.FLAG_ACC_PUBLIC | Method.FLAG_ACC_STATIC,
                name_index,
                typeDescriptorIndex,
                1,
                new Attribute_Info[]{ code_info }
        );
        methods.add(method);
        funcDecls.pop();
        current_stack = 0;
        max_stack = 0;
        max_locals = 0;
    }

    @Override
    public void visit(ReturnExpr returnExpr) {
        todo();
    }

    @Override
    public void visit(Print print) {
        //make intrinsics more general?
        int system_out_field_ref = fieldRef(SYSTEM_OUT_FIELD_REF);
        byte[] sout_field_ref_index_bytes = ByteUtils.to_bytes_big((short) system_out_field_ref);
        add_instruction(getstatic(sout_field_ref_index_bytes[0], sout_field_ref_index_bytes[1]));
        int method_ref = -1;
        if (print.arg() instanceof Just<Expr> jExpr) {
            Expr expr = jExpr.elem();
            method_ref = switch (expr.type()) {
                case StringType  s -> methodRef(PRINT_LN_STR_METHOD_REF);
                case IntegerType i -> methodRef(PRINT_LN_INT_METHOD_REF);
                default -> todo("other types for println (got '%s')".formatted(expr.type()));
             };
             expr.visit(this); // will put the instruction that puts the value on the stack
        } else {
            // todo: replace by call to println() (na args), for now add empty string ref and print that
            method_ref = methodRef(PRINT_LN_VOID_METHOD_REF);
        }
        byte[] print_ln_method_ref_bytes = ByteUtils.to_bytes_big((short) method_ref);
        add_instruction(invokevirtual(print_ln_method_ref_bytes[0], print_ln_method_ref_bytes[1], (short) -1));
    }

    @Override
    public void visit(StringLitteral stringLitteral) {
        byte index = (byte) addStringToConstantPool(stringLitteral.value());
        add_instruction(ldc(index));
        max_stack++;
    }

    @Override
    public void visit(IntegerLitteral integerLitteral) {
        if (integerLitteral.value() < Byte.MAX_VALUE) {
            // load imemdiate
            return;
        }
        // add to constant pool and load it

    }

    private short addUtf8ToConstantPool(String typeDescr) {
        constantPoolInfos.add(utf8_info(typeDescr));
        return (short) constantPoolInfos.size();
    }

    private int addStringToConstantPool(String str) {
        // todo check if constant does not already exists
        short utf8Index = addUtf8ToConstantPool(str);
        constantPoolInfos.add(string_info(utf8Index));
        return constantPoolInfos.size();
    }

    private record FieldRef(String clz, String field, String type) {
        public String toString() { return clz + "." + field + ":" + type; }
    }

    private int fieldRef(FieldRef fieldRef) {
        if (fieldRefs.containsKey(fieldRef)) {
            return fieldRefs.get(fieldRef);
        }
        int ref_class = addUtf8ToConstantPool(fieldRef.clz());
        int ref_field = addUtf8ToConstantPool(fieldRef.field);
        int ref_type = addUtf8ToConstantPool(fieldRef.type);
        constantPoolInfos.add(class_info((short) ref_class));
        int ref_class_info = constantPoolInfos.size();
        constantPoolInfos.add(nameAndType_info((short) ref_field, (short) ref_type));
        int ref_name_and_type = constantPoolInfos.size();
        constantPoolInfos.add(fieldRef_info((short) ref_class_info, (short) ref_name_and_type));

        fieldRefs.put(fieldRef, constantPoolInfos.size());
        return constantPoolInfos.size();
    }

    private record MethodRef(String clz, String name, String type) {
        public String toString() { return "%s.%s:%s".formatted(clz, name, type); }
    }

    private int methodRef(MethodRef methodRef) {
        if (methodRefs.containsKey(methodRef)) {
            return methodRefs.get(methodRef);
        }
        int ref_class = addUtf8ToConstantPool(methodRef.clz());
        int ref_name = addUtf8ToConstantPool(methodRef.name());
        int ref_type = addUtf8ToConstantPool(methodRef.type());

        constantPoolInfos.add(class_info((short) ref_class));
        int ref_class_info = constantPoolInfos.size();
        constantPoolInfos.add(nameAndType_info((short) ref_name, (short) ref_type));
        int ref_name_and_type = constantPoolInfos.size();
        constantPoolInfos.add(methodRef_info((short) ref_class_info, (short) ref_name_and_type));

        methodRefs.put(methodRef, constantPoolInfos.size());
        return constantPoolInfos.size();
    }

    public void add_instruction(Instruction instruction) {
        FunctionDecl current = funcDecls.peek();
        List<Instruction> method_instructios = instructions.computeIfAbsent(current, k -> new ArrayList<>());
        method_instructios.add(instruction);
        current_stack += instruction.stack_value_added;
        max_stack = (short) Math.max(max_stack, current_stack);
    }
}