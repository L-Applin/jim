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

public abstract class TypeVisitor implements AstVisitor {

    @Override
    public void visit(Ast ast) {

    }

    @Override
    public void visit(CodeBlock codeBlock) {

    }

    @Override
    public void visit(TypeDecl typeDecl) {

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(FunctionDecl funDecl) {

    }

    @Override
    public void visit(VarAssign varAssign) {

    }

    @Override
    public void visit(IntegerLitteral integerLitteral) {

    }

    @Override
    public void visit(FloatLitteral floatLitteral) {

    }

    @Override
    public void visit(StringLitteral stringLitteral) {

    }

    @Override
    public void visit(ArrayLitteral arrayLitteral) {

    }

    @Override
    public void visit(Unop unop) {

    }

    @Override
    public void visit(Binop binop) {

    }

    @Override
    public void visit(PExpr pExpr) {

    }

    @Override
    public void visit(Ref ref) {

    }

    @Override
    public void visit(DeRef deRef) {

    }

    @Override
    public void visit(FunctionCall functionCall) {

    }

    @Override
    public void visit(ReturnExpr returnExpr) {

    }

    @Override
    public void visit(ImportStmt importStmt) {

    }

    @Override
    public void visit(IfStmt ifStmt) {

    }

    @Override
    public void visit(ForStmt forStmt) {

    }

    @Override
    public void visit(WhileStmt whileStmt) {

    }

    @Override
    public void visit(Print print) {

    }
}
