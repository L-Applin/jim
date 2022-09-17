package ca.applin.jim.ast;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Utils.todo;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Type.FunctionType;
import java.util.List;
import java.util.Set;

public interface Intrinsic extends Ast {

    Atom PRINT = new Atom("print");

    Set<Atom> intrinsics = Set.of(
        PRINT
    );

    static Ast from(Atom atom, List<Expr> args) {
        if (PRINT.equals(atom)) {
            if (args.size() == 0) {
                return new Print(Maybe.nothing());
            }
            if (args.size() == 1) {
                return new Print(just(args.get(0)));
            }
            todo("report error intrinsic PRINT requires 0 or 1 arguments but got " + args.size());
        }
        throw new IllegalArgumentException("Unknown intrinsic '" + atom.value() + "'");
    }

    record Print(
        Maybe<Expr> arg
    ) implements Expr, Intrinsic {
        public Type type() {
            return new FunctionType(List.of(Type.STRING), Type.VOID);
        }

        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }



}
