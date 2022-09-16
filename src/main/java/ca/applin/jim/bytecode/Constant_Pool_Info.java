package ca.applin.jim.bytecode;

import static ca.applin.jim.bytecode.ByteUtils.to_bytes_big;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class Constant_Pool_Info {

    // tags
    public static final byte TAG_UTF_8 = (byte) 1;
    public static final byte TAG_INTEGER = (byte) 3;
    public static final byte TAG_FLOAT = (byte) 4;
    public static final byte TAG_LONG = (byte) 5;
    public static final byte TAG_DOUBLE = (byte) 6;
    public static final byte TAG_CLASS = (byte) 7;
    public static final byte TAG_STRING = (byte) 8;
    public static final byte TAG_FIELD_REF = (byte) 9;
    public static final byte TAG_METHOD_REF = (byte) 10;
    public static final byte TAG_INTERFACE_METHOD_REF = (byte) 11;
    public static final byte TAG_NAME_AND_TYPE = (byte) 12;
    public static final byte TAG_METHOD_HANDLE = (byte) 15;
    public static final byte TAG_METHOD_TYPE = (byte) 16;
    public static final byte TAG_DYNAMIC = (byte) 17;
    public static final byte TAG_INVOKE_DYNAMIC = (byte) 18;
    public static final byte TAG_MODULE = (byte) 19;
    public static final byte TAG_PACKAGE = (byte) 20;

    // bytes[0]             => tag byte
    // bytes[1 .. length-1] => info content
    public final byte[] bytes;

    Constant_Pool_Info(byte[] bytes) {
        this.bytes = bytes;
    }

    public static Constant_Pool_Info class_info(short name_index) {
        final byte[] bytes = to_bytes_big(name_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_CLASS,
                bytes[0], bytes[1]
        });
    }

    public static Constant_Pool_Info fieldRef_info(short class_index, short name_and_type_index) {
        byte[] class_index_bytes = to_bytes_big(class_index);
        byte[] name_and_type_index_bytes = to_bytes_big(name_and_type_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_FIELD_REF,
                class_index_bytes[0],
                class_index_bytes[1],
                name_and_type_index_bytes[0],
                name_and_type_index_bytes[1]
        });
    }

    public static Constant_Pool_Info methodRef_info(short class_index, short name_and_type_index) {
        byte[] class_index_bytes = to_bytes_big(class_index);
        byte[] name_and_type_index_bytes = to_bytes_big(name_and_type_index);
        return new Constant_Pool_Info(new byte[]{
                TAG_METHOD_REF,
                class_index_bytes[0],
                class_index_bytes[1],
                name_and_type_index_bytes[0],
                name_and_type_index_bytes[1]
        });
    }

    public static Constant_Pool_Info interfaceMethodRef_info(short class_index, short name_and_type_index) {
        byte[] class_index_bytes = to_bytes_big(class_index);
        byte[] name_and_type_index_bytes = to_bytes_big(name_and_type_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_INTERFACE_METHOD_REF,
                class_index_bytes[0],
                class_index_bytes[1],
                name_and_type_index_bytes[0],
                name_and_type_index_bytes[1]
        });
    }

    public static Constant_Pool_Info string_info(short string_index) {
        final byte[] bytes = to_bytes_big(string_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_STRING,
                bytes[0], bytes[1]
        });
    }

    public static Constant_Pool_Info integer_info(int value) {
        final byte[] int_bytes = to_bytes_big(value);
        return new Constant_Pool_Info(new byte[] {
                TAG_INTEGER,
                int_bytes[0], int_bytes[1], int_bytes[2], int_bytes[3]
        });
    }

    public static Constant_Pool_Info float_info(float value) {
        final byte[] fl_bytes = to_bytes_big(value);
        return new Constant_Pool_Info(new byte[] {
                TAG_FLOAT,
                fl_bytes[0], fl_bytes[1], fl_bytes[2], fl_bytes[3]
        });
    }

    public static Constant_Pool_Info long_info(int high_bytes, int low_bytes) {
        byte[] high = to_bytes_big(high_bytes);
        byte[] low  = to_bytes_big(low_bytes);
        return new Constant_Pool_Info(new byte[] {
            TAG_LONG,
            high[0], high[1], high[2], high[3],
            low[0],  low[1],  low[2],  low[3]
        });
    }

    public static Constant_Pool_Info double_info(int high_bytes, int low_bytes) {
        byte[] high = to_bytes_big(high_bytes);
        byte[] low  = to_bytes_big(low_bytes);
        return new Constant_Pool_Info(new byte[] {
                TAG_FLOAT,
                high[0], high[1], high[2], high[3],
                low[0],  low[1],  low[2],  low[3]
        });
    }

    public static Constant_Pool_Info nameAndType_info(short name_index, short descriptor_index) {
        byte[] name_bytes = to_bytes_big(name_index);
        byte[] descriptor_bytes = to_bytes_big(descriptor_index);
        return new Constant_Pool_Info(new byte[] {
            TAG_NAME_AND_TYPE,
            name_bytes[0], name_bytes[1],
            descriptor_bytes[0], descriptor_bytes[1],
        });
    }

    // does not check the "modified UTF-8" encoding ...
    public static Constant_Pool_Info utf8_info(byte[] bytes) {
        byte[] data = new byte[bytes.length + 3];
        byte[] len = to_bytes_big((short) bytes.length);
        data[0] = TAG_UTF_8;
        data[1] = len[0];
        data[2] = len[1];
        System.arraycopy(bytes, 0, data, 3, bytes.length);
        return new Constant_Pool_Info(data);
    }

    // does not check the "modified UTF-8" encoding ...
    public static Constant_Pool_Info utf8_info(String str) {
        return utf8_info(str.getBytes(StandardCharsets.UTF_8));
    }

    public static Constant_Pool_Info methodHandle_info(byte reference_kind, short reference_index) {
        byte[] ref_bytes = to_bytes_big(reference_index);
        return new Constant_Pool_Info(new byte[] {
            TAG_METHOD_HANDLE,
            reference_kind,
            ref_bytes[0], ref_bytes[1]
        });
    }

    public static Constant_Pool_Info dynamic_info(short bootstrap_method_attr_index, short name_and_type_index) {
        byte[] attr_bytes = to_bytes_big(bootstrap_method_attr_index);
        byte[] names_and_types_bytes = to_bytes_big(name_and_type_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_DYNAMIC,
                attr_bytes[0], attr_bytes[1],
                names_and_types_bytes[0], names_and_types_bytes[1],
        });
    }

    public static Constant_Pool_Info invokeDynamic_info(short bootstrap_method_attr_index, short name_and_type_index) {
        byte[] attr_bytes = to_bytes_big(bootstrap_method_attr_index);
        byte[] names_and_types_bytes = to_bytes_big(name_and_type_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_INVOKE_DYNAMIC,
                attr_bytes[0], attr_bytes[1],
                names_and_types_bytes[0], names_and_types_bytes[1],
        });
    }

    public static Constant_Pool_Info module_info(short name_index) {
        final byte[] bytes = to_bytes_big(name_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_MODULE,
                bytes[0], bytes[1]
        });
    }

    public static Constant_Pool_Info package_info(short name_index) {
        final byte[] bytes = to_bytes_big(name_index);
        return new Constant_Pool_Info(new byte[] {
                TAG_PACKAGE,
                bytes[0], bytes[1]
        });
    }

}
