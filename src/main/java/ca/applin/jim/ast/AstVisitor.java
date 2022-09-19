package ca.applin.jim.ast;

import static ca.applin.jim.ast.Ast.*;
import static ca.applin.jim.ast.Decl.*;
import static ca.applin.jim.ast.Expr.*;
import static ca.applin.jim.ast.Stmt.*;

import ca.applin.jim.ast.Type.ArrayType;
import ca.applin.jim.ast.Type.DoubleType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.GenericType;
import ca.applin.jim.ast.Type.IntegerType;
import ca.applin.jim.ast.Type.PType;
import ca.applin.jim.ast.Type.Primitive;
import ca.applin.jim.ast.Type.SimpleType;
import ca.applin.jim.ast.Type.StringType;
import ca.applin.jim.ast.Type.StructElem;
import ca.applin.jim.ast.Type.StructType;
import ca.applin.jim.ast.Type.SumType;
import ca.applin.jim.ast.Type.TupleType;
import ca.applin.jim.ast.Intrinsic.Print;

public interface AstVisitor {

    interface Visited {
        void visit(AstVisitor astVisitor);
    }

    void visit(Ast ast);
    void visit(CodeBlock codeBlock);

    void visit(TypeDecl typeDecl);
    void visit(VarDecl varDecl);
    void visit(FunctionDecl funDecl);
    void visit(VarAssign varAssign);

    void visit(IntegerLitteral integerLitteral);
    void visit(DoubleLitteral doubleLitteral);
    void visit(StringLitteral stringLitteral);
    void visit(ArrayLitteral arrayLitteral);
    void visit(Unop unop);
    void visit(Binop binop);
    void visit(PExpr pExpr);
    void visit(VarRef varRef);
    void visit(DeRef deRef);
    void visit(FunctionCall functionCall);
    void visit(ReturnExpr returnExpr);

    void visit(ImportStmt importStmt);
    void visit(IfStmt ifStmt);
    void visit(ForStmt forStmt);
    void visit(WhileStmt whileStmt);

    void visit(SimpleType simpleType);
    void visit(Primitive primitive);
    void visit(StringType stringType);
    void visit(IntegerType integerType);
    void visit(DoubleType doubleType);
    void visit(GenericType genericType);
    void visit(SumType sumType);
    void visit(StructType structType);
    void visit(StructElem structElem);
    void visit(TupleType tupleType);
    void visit(ArrayType arrayType);
    void visit(FunctionType functionType);
    void visit(PType pType);

    void visit(Print print);
}
