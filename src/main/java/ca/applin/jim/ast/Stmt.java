package ca.applin.jim.ast;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.lexer.LexerToken.Location;

public interface Stmt extends Ast {

    Location location();

    record ImportStmt(
            Location location,
            String stmt,
            boolean isStatic
    ) implements Stmt {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record IfStmt(
        Location location,
        Expr cond,
        Ast thenBlock,
        Maybe<Ast> elseBlock
    ) implements Stmt {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record ForStmt(
        Location location,
        Expr iterator,
        Ast codeBlock
    ) implements Stmt {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record WhileStmt(
        Location location,
        Expr cond,
        Ast codeBlock
   ) implements Stmt {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }


}
