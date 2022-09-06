package ca.applin.jim.expr;

import static ca.applin.jib.utils.Maybe.just;

import ca.applin.jib.utils.Maybe;
import ca.applin.jim.lexer.LexerToken;
import ca.applin.jim.lexer.LexerToken.Location;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public /*sealed*/ interface Type extends Expr
       // permits SimpleType, FunctionType, ArrayType, TupleType, StructType, SumType, GenericType, PType
{
    // for test only???

    /// how will we ahandle primitives in code???
    Primitive STRING = new Primitive("String");
    Primitive INTEGER = new Primitive("Int");
    Primitive FLOAT = new Primitive("Float");

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


    record Unit() implements Type { public String toString(){ return "<Unit>"; } }
    Type UNIT = new Unit();

    record TypeType() implements Type {
        public static TypeType INSTANCE = new TypeType();
    }

    record Unknown() implements Type { }
    Type UNKNOWN = new Unknown();

    record SimpleType(
            String name
    ) implements Type {
        public String toString() {
            return name();
        }
    }

    // helper Type to help compiler identify tuple with a single elements
    record PType(
            Type inner
    ) implements Type {
        public static Maybe<Type> from(Type type) {
            return just(new PType(type));
        }

        @Override
        public Type unpack() {
            return this.inner;
        }
    }

    record FunctionType(
            List<Type> args,
            Type returnType
    ) implements Type {

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
        public String toString() {
            return "array::[" + baseType().toString() + "]";
        }
    }

    record TupleType(
            List<Type> types
    ) implements Type {
        public String toString() {
            return "tuple::(" + types().stream().map(Type::toString).collect(Collectors.joining(", ")) + ")";
        }
    }

    record StructType(
            Map<String, StructElem> elems
    ) implements Type { }

    record StructElem(
            Location location,
            Atom name,
            boolean isConst,
            Type type
    ) implements Decl {
        public boolean isConst() {
            return false;
        }

        public StructElem(LexerToken lexerToken, boolean isConst, Type type) {
            this(lexerToken.location(), new Atom(lexerToken.str()), isConst, type);
        }
    }

    record GenericType(
            String name,
            List<Type> generics
    ) implements Type {
        public String toString() {
            return name() + "<" + generics().stream().map(Type::toString).collect(Collectors.joining(", ")) + ">";
        }
    }

    record SumType (
            Map<String, List<Type>> constructors
    ) implements Type { }

    // List A = Nil | Cons A (List A) ;
    //
    record SumTypeElem(
            String constructorName,
            Type elemType
    ) implements Type { }

    record Primitive(String name) implements Type { }

}