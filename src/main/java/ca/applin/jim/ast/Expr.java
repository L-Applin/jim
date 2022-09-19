package ca.applin.jim.ast;

import ca.applin.jim.ast.Type.ArrayType;
import ca.applin.jim.ast.Type.DoubleType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.IntegerType;
import ca.applin.jim.lexer.LexerToken.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Expr extends Ast {

    Type type();

    default Type findType(Map<Atom, Type> context) {
        return type();
    }

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
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record DoubleLitteral(Double value) implements Litteral<Double> {
        public Litteral<Double> flipSign() {
            return new DoubleLitteral(-value());
        }
        public Type type() { return Type.DOUBLE; }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record StringLitteral(String value) implements Litteral<String> {
        public Type type() {
            return Type.STRING;
        }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    ArrayLitteral EMPTY_ARRAY = new ArrayLitteral(new ArrayList<>(), Type.UNKNOWN);
    record ArrayLitteral(List<Expr> value, Type baseType) implements Litteral<List<Expr>> {
        public Type type() {
            return new ArrayType(baseType);
        }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record Unop(Expr expr, Operator operator, Type type) implements Expr {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this);}
    }

    record Binop(Expr left, Expr right, Operator op) implements Expr {

        @Override
        public Type findType(Map<Atom, Type> context) {
            final Type rightType = right.findType(context);
            final Type leftType = left.findType(context);
            if (leftType.equals(rightType)) {
                return leftType;
            }
            if ((leftType instanceof IntegerType && rightType instanceof DoubleType)
                    || (rightType instanceof IntegerType && leftType instanceof DoubleType))  {
                return Type.DOUBLE;
            }
            return type();

        }

        public Type type() {
            final Type rightType = right.type();
            final Type leftType = left.type();
            if (leftType.equals(rightType)) {
                return left().type();
            }
            if ((leftType instanceof IntegerType && rightType instanceof DoubleType)
                || (rightType instanceof IntegerType && leftType instanceof DoubleType))  {
                    return Type.DOUBLE;
            }
            return Type.UNKNOWN;
        }

        public String toString() {
            return "Binop[%s, left=%s, right=%s]"
                    .formatted(op().toString(), left(), right());
        }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record PExpr(Expr inner) implements Expr {
        public Type type() { return inner().type(); }
        public Expr unpack() { return inner(); }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record VarRef(Atom ref, Type type) implements Expr {
        public VarRef(Atom ref) {
            this(ref, Type.UNKNOWN);
        }

        @Override
        public Type findType(Map<Atom, Type> context) {
            return context.getOrDefault(ref, type);
        }

        @Override
        public String toString() {
            return "Ref['%s']".formatted(ref.value());
        }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record DeRef(Expr left, Atom rigth, Type type) implements Expr {
        public DeRef(Expr left, Atom right) {
            this(left, right, Type.UNKNOWN);
        }

        @Override
        public String toString() {
            return "DeRef[left=%s, rigth=%s".formatted(left().toString(), rigth().toString());
        }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record FunctionCall(Location location, Atom ref, List<Expr> args) implements Expr {
        public Type type() {
            return new FunctionType(args().stream().map(Expr::type).toList(), Type.UNKNOWN);
        }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record ReturnExpr(Expr expr) implements Expr {
        public Type type() { return expr.type(); }

        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

}
