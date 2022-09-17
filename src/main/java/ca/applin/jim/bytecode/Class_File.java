package ca.applin.jim.bytecode;

import java.util.Arrays;
import java.util.HexFormat;
import org.w3c.dom.Attr;

/*
https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-2.html#jvms-2.1
struct Class_File_Format {
   u4 magic_number;

   u2 minor_version;
   u2 major_version;

   u2 constant_pool_count;
   cp_info constant_pool[constant_pool_count - 1];

   u2 access_flags;

   u2 this_class;
   u2 super_class;

   u2 interfaces_count;
   u2 interfaces[interfaces_count];

   u2 fields_count;
   field_info fields[fields_count];

   u2 methods_count;
   method_info methods[methods_count];

   u2 attributes_count;
   attribute_info attributes[attributes_count];
}
 */
public class Class_File {

    public static class Flag {
        public static final short ACC_PUBLIC = 0x0001;
        public static final short ACC_FINAL = 0x0010;
        public static final short ACC_SUPER = 0x0020;
        public static final short ACC_INTERFACE = 0x0200;
        public static final short ACC_ABSTRACT = 0x0400;
        public static final short ACC_SYNTHETIC = 0x1000;
        public static final short ACC_ANNOTATION = 0x2000;
        public static final short ACC_ENUM = 0x4000;
        public static final short ACC_MODULE = (short) 0x8000;
    }

    // note(applin 07-2022)
    // arbitrarily compiling to java 11 for now, more thought needs to be put into this
    public static final byte JAVA_MAJOR_VERSION = 11;
    public static final byte JAVA_11_CLASS_MAJOR_VERION = 55;
    public static final byte JAVA_11_CLASS_MINOR_VERION = 0;

    public static final int JAVA_MAGIC_BYTES = 0xCA_FE_BA_BE;

    public int magic_bytes;

    public short minor_version;
    public short major_version;

    public short constant_pool_count;
    public Constant_Pool_Info[] constant_pool;

    public short access_flag;

    public short this_class;
    public short super_class;

    public short interfaces_count;
    public Interface[] interfaces;

    public short fields_count;
    public Field[] fields;

    public short method_count;
    public Method[] methods;

    public short attributes_count;
    public Attribute_Info[] attributes;

    public Class_File(
            short minor_version,
            short major_version,
            short constant_pool_count,
            Constant_Pool_Info[] constant_pool,
            short access_flag,
            short this_class,
            short super_class,
            Interface[] interfaces,
            Field[] fields,
            Method[] methods,
            Attribute_Info[] attributes) {
        this.magic_bytes = JAVA_MAGIC_BYTES;
        this.minor_version = minor_version;
        this.major_version = major_version;
        this.constant_pool_count = constant_pool_count;
        this.constant_pool = constant_pool;
        this.access_flag = access_flag;
        this.this_class = this_class;
        this.super_class = super_class;
        this.interfaces_count = (short) interfaces.length;
        this.interfaces = interfaces;
        this.fields_count = (short) fields.length;
        this.fields = fields;
        this.method_count = (short) methods.length;
        this.methods = methods;
        this.attributes_count = (short) attributes.length;
        this.attributes = attributes;
    }

    public byte[] get_content() {
        int constant_pool_total_size = 0;
        for (Constant_Pool_Info constant_pool_info: constant_pool) {
            constant_pool_total_size += constant_pool_info.bytes.length;
        }

        int interfaces_total_size = 0;
        for (Interface interface_info: interfaces) {
            interfaces_total_size += interface_info.bytes.length;
        }

        int field_total_size = 0;
        for (Field field: fields) {
            field_total_size += field.info.length;
        }

        int method_total_size = 0;
        for (Method method: methods) {
            method_total_size += method.info.length;
        }

        int attr_info_total_size = 0;
        for (Attribute_Info attr_info: attributes) {
            attr_info_total_size += attr_info.info.length;
        }
        byte[] bytes = new byte[get_total_size(constant_pool_total_size, interfaces_total_size,
                field_total_size, method_total_size, attr_info_total_size)];
        int current_index = 0;
        byte[] b = ByteUtils.to_bytes_big(JAVA_MAGIC_BYTES);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        bytes[current_index++] = b[2];
        bytes[current_index++] = b[3];
        b = ByteUtils.to_bytes_big(minor_version);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        b = ByteUtils.to_bytes_big(major_version);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];

        b = ByteUtils.to_bytes_big(constant_pool_count);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        for (Constant_Pool_Info pool_info: constant_pool) {
            System.arraycopy(pool_info.bytes, 0, bytes, current_index, pool_info.bytes.length);
            current_index += pool_info.bytes.length;
        }

        b = ByteUtils.to_bytes_big(access_flag);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];

        b = ByteUtils.to_bytes_big(this_class);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];

        b = ByteUtils.to_bytes_big(super_class);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];

        b = ByteUtils.to_bytes_big(interfaces_count);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        for (Interface interf: interfaces) {
            System.arraycopy(interf.bytes, 0, bytes, current_index, interf.bytes.length);
            current_index += interf.bytes.length;
        }

        b = ByteUtils.to_bytes_big(fields_count);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        for (Field field: fields) {
            System.arraycopy(field.info, 0, bytes, current_index, field.info.length);
            current_index += field.info.length;
        }

        b = ByteUtils.to_bytes_big(method_count);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        for (Method method: methods) {
            System.arraycopy(method.info, 0, bytes, current_index, method.info.length);
            current_index += method.info.length;
        }

        b = ByteUtils.to_bytes_big(attributes_count);
        bytes[current_index++] = b[0];
        bytes[current_index++] = b[1];
        for (Attribute_Info attr_info: attributes) {
            System.arraycopy(attr_info.info, 0, bytes, current_index, attr_info.info.length);
            current_index += attr_info.info.length;
        }

        return bytes;
    }

    private int get_total_size(int constant_pool_total_size, int interfaces_total_size,
            int field_total_size, int method_total_size, int attr_info_total_size) {

        return
            4           // magic bytes
            + 2 + 2     // minor major version
            + 2  + constant_pool_total_size
            + 2         // access flags
            + 2 + 2     // this class, super class
            + 2 + interfaces_total_size
            + 2 + field_total_size
            + 2 + method_total_size
            + 2 + attr_info_total_size
            ;
    }

    public String version() {
        return major_version + "." + minor_version;
    }

    @Override
    public String toString() {
        HexFormat fmt = HexFormat.of().withDelimiter(" ");
        return "Class_File{" +
                "magic_bytes=" + fmt.toHexDigits(magic_bytes) +
                ", minor_version=" + fmt.toHexDigits(minor_version) +
                ", major_version=" + fmt.toHexDigits(major_version) +
                ", constant_pool_count=" + fmt.toHexDigits(constant_pool_count) +
                ", constant_pool=" + Arrays.toString(constant_pool) +
                ", access_flag=" + fmt.toHexDigits(access_flag) +
                ", this_class=" + fmt.toHexDigits(this_class) +
                ", super_class=" + fmt.toHexDigits(super_class) +
                ", interfaces_count=" + fmt.toHexDigits(interfaces_count) +
                ", interfaces=" + Arrays.toString(interfaces) +
                ", fields_count=" + fmt.toHexDigits(fields_count) +
                ", fields=" + Arrays.toString(fields) +
                ", method_count=" + fmt.toHexDigits(method_count) +
                ", methods=" + Arrays.toString(methods) +
                ", attributes_count=" + fmt.toHexDigits(attributes_count) +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }
}
