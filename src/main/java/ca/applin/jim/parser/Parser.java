package ca.applin.jim.parser;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.expr.Ast;

public interface Parser<AST extends Ast> {
   Maybe<AST> parse();

}
