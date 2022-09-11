package ca.applin.jim.bytecode;


import static ca.applin.jib.utils.Utils.todo;

import ca.applin.jim.ast.Ast.CodeBlock;
import ca.applin.jim.ast.AstVisitorAdapter;
import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.ArrayType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.GenericType;
import ca.applin.jim.ast.Type.PType;
import ca.applin.jim.ast.Type.Primitive;
import ca.applin.jim.ast.Type.SimpleType;
import ca.applin.jim.ast.Type.StructElem;
import ca.applin.jim.ast.Type.StructType;
import ca.applin.jim.ast.Type.SumType;
import ca.applin.jim.ast.Type.TupleType;
import ca.applin.jim.ast.TypeVisitor;

public class BytecodeGenerator extends TypeVisitor {

    @Override
    public void visit(CodeBlock codeBlock) {
        codeBlock.elem.visit(this);
        if (codeBlock.next != null) {
            codeBlock.next.visit(this);
        }
    }

    @Override
    public void visit(SimpleType simpleType) {

    }

    @Override
    public void visit(Primitive primitive) {

    }

    @Override
    public void visit(GenericType genericType) {

    }

    @Override
    public void visit(SumType sumType) {

    }

    @Override
    public void visit(StructType structType) {

    }

    @Override
    public void visit(StructElem structElem) {

    }

    @Override
    public void visit(TupleType tupleType) {

    }

    @Override
    public void visit(ArrayType arrayType) {

    }

    @Override
    public void visit(FunctionType functionType) {

    }

    @Override
    public void visit(PType pType) {

    }

    public static String typeToDescriptor(Type type) {
        if (type instanceof Primitive p) {
            return switch (p.name()) {
                case "String" -> "Ljava/lang/String";
                case "Int" -> "I";
                case "Float" -> "D";
                default -> todo("report unknown primitive '" + p.name() + "'");
            };
        }
        return todo();
    }
}
