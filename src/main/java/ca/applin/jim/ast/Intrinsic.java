package ca.applin.jim.ast;

import static ca.applin.jib.utils.Utils.todo;

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
            if (args.size() != 1) {
                todo("report error intrinsic PRINT requires 1 arguments but got " + args.size());
            }
            return new Print(args.get(0));
        }
        throw new IllegalArgumentException("Unknown intrinsic '" + atom.value() + "'");
    }

    record Print(
        Expr arg
    ) implements Expr, Intrinsic {
        public Type type() {
            return new FunctionType(List.of(Type.STRING), Type.VOID);
        }

        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }



}
