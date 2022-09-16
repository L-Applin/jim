package ca.applin.jim.ast;

import ca.applin.jim.ast.Ast.CodeBlock;
import ca.applin.jim.ast.Decl.FunctionDecl;
import ca.applin.jim.ast.Decl.TypeDecl;
import ca.applin.jim.ast.Decl.VarAssign;
import ca.applin.jim.ast.Decl.VarDecl;
import ca.applin.jim.ast.Expr.ArrayLitteral;
import ca.applin.jim.ast.Expr.Binop;
import ca.applin.jim.ast.Expr.DeRef;
import ca.applin.jim.ast.Expr.FloatLitteral;
import ca.applin.jim.ast.Expr.FunctionCall;
import ca.applin.jim.ast.Expr.IntegerLitteral;
import ca.applin.jim.ast.Expr.PExpr;
import ca.applin.jim.ast.Expr.Ref;
import ca.applin.jim.ast.Expr.ReturnExpr;
import ca.applin.jim.ast.Expr.StringLitteral;
import ca.applin.jim.ast.Expr.Unop;
import ca.applin.jim.ast.Intrinsic.Print;
import ca.applin.jim.ast.Stmt.ForStmt;
import ca.applin.jim.ast.Stmt.IfStmt;
import ca.applin.jim.ast.Stmt.ImportStmt;
import ca.applin.jim.ast.Stmt.WhileStmt;
import ca.applin.jim.ast.Type.ArrayType;
import ca.applin.jim.ast.Type.FloatType;
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

public abstract class AstVisitorAdapter implements AstVisitor {

    @Override
    public void visit(Ast ast) {
        // intentionally left blank
    }

    @Override
    public void visit(CodeBlock codeBlock) {
        // default implementation for linked list
        codeBlock.elem.visit(this);
        if (codeBlock.next != null) {
            codeBlock.next.visit(this);
        }
    }

    @Override
    public void visit(TypeDecl typeDecl) {
        // intentionally left blank
    }

    @Override
    public void visit(VarDecl varDecl) {
        // intentionally left blank
    }

    @Override
    public void visit(FunctionDecl funDecl) {
        // intentionally left blank
    }

    @Override
    public void visit(VarAssign varAssign) {
        // intentionally left blank
    }

    @Override
    public void visit(IntegerLitteral integerLitteral) {
        // intentionally left blank
    }

    @Override
    public void visit(FloatLitteral floatLitteral) {
        // intentionally left blank
    }

    @Override
    public void visit(StringLitteral stringLitteral) {
        // intentionally left blank
    }

    @Override
    public void visit(ArrayLitteral arrayLitteral) {
        // intentionally left blank
    }

    @Override
    public void visit(Unop unop) {
        // intentionally left blank
    }

    @Override
    public void visit(Binop binop) {
        // intentionally left blank
    }

    @Override
    public void visit(PExpr pExpr) {
        // default implementaion
        final Expr inner = pExpr.inner();
        if (inner != null) {
            inner.visit(this);
        }
    }

    @Override
    public void visit(Ref ref) {
        // intentionally left blank
    }

    @Override
    public void visit(DeRef deRef) {
        // intentionally left blank
    }

    @Override
    public void visit(FunctionCall functionCall) {
        // intentionally left blank
    }

    @Override
    public void visit(ReturnExpr returnExpr) {
        // intentionally left blank
    }

    @Override
    public void visit(ImportStmt importStmt) {
        // intentionally left blank
    }

    @Override
    public void visit(IfStmt ifStmt) {
        // intentionally left blank
    }

    @Override
    public void visit(ForStmt forStmt) {
        // intentionally left blank
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        // intentionally left blank
    }

    @Override
    public void visit(SimpleType simpleType) {
        // intentionally left blank
    }

    @Override
    public void visit(Primitive primitive) {
        // intentionally left blank
    }

    @Override
    public void visit(GenericType genericType) {
        // intentionally left blank
    }

    @Override
    public void visit(SumType sumType) {
        // intentionally left blank
    }

    @Override
    public void visit(StructType structType) {
        // intentionally left blank
    }

    @Override
    public void visit(StructElem structElem) {
        // intentionally left blank
    }

    @Override
    public void visit(TupleType tupleType) {
        // intentionally left blank
    }

    @Override
    public void visit(ArrayType arrayType) {
        // intentionally left blank
    }

    @Override
    public void visit(FunctionType functionType) {
        // intentionally left blank
    }

    @Override
    public void visit(PType pType) {
        // intentionally left blank
    }

    @Override
    public void visit(Print print) {
        // intentionally left blank
    }

    @Override
    public void visit(StringType stringType) {

    }

    @Override
    public void visit(IntegerType integerType) {

    }

    @Override
    public void visit(FloatType floatType) {

    }
}