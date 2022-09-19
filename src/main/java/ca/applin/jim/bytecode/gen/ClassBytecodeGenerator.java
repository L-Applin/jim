package ca.applin.jim.bytecode.gen;


import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.bytecode.Attribute_Info.code;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MAJOR_VERION;
import static ca.applin.jim.bytecode.Class_File.JAVA_11_CLASS_MINOR_VERION;
import static ca.applin.jim.bytecode.Constant_Pool_Info.class_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.methodRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.nameAndType_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.utf8_info;
import static ca.applin.jim.bytecode.Instruction.aload_0;
import static ca.applin.jim.bytecode.Instruction.bipush;
import static ca.applin.jim.bytecode.Instruction.bytecode;
import static ca.applin.jim.bytecode.Instruction.dadd;
import static ca.applin.jim.bytecode.Instruction.ddiv;
import static ca.applin.jim.bytecode.Instruction.dload;
import static ca.applin.jim.bytecode.Instruction.dmul;
import static ca.applin.jim.bytecode.Instruction.dstore;
import static ca.applin.jim.bytecode.Instruction.dsub;
import static ca.applin.jim.bytecode.Instruction.getstatic;
import static ca.applin.jim.bytecode.Instruction.i2d;
import static ca.applin.jim.bytecode.Instruction.iadd;
import static ca.applin.jim.bytecode.Instruction.idiv;
import static ca.applin.jim.bytecode.Instruction.iload;
import static ca.applin.jim.bytecode.Instruction.imul;
import static ca.applin.jim.bytecode.Instruction.invokespecial;
import static ca.applin.jim.bytecode.Instruction.invokevirtual;
import static ca.applin.jim.bytecode.Instruction.istore;
import static ca.applin.jim.bytecode.Instruction.isub;
import static ca.applin.jim.bytecode.Instruction.ldc;
import static ca.applin.jim.bytecode.Instruction.ldc2_w;
import static ca.applin.jim.bytecode.Instruction.return_void;
import static ca.applin.jim.bytecode.gen.ConstantPool.PRINT_LN_DOUBLE_METHOD_REF;
import static ca.applin.jim.bytecode.gen.ConstantPool.PRINT_LN_INT_METHOD_REF;
import static ca.applin.jim.bytecode.gen.ConstantPool.PRINT_LN_STR_METHOD_REF;
import static ca.applin.jim.bytecode.gen.ConstantPool.PRINT_LN_VOID_METHOD_REF;
import static ca.applin.jim.bytecode.gen.ConstantPool.SYSTEM_OUT_FIELD_REF;

import ca.applin.jib.utils.Just;
import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.Ast.Atom;
import ca.applin.jim.ast.AstVisitorAdapter;
import ca.applin.jim.ast.Decl.FunctionDecl;
import ca.applin.jim.ast.Decl.VarDecl;
import ca.applin.jim.ast.Expr;
import ca.applin.jim.ast.Expr.Binop;
import ca.applin.jim.ast.Expr.DoubleLitteral;
import ca.applin.jim.ast.Expr.IntegerLitteral;
import ca.applin.jim.ast.Expr.VarRef;
import ca.applin.jim.ast.Expr.ReturnExpr;
import ca.applin.jim.ast.Expr.StringLitteral;
import ca.applin.jim.ast.Intrinsic.Print;
import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.DoubleType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.IntegerType;
import ca.applin.jim.ast.Type.StringType;
import ca.applin.jim.ast.Type.Unknown;
import ca.applin.jim.ast.Type.Void;
import ca.applin.jim.bytecode.Attribute_Info;
import ca.applin.jim.bytecode.Attribute_Info.Exception_Table;
import ca.applin.jim.bytecode.ByteUtils;
import ca.applin.jim.bytecode.Class_File;
import ca.applin.jim.bytecode.Class_File.Flag;
import ca.applin.jim.bytecode.Constant_Pool_Info;
import ca.applin.jim.bytecode.Field;
import ca.applin.jim.bytecode.Instruction;
import ca.applin.jim.bytecode.Interface;
import ca.applin.jim.bytecode.Method;
import ca.applin.jim.bytecode.TypeDescriptorMapper;
import ca.applin.jim.compiler.CompilerException;
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

    private String _package, file;

    public ClassBytecodeGenerator(String _package, String file) {
        this._package = _package;
        this.file = file;
    }

   private final Map<Atom, Short> localVars = new HashMap<>();

    // HACK!!! replace by actuall type check
    private final Map<Atom, Type> types = new HashMap<>();

    private final Map<FunctionDecl, List<Instruction>> instructions = new HashMap<>();
    private final Stack<FunctionDecl> funcDecls = new Stack<>();

    private short max_stack = 0,
                  current_stack = 0,
                  max_locals = 0,
                  local_var_offset = 0;

    private final TypeDescriptorMapper typeDescriptorMapper = new TypeDescriptorMapper();
    private final ConstantPool constantPool = new ConstantPool();

    private final List<Method> methods = new ArrayList<>();

    public byte[] generate(Ast ast) {
        init_class_bytecode();

        // generate bytecode for the whole file
        ast.visit(this);

        short name_index = constantPool.addUtf8(_package + "/" + file);
        short this_class = constantPool.add(class_info(name_index));

        Class_File test_class = new Class_File(
                JAVA_11_CLASS_MINOR_VERION,
                JAVA_11_CLASS_MAJOR_VERION,
                constantPool.size(), // stupid constant pool...
                constantPool.infos(),
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

    // adds super ref (object for now...) and <init> method
    private void init_class_bytecode() {
        byte[] init_code = bytecode(
                aload_0(), // load `this`
                invokespecial((byte) 0x00, (byte) 0x01, (short) 0), // call Object.<init>
                return_void()
        );
        Attribute_Info[] init_code_attr = new Attribute_Info[] {
                code(constantPool.code_info(), (short) 1, (short) 1, init_code, new Exception_Table[0], new Attribute_Info[0])
        };
        Method init_method = new Method(Method.FLAG_ACC_PUBLIC, 5, 6, init_code_attr.length, init_code_attr);
        methods.add(init_method);
    }

    @Override
    public void visit(FunctionDecl funDecl) {

        initFuncDecl(funDecl);

        Type type = funDecl.type()
                .orElseThrow(new ParserException("Unknown type: " + funDecl.type()));
        String type_descr = typeDescriptorMapper.typeToDescriptor(type);
        short type_descriptor_index = constantPool.addUtf8(type_descr);
        short name_index = constantPool.addUtf8(funDecl.name().value());
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
                constantPool.code_info(),
                max_stack,
                max_locals,
            bytecode(code),
            new Exception_Table[0],
            new Attribute_Info[0]
        );
        Method method = new Method(
                (short) (Method.FLAG_ACC_PUBLIC | Method.FLAG_ACC_STATIC),
                name_index,
                type_descriptor_index,
                (short) 1,
                new Attribute_Info[]{ code_info }
        );
        methods.add(method);
        cleanUpFuncDecl();
    }

    private void initFuncDecl(FunctionDecl funDecl) {
        max_stack = 0;
        max_locals = (short) funDecl.args().size();
        funcDecls.push(funDecl);
        instructions.put(funDecl, new ArrayList<>());
        int curr_size = localVars.size();
        for (int i = 0; i < funDecl.args().size(); i++) {
            localVars.put(funDecl.args().get(i), (short) (i + curr_size));
        }
    }

    private void cleanUpFuncDecl() {
        funcDecls.pop();
        current_stack = 0;
        max_stack = 0;
        max_locals = 0;
        localVars.clear();
    }

    @Override
    public void visit(ReturnExpr returnExpr) {
        todo();
    }

    @Override
    public void visit(Print print) {
        //make intrinsics more general?
        int system_out_field_ref = constantPool.fieldRef(SYSTEM_OUT_FIELD_REF);
        byte[] sout_field_ref_index_bytes = ByteUtils.to_bytes_big((short) system_out_field_ref);
        add_instruction(getstatic(sout_field_ref_index_bytes[0], sout_field_ref_index_bytes[1]));
        int method_ref;
        if (print.arg() instanceof Just<Expr> jExpr) {
            Expr expr = jExpr.elem();
            method_ref = switch (expr.findType(types)) {
                case StringType  s -> constantPool.methodRef(PRINT_LN_STR_METHOD_REF);
                case IntegerType i -> constantPool.methodRef(PRINT_LN_INT_METHOD_REF);
                case DoubleType  d -> constantPool.methodRef(PRINT_LN_DOUBLE_METHOD_REF);
                case Unknown __    -> todo("Unknown type for expr: " + expr);
                default -> todo("other types for println (got '%s')".formatted(expr.type()));
             };
             expr.visit(this); // will put the instruction that puts the value on the stack
        } else {
            // todo: replace by call to println() (na args), for now add empty string ref and print that
            method_ref = constantPool.methodRef(PRINT_LN_VOID_METHOD_REF);
        }
        byte[] print_ln_method_ref_bytes = ByteUtils.to_bytes_big((short) method_ref);
        add_instruction(invokevirtual(print_ln_method_ref_bytes[0], print_ln_method_ref_bytes[1], (short) 2));
    }

    @Override
    public void visit(StringLitteral stringLitteral) {
        byte index = (byte) constantPool.addString(stringLitteral.value());
        add_instruction(ldc(index));
    }

    @Override
    public void visit(IntegerLitteral integerLitteral) {
        if (integerLitteral.value() <= Byte.MAX_VALUE && integerLitteral.value() >= Byte.MIN_VALUE) {
            add_instruction(bipush(integerLitteral.value().byteValue()));
            return;
        }
        // add to constant pool and load it
        byte ref = (byte) constantPool.addInt(integerLitteral.value());
        add_instruction(ldc(ref));
    }

    @Override
    public void visit(DoubleLitteral doubleLitteral) {
        short index = constantPool.addDouble(doubleLitteral.value());
        byte[] index_bytes = ByteUtils.to_bytes_big(index);
        add_instruction(ldc2_w(index_bytes[0], index_bytes[1]));
    }

    @Override
    public void visit(Binop binop) {
        final Type binopType = binop.findType(types);
        final Type leftType = binop.left().findType(types);
        final Type rightType = binop.right().findType(types);

        binop.left().visit(this);
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
                switch (binopType) {
                    case IntegerType __ -> add_instruction(idiv());
                    case DoubleType __-> add_instruction(ddiv());
                    case Unknown __ -> todo("report unknown type for " + binop);
                    default -> todo("Div for type " + binop.type());
                }
            }

        case MOD,
            LOGICAL_OR,
            LOGICAL_AND,
            LOGICAL_XOR,
            EQ,
            NEQ,
            BIT_SHIFT_LEFT,
            BIT_SHIFT_RIGHT,
            UNARY_PLUS,
            UNARY_MINUS,
            ACCESSOR
                -> todo();
        }
    }

    @Override
    public void visit(VarDecl varDecl) {
        varDecl.expr().visit(this); // put result of init expr of the operand stack
        short index = (short) (localVars.size() + local_var_offset);
        localVars.put(varDecl.name(), index);
        types.put(varDecl.name(), varDecl.expr().findType(types));
        if (index > Byte.MAX_VALUE) {
            todo("!!! MAX VAR SIZE REACHED WITHOUT WIDE !!!");
        }
        switch (varDecl.expr().findType(types)) {
            case IntegerType __ -> add_instruction(istore((byte) index));
            case DoubleType __  -> {
                add_instruction(dstore((byte) index));
                local_var_offset += 1;
                max_locals++;
            }
            case Unknown __      -> todo("report unknown type");
            default -> todo("push var of type '" + varDecl.expr().type() + "' not yet implemented.");
        }
        localVars.put(varDecl.name(), index);
        max_locals++;
    }

    @Override
    public void visit(VarRef varRef) {
        if (!localVars.containsKey(varRef.ref())) {
            throw new CompilerException("Variable not found: " + varRef);
        }
        short index = localVars.get(varRef.ref());
        Type type = varRef.type();
        if (type instanceof Unknown) {
            type = types.get(varRef.ref());
            if (type == null) {
                throw new CompilerException("Could not resolve type of var: " + varRef);
            }
        }
        switch (type) {
            case IntegerType __ -> add_instruction(iload((byte) index));
            case DoubleType __  -> add_instruction(dload((byte) index));
            case Unknown __      -> todo("report unknown type: " + varRef);
            default -> todo("load var for " + varRef);
        }
    }


    public void add_instruction(Instruction instruction) {
        List<Instruction> method_instructios = instructions.computeIfAbsent(funcDecls.peek(), k -> new ArrayList<>());
        method_instructios.add(instruction);
        current_stack += instruction.stack_value_added;
        max_stack = (short) Math.max(max_stack, current_stack);
    }

}