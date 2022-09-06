package ca.applin.jim.expr;

import ca.applin.jim.lexer.LexerToken.Location;

public interface Stmt extends Ast {

    record ImportStmt(Location location, String stmt, boolean isStatic) implements Stmt { }

}
