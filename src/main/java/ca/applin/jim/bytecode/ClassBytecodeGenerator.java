package ca.applin.jim.bytecode;


import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.bytecode.Attribute_Info.code;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MAJOR_VERION;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MINOR_VERION;
import static ca.applin.jim.bytecode.Constant_Pool_Info.class_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.double_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.fieldRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.integer_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.methodRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.nameAndType_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.string_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.utf8_info;
import static ca.applin.jim.bytecode.Instruction.aload_0;
import static ca.applin.jim.bytecode.Instruction.bipush;
import static ca.applin.jim.bytecode.Instruction.bytecode;
import static ca.applin.jim.bytecode.Instruction.dadd;
import static ca.applin.jim.bytecode.Instruction.ddiv;
import static ca.applin.jim.bytecode.Instruction.dmul;
import static ca.applin.jim.bytecode.Instruction.dsub;
import static ca.applin.jim.bytecode.Instruction.getstatic;
import static ca.applin.jim.bytecode.Instruction.i2d;
import static ca.applin.jim.bytecode.Instruction.iadd;
import static ca.applin.jim.bytecode.Instruction.idiv;
import static ca.applin.jim.bytecode.Instruction.imul;
import static ca.applin.jim.bytecode.Instruction.invokespecial;
import static ca.applin.jim.bytecode.Instruction.invokevirtual;
import static ca.applin.jim.bytecode.Instruction.isub;
import static ca.applin.jim.bytecode.Instruction.ldc;
import static ca.applin.jim.bytecode.Instruction.ldc2_w;
import static ca.applin.jim.bytecode.Instruction.return_void;

import ca.applin.jib.utils.Just;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.AstVisitorAdapter;
import ca.applin.jim.ast.Decl.FunctionDecl;
import ca.applin.jim.ast.Expr;
import ca.applin.jim.ast.Expr.Binop;
import ca.applin.jim.ast.Expr.DoubleLitteral;
import ca.applin.jim.ast.Expr.IntegerLitteral;
import ca.applin.jim.ast.Expr.ReturnExpr;
import ca.applin.jim.ast.Expr.StringLitteral;
import ca.applin.jim.ast.Intrinsic.Print;
import ca.applin.jim.ast.Operator;
import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.DoubleType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.IntegerType;
import ca.applin.jim.ast.Type.StringType;
import ca.applin.jim.ast.Type.Unknown;
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

    public static final MethodRef PRINT_LN_DOUBLE_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "(D)V"
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

    private short max_stack = 0, current_stack = 0, max_locals = 0, current_locals = 0, index_offset = 0;

    private final TypeDescriptorMapper typeDescriptorMapper = new TypeDescriptorMapper();
    private final List<Constant_Pool_Info> constantPoolInfos = new ArrayList<>() {{
        addAll(SUPER_OBJECT);
        add(UTF8_CODE_CONSTANT_POOL_INFO); // index #7
    }};

    private List<Method> methods = new ArrayList<>();

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
        short this_class = current_index();
        Class_File test_class = new Class_File(
                JAVA_11_CLASS_MINOR_VERION,
                JAVA_11_CLASS_MAJOR_VERION,
                (short) (constantPoolInfos.size() + 1 + index_offset), // stupid constant pool...
                constantPoolInfos.toArray(new Constant_Pool_Info[0]),
                (short) (Flag.ACC_PUBLIC | Flag.ACC_SUPER),
                this_class,
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
        String type_descr = typeDescriptorMapper.typeToDescriptor(type);
        int type_descriptor_index = addUtf8ToConstantPool(type_descr);
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
                type_descriptor_index,
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
        int method_ref;
        if (print.arg() instanceof Just<Expr> jExpr) {
            Expr expr = jExpr.elem();
            method_ref = switch (expr.type()) {
                case StringType  s -> methodRef(PRINT_LN_STR_METHOD_REF);
                case IntegerType i -> methodRef(PRINT_LN_INT_METHOD_REF);
                case DoubleType  d -> methodRef(PRINT_LN_DOUBLE_METHOD_REF);
                default -> todo("other types for println (got '%s')".formatted(expr.type()));
             };
             expr.visit(this); // will put the instruction that puts the value on the stack
        } else {
            // todo: replace by call to println() (na args), for now add empty string ref and print that
            method_ref = methodRef(PRINT_LN_VOID_METHOD_REF);
        }
        byte[] print_ln_method_ref_bytes = ByteUtils.to_bytes_big((short) method_ref);
        add_instruction(invokevirtual(print_ln_method_ref_bytes[0], print_ln_method_ref_bytes[1], (short) 2));
    }

    @Override
    public void visit(StringLitteral stringLitteral) {
        byte index = (byte) addStringToConstantPool(stringLitteral.value());
        add_instruction(ldc(index));
    }

    @Override
    public void visit(IntegerLitteral integerLitteral) {
        if (integerLitteral.value() <= Byte.MAX_VALUE && integerLitteral.value() >= Byte.MIN_VALUE) {
            add_instruction(bipush(integerLitteral.value().byteValue()));
            return;
        }
        // add to constant pool and load it
        byte ref = (byte) addIntToConstantPool(integerLitteral.value());
        add_instruction(ldc(ref));
    }

    @Override
    public void visit(DoubleLitteral doubleLitteral) {
        short index = addDoubleToConstantPool(doubleLitteral.value());
        byte[] index_bytes = ByteUtils.to_bytes_big(index);
        add_instruction(ldc2_w(index_bytes[0], index_bytes[1]));
    }

    @Override
    public void visit(Binop binop) {

        binop.left().visit(this);
        final Type binopType = binop.type();
        final Type leftType = binop.left().type();
        final Type rightType = binop.right().type();
        if (binopType instanceof DoubleType) {
            if (leftType instanceof IntegerType) {
                add_instruction(i2d());
            }
        }

        binop.right().visit(this);
        if (binopType instanceof DoubleType) {
            if (rightType instanceof IntegerType) {
                add_instruction(i2d());
            }
        }

        switch (binop.op()) {

            case PLUS -> {
                switch (binopType) {
                    case IntegerType __ -> add_instruction(iadd());
                    case DoubleType __ -> add_instruction(dadd());
                    case Unknown __ -> todo("report unknown type for " + binop);
                    default -> todo("Plus for type " + binop.type());
                }
            }

            case MINUS -> {
                switch (binopType) {
                    case IntegerType __ -> add_instruction(isub());
                    case DoubleType __ -> add_instruction(dsub());
                    case Unknown __ -> todo("report unknown type for " + binop);
                    default -> todo("Minus for type " + binop.type());
                }
            }

            case TIMES -> {
                switch (binopType) {
                    case IntegerType __ -> add_instruction(imul());
                    case DoubleType __-> add_instruction(dmul());
                    case Unknown __ -> todo("report unknown type for " + binop);
                    default -> todo("Times for type " + binop.type());
                }
            }

        case DIV -> {
                switch (binop.type()) {
                    case IntegerType __ -> add_instruction(idiv());
                    case DoubleType __-> add_instruction(ddiv());
                    case Unknown __ -> todo("report unknown type for " + binop);
                    default -> todo("Div for type " + binop.type());
                }
            }

            case MOD -> {
            }
            case LOGICAL_OR -> {
            }
            case LOGICAL_AND -> {
            }
            case LOGICAL_XOR -> {
            }
            case EQ -> {
            }
            case NEQ -> {
            }
            case BIT_SHIFT_LEFT -> {
            }
            case BIT_SHIFT_RIGHT -> {
            }
            case UNARY_PLUS -> {
            }
            case UNARY_MINUS -> {
            }
            case ACCESSOR -> {
            }
        }
    }

    private short addUtf8ToConstantPool(String typeDescr) {
        constantPoolInfos.add(utf8_info(typeDescr));
        return current_index();
    }

    private short addStringToConstantPool(String str) {
        // todo check if constant does not already exists
        short utf8Index = addUtf8ToConstantPool(str);
        constantPoolInfos.add(string_info(utf8Index));
        return current_index();
    }

    private short addIntToConstantPool(int value) {
        constantPoolInfos.add(integer_info(value));
        return current_index();
    }

    private short addDoubleToConstantPool(double value) {
        long bytes = Double.doubleToRawLongBits(value);
        int low_bytes  = (int) bytes;
        int high_bytes = (int) ((bytes & 0x00000000FFFFFFFF) >> 32);
        constantPoolInfos.add(double_info(high_bytes, low_bytes));
        short index = current_index();
        index_offset++;
        return index;
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
        int ref_class_info = current_index();
        constantPoolInfos.add(nameAndType_info((short) ref_field, (short) ref_type));
        int ref_name_and_type = current_index();
        constantPoolInfos.add(fieldRef_info((short) ref_class_info, (short) ref_name_and_type));

        fieldRefs.put(fieldRef, (int) current_index());
        return current_index();
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
        short ref_class_info = current_index() ;
        constantPoolInfos.add(nameAndType_info((short) ref_name, (short) ref_type));
        short ref_name_and_type = current_index() ;
        constantPoolInfos.add(methodRef_info(ref_class_info, ref_name_and_type));

        methodRefs.put(methodRef, (int) current_index());
        return current_index() ;
    }

    public void add_instruction(Instruction instruction) {
        FunctionDecl current = funcDecls.peek();
        List<Instruction> method_instructios = instructions.computeIfAbsent(current, k -> new ArrayList<>());
        method_instructios.add(instruction);
        current_stack += instruction.stack_value_added;
        max_stack = (short) Math.max(max_stack, current_stack);
    }

    private short current_index() {
        return (short) (constantPoolInfos.size() + index_offset);
    }

}