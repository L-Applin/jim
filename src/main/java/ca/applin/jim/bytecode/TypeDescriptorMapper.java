package ca.applin.jim.bytecode;

import ca.applin.jim.ast.Type;
import ca.applin.jim.ast.Type.Void;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;

import static ca.applin.jib.utils.Utils.todo;
import static ca.applin.jim.ast.Type.*;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.util.stream.Collectors.joining;

public class TypeDescriptorMapper {


    public TypeDescriptor toTypeDesciptor(Type type) {
        return switch (type) {
            case Unit u -> java.lang.Void.class;
            case Void v -> java.lang.Void.class;
            case PType pt -> toTypeDesciptor(pt.type());
            case SimpleType sp -> ClassDesc.of(sp.name()); // ... todo resolve full name ...
            case StringType st -> CD_String;
            case IntegerType it -> CD_Integer;
            case FloatType ft -> CD_Double;
            case ArrayType at -> CD_List;
            case GenericType gt -> toTypeDesciptor(gt.type());
            case FunctionType functionType -> forMethod(functionType);
            case StructType structType -> todo();
            case SumType sumType -> todo();
            case TupleType tupleType -> todo();
            case TypeType typeType -> todo();
            case Unknown unknown -> todo();
        };
    }

    private TypeDescriptor forMethod(FunctionType ft) {
        return todo();
    }

    private String typeToString(Type type) {
        return todo();
    }

    // todo resolve type name against fully classified name, ie List -> Ljava/util/List
    public String typeToDescriptor(Type type) {
        return typeToDescriptor(type, true);
    }

    private String typeToDescriptor(Type type, boolean root) {
        return switch (type) {

            case Type.Void __ -> "V";

            case PType pt -> typeToDescriptor(pt.type(), true);

            case SimpleType sp -> sp.name() + ";"; // todo resolve full name ...

            case GenericType gt -> "%s<%s>".formatted(
                typeToDescriptor(gt.type(), false),
                gt.generics().stream().map(t -> typeToDescriptor(t, false)).collect(joining())
            );

            case Primitive p ->
                    switch (p.name()) {
                        case "String" -> "Ljava/lang/String;";
                        case "Int" -> "I";
                        case "Float" -> "D";
                        default -> todo("report unknown primitive '" + p.name() + "'");
                    };

            case ArrayType ar -> "[" + typeToDescriptor(ar.baseType());

            case FunctionType f -> {
                if (!root) {
                    todo("Type descriptor for functions that wont be method: " + type);
                }
                yield "(%s)%s".formatted(
                    f.args().stream().map(t -> typeToDescriptor(t, false)).collect(joining()),
                    typeToDescriptor(f.returnType(), false));
            }

            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
