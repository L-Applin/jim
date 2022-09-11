package ca.applin.jim.parser;

import ca.applin.jim.ast.Ast.Atom;
import ca.applin.jim.ast.Expr;
import ca.applin.jim.ast.Expr.ArrayLitteral;
import ca.applin.jim.ast.Expr.Binop;
import ca.applin.jim.ast.Expr.FloatLitteral;
import ca.applin.jim.ast.Expr.FunctionCall;
import ca.applin.jim.ast.Expr.IntegerLitteral;
import ca.applin.jim.ast.Expr.PExpr;
import ca.applin.jim.ast.Expr.Ref;
import ca.applin.jim.ast.Expr.StringLitteral;
import ca.applin.jim.ast.Operator;
import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.ArrayType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.GenericType;
import ca.applin.jim.ast.Type.SimpleType;
import ca.applin.jim.ast.Type.TupleType;
import ca.applin.jim.lexer.LexerToken.Location;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestUtils {

    public static SimpleType STRING_TYPE = new SimpleType("String");
    public static SimpleType INT_TYPE = new SimpleType("Int");
    public static Location TEST_LOCATION = new Location("<from string>", 0, 0);

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
        return new FunctionCall(TEST_LOCATION, new Atom(name), Arrays.asList(args));
    }

    static Ref var(String name) {
        return new Ref(new Atom(name));
    }

    static ArrayLitteral array(Type type, List<Expr> elems) {
        return new ArrayLitteral(elems, type);
    }
}
