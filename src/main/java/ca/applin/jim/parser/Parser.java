package ca.applin.jim.parser;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Ast;

public interface Parser<AST extends Ast> {
   Maybe<AST> parse();

}
