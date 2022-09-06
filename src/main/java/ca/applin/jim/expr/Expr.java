package ca.applin.jim.expr;

import ca.applin.jim.expr.Type.ArrayType;
import ca.applin.jim.expr.Type.FunctionType;
import java.util.ArrayList;
import java.util.List;

public interface Expr extends Ast {

    Type type();

    default Expr unpack() {
        return this;
    }

    default boolean isLitteral() {
        return false;
    }

    interface Litteral<T> extends Expr {
        T value();
        default boolean isLitteral() {
            return true;
        }
        default Litteral<T> flipSign() { return this; }
    }

    record IntegerLitteral(Integer value) implements Litteral<Integer> {
        public Litteral<Integer> flipSign() {
            return new IntegerLitteral(-value());
        }
        public Type type() { return Type.INTEGER; }
    }

    record FloatLitteral(Double value) implements Litteral<Double> {
        public Litteral<Double> flipSign() {
            return new FloatLitteral(-value());
        }
        public Type type() { return Type.FLOAT; }
    }

    record StringLitteral(String value) implements Litteral<String> {
        public Type type() {
            return Type.STRING;
        }
    }

    ArrayLitteral EMPTY_ARRAY = new ArrayLitteral(new ArrayList<>(), Type.UNKNOWN);
    record ArrayLitteral(List<Expr> value, Type baseType) implements Litteral<List<Expr>> {
        public Type type() {
            return new ArrayType(baseType);
        }
    }

    record Unop(Expr expr, Operator operator, Type type) implements Expr { }

    record Binop(Expr left, Expr right, Operator op) implements Expr {
        public Type type() {
            return left().equals(right()) ? left().type() : Type.UNKNOWN;
        }

        public String toString() {
            return "Binop[%s, left=%s, right=%s]"
                    .formatted(op().toString(), left(), right());
        }
    }

    record PExpr(Expr inner) implements Expr {
        public Type type() { return inner().type(); }
        public Expr unpack() { return inner(); }
    }

    record Ref(Atom ref, Type type) implements Expr {
        public Ref(Atom ref) {
            this(ref, Type.UNKNOWN);
        }

        @Override
        public String toString() {
            return "Ref['%s']".formatted(ref.value());
        }
    }

    record DeRef(Expr left, Atom rigth, Type type) implements Expr {
        public DeRef(Expr left, Atom right) {
            this(left, right, Type.UNKNOWN);
        }

        @Override
        public String toString() {
            return "DeRef[left=%s, rigth=%s".formatted(left().toString(), rigth().toString());
        }
    }

    record FunctionCall(Atom ref, List<Expr> args) implements Expr {
        public Type type() {
            return new FunctionType(args().stream().map(Expr::type).toList(), Type.UNKNOWN);
        }
    }

    record ReturnExpr(Expr expr) implements Expr {
        public Type type() { return expr.type(); }
    }

}