package ca.applin.jim.interp;

import ca.applin.jim.ast.Ast;
import ca.applin.jim.ast.Ast.CodeBlock;
import ca.applin.jim.ast.AstVisitor;
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
import ca.applin.jim.ast.Stmt.ForStmt;
import ca.applin.jim.ast.Stmt.IfStmt;
import ca.applin.jim.ast.Stmt.ImportStmt;
import ca.applin.jim.ast.Stmt.WhileStmt;
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
import ca.applin.jim.ast.Intrinsic.Print;
import java.util.Set;

public class JimInterpreter {

    static class Context {
        private Set<ImportStmt> imports;
    }

    private Context context;
    private Ast ast;
    private AstVisitor astVisitor = new SystemOutVisitor();

    public void interp(Ast ast) {
        ast.visit(astVisitor);
    }

    static class SystemOutVisitor implements AstVisitor {

        @Override
        public void visit(Ast ast) {
            System.out.println("visit AST: " + ast.toString());
        }

        @Override
        public void visit(CodeBlock codeBlock) {
            System.out.println("visit CODE BLOCK: " + codeBlock.toString());
            codeBlock.elem.visit(this);
            if (codeBlock.next != null) {
                codeBlock.next.visit(this);
            }
        }

        @Override
        public void visit(TypeDecl typeDecl) {
            System.out.println("visit TYPE DECL: " + typeDecl.toString());
        }

        @Override
        public void visit(VarDecl varDecl) {
            System.out.println("visit VAR DECL: " + varDecl.toString());
            varDecl.typeDecl().ifPresent(t -> t.visit(this));
            varDecl.expr().visit(this);
        }

        @Override
        public void visit(FunctionDecl funDecl) {
            System.out.println("visit FUNC DECL: " + funDecl.toString());
            funDecl.body().visit(this);
        }

        @Override
        public void visit(VarAssign varAssign) {
            System.out.println("visit VAR ASSING: " + varAssign.toString());
            varAssign.expr().visit(this);
        }

        @Override
        public void visit(IntegerLitteral integerLitteral) {
            System.out.println("visit INT LITTERAL: " + integerLitteral.toString());
        }

        @Override
        public void visit(FloatLitteral floatLitteral) {
            System.out.println("visit FLOAT LITTERAL: " + floatLitteral.toString());
        }

        @Override
        public void visit(StringLitteral stringLitteral) {
            System.out.println("visit STRING LITTERAL: " + stringLitteral.toString());
        }

        @Override
        public void visit(ArrayLitteral arrayLitteral) {
            System.out.println("visit ARRAY LITTERAL: " + arrayLitteral.toString());
            arrayLitteral.value().forEach(expr -> expr.visit(this));
        }

        @Override
        public void visit(Unop unop) {
            System.out.println("visit UNOP: " + unop.toString());
            unop.expr().visit(this);
        }

        @Override
        public void visit(Binop binop) {
            System.out.println("visit BINOP: " + binop.toString());
            binop.left().visit(this);
            binop.right().visit(this);
        }

        @Override
        public void visit(PExpr pExpr) {
            System.out.println("visit PEXPR: " + pExpr.toString());
            pExpr.inner().visit(this);
        }

        @Override
        public void visit(Ref ref) {
            System.out.println("visit REF: " + ref.toString());
        }

        @Override
        public void visit(DeRef deRef) {
            System.out.println("visit DEREF: " + deRef.toString());
            deRef.left().visit(this);
        }

        @Override
        public void visit(FunctionCall functionCall) {
            System.out.println("visit FUNC CALL: " + functionCall.toString());
            functionCall.args().forEach(arg -> arg.visit(this));
        }

        @Override
        public void visit(ReturnExpr returnExpr) {
            System.out.println("visit RETURN EXPR: " + returnExpr.toString());
            returnExpr.expr().visit(this);
        }

        @Override
        public void visit(ImportStmt importStmt) {
            System.out.println("visit IMPORT STMT: " + importStmt.toString());
        }

        @Override
        public void visit(IfStmt ifStmt) {
            System.out.println("visit IF STMT: " + ifStmt.toString());
            ifStmt.cond().visit(this);
            ifStmt.thenBlock().visit(this);
            ifStmt.elseBlock().ifPresent(eles -> eles.visit(this));
        }

        @Override
        public void visit(ForStmt forStmt) {
            System.out.println("visit FOR STMT: " + forStmt.toString());
            forStmt.iterator().visit(this);
            forStmt.codeBlock().visit(this);
        }

        @Override
        public void visit(WhileStmt whileStmt) {
            System.out.println("visit WHILE STMT: " + whileStmt.toString());
            whileStmt.cond().visit(this);
            whileStmt.codeBlock().visit(this);
        }

        @Override
        public void visit(SimpleType simpleType) {
            System.out.println("visit SIMPLE TYPE: " + simpleType.toString());
        }

        @Override
        public void visit(Primitive primitive) {
            System.out.println("visit PRIMITIVE: " + primitive.toString());
        }

        @Override
        public void visit(GenericType genericType) {
            System.out.println("visit GENERIC: " + genericType.toString());
            genericType.generics().forEach(gen -> gen.visit(this));
        }

        @Override
        public void visit(SumType sumType) {
            System.out.println("visit SUM TYPE: " + sumType.toString());
            sumType.constructors().forEach(
                (k, v) -> v.forEach(t -> t.visit(this))
            );
        }

        @Override
        public void visit(StructType structType) {
            System.out.println("visit STRUCT TYPE: " + structType.toString());
            structType.elems().forEach(
                (k, v) -> v.visit(this)
            );
        }

        @Override
        public void visit(StructElem structElem) {
            System.out.println("visit STRUCT ELEM: " + structElem.toString());
            structElem.type().visit(this);
        }

        @Override
        public void visit(TupleType tupleType) {
            System.out.println("visit TUPLE TYPE: " + tupleType.toString());
            tupleType.types().forEach(t -> t.visit(this));
        }

        @Override
        public void visit(ArrayType arrayType) {
            System.out.println("visit ARRAY TYPE: " + arrayType.toString());
        }

        @Override
        public void visit(FunctionType functionType) {
            System.out.println("visit TUPLE TYPE: " + functionType.toString());
            functionType.args().forEach(t -> t.visit(this));
            functionType.returnType().visit(this);
        }

        @Override
        public void visit(PType pType) {
            System.out.println("visit TUPLE TYPE: " + pType.toString());
            pType.inner().visit(this);
        }

        @Override
        public void visit(Print print) {
            System.out.println("visit PRINT intrinsic: " + print.toString());
            print.arg().visit(this);
        }
    }

}
