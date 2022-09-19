package ca.applin.jim.bytecode.gen;

import static ca.applin.jim.bytecode.Constant_Pool_Info.class_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.double_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.fieldRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.integer_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.methodRef_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.nameAndType_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.string_info;
import static ca.applin.jim.bytecode.Constant_Pool_Info.utf8_info;

import ca.applin.jim.bytecode.Constant_Pool_Info;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPool {

    private static final List<Constant_Pool_Info> SUPER_OBJECT = List.of(
            methodRef_info((short) 2, (short) 3),
            class_info((short) 4),
            nameAndType_info((short) 5, (short) 6),
            utf8_info("java/lang/Object"),
            utf8_info("<init>"),
            utf8_info("()V")
    );

    public static final FieldRef SYSTEM_OUT_FIELD_REF = new FieldRef(
            "java/lang/System",
            "out",
            "Ljava/io/PrintStream;");

    public static final MethodRef PRINT_LN_VOID_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "()V"
    );

    public static final MethodRef PRINT_LN_STR_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V"
    );

    public static final MethodRef PRINT_LN_INT_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "(I)V"
    );

    public static final MethodRef PRINT_LN_DOUBLE_METHOD_REF = new MethodRef(
            "java/io/PrintStream",
            "println",
            "(D)V"
    );
    
    public static final Constant_Pool_Info UTF8_CODE_CONSTANT_POOL_INFO = utf8_info("Code");
    public static final short UTF8_CODE_CONSTANT_POOL_INFO_INDEX = 7;

    private final List<Constant_Pool_Info> constantPoolInfos = new ArrayList<>() {{
        addAll(SUPER_OBJECT);
        add(UTF8_CODE_CONSTANT_POOL_INFO); // index #7
    }};

    private final Map<FieldRef, Short> fieldRefs  = new HashMap<>();
    private final Map<MethodRef, Short> methodRefs = new HashMap<>();

    private short index_offset = 0;

    public short add(Constant_Pool_Info class_info) {
        constantPoolInfos.add(class_info);
        return current_index();
    }

    public short size() {
        return (short) (constantPoolInfos.size() + 1 + index_offset);
    }

    public Constant_Pool_Info[] infos() {
        return constantPoolInfos.toArray(new Constant_Pool_Info[0]);
    }

    public short code_info() {
        return UTF8_CODE_CONSTANT_POOL_INFO_INDEX;
    }

    public short addUtf8(String typeDescr) {
        constantPoolInfos.add(utf8_info(typeDescr));
        return current_index();
    }

    public short addString(String str) {
        // todo check if constant does not already exists
        short utf8Index = addUtf8(str);
        constantPoolInfos.add(string_info(utf8Index));
        return current_index();
    }

    public short addInt(int value) {
        constantPoolInfos.add(integer_info(value));
        return current_index();
    }

    public short addDouble(double value) {
        long bytes = Double.doubleToRawLongBits(value);
        int low_bytes  = (int) bytes;
        int high_bytes = (int) ((bytes & 0x00000000FFFFFFFF) >> 32);
        constantPoolInfos.add(double_info(high_bytes, low_bytes));
        short index = current_index();
        index_offset++;
        return index;
    }

    public short createMethodRefOrGetIndex() {
        return (short) -1;
    }

    public record FieldRef(String clz, String field, String type) {
        public String toString() { return clz + "." + field + ":" + type; }
    }

    public short fieldRef(FieldRef fieldRef) {
        if (fieldRefs.containsKey(fieldRef)) {
            return fieldRefs.get(fieldRef);
        }
        short ref_class = addUtf8(fieldRef.clz());
        short ref_field = addUtf8(fieldRef.field);
        short ref_type = addUtf8(fieldRef.type);
        constantPoolInfos.add(class_info(ref_class));
        short  ref_class_info = current_index();
        constantPoolInfos.add(nameAndType_info(ref_field, ref_type));
        short ref_name_and_type = current_index();
        constantPoolInfos.add(fieldRef_info(ref_class_info, ref_name_and_type));

        fieldRefs.put(fieldRef, current_index());
        return current_index();
    }

    public short current_index() {
        return (short) (constantPoolInfos.size() + index_offset);
    }

    public record MethodRef(String clz, String name, String type) {
        public String toString() { return "%s.%s:%s".formatted(clz, name, type); }
    }

    public short methodRef(MethodRef methodRef) {
        if (methodRefs.containsKey(methodRef)) {
            return methodRefs.get(methodRef);
        }
        short ref_class = addUtf8(methodRef.clz());
        short ref_name =  addUtf8(methodRef.name());
        short ref_type =  addUtf8(methodRef.type());

        short ref_class_info = add(class_info(ref_class));
        short ref_name_and_type = add(nameAndType_info(ref_name, ref_type));

        add(methodRef_info(ref_class_info, ref_name_and_type));
        methodRefs.put(methodRef, current_index());
        return current_index();
    }

}
