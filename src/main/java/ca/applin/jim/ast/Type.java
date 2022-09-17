package ca.applin.jim.ast;

import static ca.applin.jib.utils.Maybe.just;
import static ca.applin.jib.utils.Maybe.nothing;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.ast.Type.ArrayType;
import ca.applin.jim.ast.Type.DoubleType;
import ca.applin.jim.ast.Type.FunctionType;
import ca.applin.jim.ast.Type.GenericType;
import ca.applin.jim.ast.Type.IntegerType;
import ca.applin.jim.ast.Type.PType;
import ca.applin.jim.ast.Type.Primitive;
import ca.applin.jim.ast.Type.SimpleType;
import ca.applin.jim.ast.Type.StringType;
import ca.applin.jim.ast.Type.StructType;
import ca.applin.jim.ast.Type.SumType;
import ca.applin.jim.ast.Type.TupleType;
import ca.applin.jim.ast.Type.TypeType;
import ca.applin.jim.ast.Type.Unit;
import ca.applin.jim.ast.Type.Unknown;
import ca.applin.jim.ast.Type.Void;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.Location;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface Type extends Expr
        permits ArrayType, DoubleType, FunctionType, GenericType, IntegerType, PType, Primitive,
        SimpleType, StringType, StructType, SumType, TupleType, TypeType, Unit, Unknown, Void
{
    // for test only???

    /// how will we ahandle primitives in code???
    Primitive STRING = new StringType();
    Primitive INTEGER = new IntegerType();
    Primitive DOUBLE = new DoubleType();

    default Type type() {
        return TypeType.INSTANCE;
    }

    default boolean typeIsKnown() {
        return ! (type() instanceof Type.Unknown);
    }

    default Type unpack() {
        return this;
    }

    default boolean isFunctionType() {
        return false;
    }


    Type UNIT = new Unit();
    record Unit() implements Type {
        public String toString(){ return "<Unit>"; }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    Type VOID = new Void();
    record Void() implements Type {
        public String toString(){ return "<Void>"; }
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record TypeType() implements Type {
        public static TypeType INSTANCE = new TypeType();
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record Unknown() implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }
    Type UNKNOWN = new Unknown();

    record SimpleType(
            String name
    ) implements Type {
        public String toString() {
            return name();
        }

        @Override
        public void visit(AstVisitor astVisitor) {
            astVisitor.visit(this);
        }
    }

    // helper Type to help compiler identify tuple with a single elements
    record PType(
            Type inner
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public static Maybe<Type> from(Type type) { return just(new PType(type)); }
        public Type unpack() { return this.inner; }
    }

    record FunctionType(
            List<Type> args,
            Type returnType
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }

        @Override
        public boolean isFunctionType() {
            return true;
        }

        public String toString() {
            return "function::("
                    + (args().size() == 1 ? "" : "(")
                    + args().stream().map(Type::toString).collect(Collectors.joining(", "))
                    + (args().size() == 1 ? " ": ") ")
                    + "-> "
                    + returnType().toString()
                    + ")"
                    ;
        }
    }

    record ArrayType(
            Type baseType
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public String toString() {
            return "array::[" + baseType().toString() + "]";
        }
    }

    record TupleType(
            List<Type> types
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public String toString() {
            return "tuple::(" + types().stream().map(Type::toString).collect(Collectors.joining(", ")) + ")";
        }
    }

    record StructType(
            Map<String, StructElem> elems
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    record StructElem(
            Location location,
            Atom name,
            boolean isConst,
            Type type
    ) implements Decl {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public boolean isConst() { return false; }

        public StructElem(LexerToken lexerToken, boolean isConst, Type type) {
            this(lexerToken.location(), new Atom(lexerToken.str()), isConst, type);
        }
    }

    record GenericType(
            String name,
            List<Type> generics
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public String toString() {
            return name() + "<" + generics().stream().map(Type::toString).collect(Collectors.joining(", ")) + ">";
        }
    }

    record SumType (
            Map<String, List<Type>> constructors
    ) implements Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
    }

    // List A = Nil | Cons A (List A) ;
    //
    record SumTypeElem(
            String constructorName,
            Type elemType
    ) { }

    sealed interface Primitive extends Type  permits StringType, IntegerType, DoubleType {
        String name();
        static Maybe<Type> fromString(String str) {
            return switch (str) {
                case "String" -> just(STRING);
                case "Int" -> just(INTEGER);
                case "Float" -> just(DOUBLE);
                default -> nothing();
            };
        }
    }

    record StringType() implements Primitive, Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public String name() { return "String"; }
    }
    record IntegerType() implements Primitive, Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public String name() { return "Int"; }
    }
    record DoubleType() implements Primitive, Type {
        public void visit(AstVisitor astVisitor) { astVisitor.visit(this); }
        public String name() { return "Float"; }
    }

}
