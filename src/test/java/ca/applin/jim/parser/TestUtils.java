package ca.applin.jim.parser;

import ca.applin.jim.expr.Ast;
import ca.applin.jim.expr.Ast.Atom;
import ca.applin.jim.expr.Expr;
import ca.applin.jim.expr.Expr.ArrayLitteral;
import ca.applin.jim.expr.Expr.Binop;
import ca.applin.jim.expr.Expr.FloatLitteral;
import ca.applin.jim.expr.Expr.FunctionCall;
import ca.applin.jim.expr.Expr.IntegerLitteral;
import ca.applin.jim.expr.Expr.Litteral;
import ca.applin.jim.expr.Expr.PExpr;
import ca.applin.jim.expr.Expr.Ref;
import ca.applin.jim.expr.Expr.StringLitteral;
import ca.applin.jim.expr.Operator;
import ca.applin.jim.expr.Type;
import ca.applin.jim.expr.Type.ArrayType;
import ca.applin.jim.expr.Type.FunctionType;
import ca.applin.jim.expr.Type.GenericType;
import ca.applin.jim.expr.Type.SimpleType;
import ca.applin.jim.expr.Type.TupleType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestUtils {

    public static SimpleType STRING_TYPE = new SimpleType("String");
    public static SimpleType INT_TYPE = new SimpleType("Int");

    static Type maybe(String s) {
        return generic("Maybe", type(s));
    }

    static Type type(String str) {
        return new SimpleType(str);
    }

    static TupleType tuple(String... types) {
        return new TupleType(Stream.of(types).map(TestUtils::type).toList());
    }

    static TupleType tuple(Type... types) {
        return new TupleType(Arrays.asList(types));
    }

    static GenericType generic(String name, Type... generics) {
        return new GenericType(name, Arrays.asList(generics));
    }

    static ArrayType array(Type type) {
        return new ArrayType(type);
    }

    static FunctionType fun(List<Type> args, Type ret) {
        return new FunctionType(args, ret);
    }

    static FunctionType fun(Type args, Type ret) {
        return fun(List.of(args), ret);
    }

    static Binop binop(Operator op, Expr left, Expr right) {
        return new Binop(left, right, op);
    }

    static IntegerLitteral litteral(int i) {
        return new IntegerLitteral(i);
    }

    static FloatLitteral litteral(double d) {
        return new FloatLitteral(d);
    }

    static StringLitteral litteral(String s) {
        return new StringLitteral(s);
    }

    static PExpr p(Expr inner) {
        return new PExpr(inner);
    }

    static FunctionCall fcall(String name, Expr... args) {
        return new FunctionCall(new Atom(name), Arrays.asList(args));
    }

    static Ref var(String name) {
        return new Ref(new Atom(name));
    }

    static ArrayLitteral array(Type type, List<Expr> elems) {
        return new ArrayLitteral(elems, type);
    }
}
