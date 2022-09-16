package ca.applin.jim.bytecode;

import java.util.Arrays;
import java.util.HexFormat;

public class Method {

    // access flags
    public static final short FLAG_ACC_PUBLIC        = 0x0001;
    public static final short FLAG_ACC_PRIVATE       = 0x0002;
    public static final short FLAG_ACC_PROTECTED     = 0x0004;
    public static final short FLAG_ACC_STATIC        = 0x0008;
    public static final short FLAG_ACC_FINAL         = 0x0010;
    public static final short FLAG_ACC_SYNCHRONIZED  = 0x0020;
    public static final short FLAG_ACC_BRIDGE        = 0x0040;
    public static final short FLAG_ACC_VARARGS       = 0x0080;
    public static final short FLAG_ACC_NATIVE        = 0x0100;
    public static final short FLAG_ACC_ABSTRACT      = 0x0400;
    public static final short FLAG_ACC_STRICT        = 0x0800;
    public static final short FLAG_ACC_SYNTHETIC     = 0x1000;

    public byte[] info;

    /*
    method_info {
        u2             access_flags;
        u2             name_index;
        u2             descriptor_index;
        u2             attributes_count;
        attribute_info attributes[attributes_count];
    }
     */
    public Method(int access_flags, int name_index, int descriptor_index, int attributes_count,
            Attribute_Info[] attributes) {
        this((short) access_flags, (short) name_index, (short) descriptor_index, (short) attributes_count, attributes);
    }

    public Method(short access_flags, short name_index, short descriptor_index, short attributes_count,
            Attribute_Info[] attributes) {
        int total_size = 2 + 2 + 2 + 2;
        for (Attribute_Info attr : attributes) {
            total_size += attr.info.length;
        }
        this.info = new byte[total_size];
        byte[] acces_flage_bytes = ByteUtils.to_bytes_big(access_flags);
        byte[] name_index_bytes = ByteUtils.to_bytes_big(name_index);
        byte[] desc_index_bytes = ByteUtils.to_bytes_big(descriptor_index);
        byte[] attr_count_bytes = ByteUtils.to_bytes_big(attributes_count);
        this.info[0] = acces_flage_bytes[0];
        this.info[1] = acces_flage_bytes[1];
        this.info[2] = name_index_bytes[0];
        this.info[3] = name_index_bytes[1];
        this.info[4] = desc_index_bytes[0];
        this.info[5] = desc_index_bytes[1];
        this.info[6] = attr_count_bytes[0];
        this.info[7] = attr_count_bytes[1];
        int current_index = 0;
        for (int i = 0; i < attributes_count; i++) {
            for (int j = 0; j < attributes[i].info.length; j++) {
                this.info[8 + current_index++] = attributes[i].info[j];
            }
        }
    }

    @Override
    public String toString() {
        final HexFormat hex = HexFormat.of();
        return "Method[access_flag=%s, name_index=%s, descriptor_index=%s, attributes_count=%s, attributes=%s]"
            .formatted(
                hex.formatHex(new byte[]{info[0], info[1]}),
                hex.formatHex(new byte[]{info[2], info[3]}),
                hex.formatHex(new byte[]{info[4], info[5]}),
                hex.formatHex(new byte[]{info[6], info[7]}),
                hex.formatHex(Arrays.copyOfRange(info, 8, info.length))
            );
    }
}
