package ca.applin.jim.bytecode;

public class Field {

    // access flags
    public static final short ACC_PUBLIC      = 0x0001;
    public static final short ACC_PRIVATE	  = 0x0002;
    public static final short ACC_PROTECTED   = 0x0004;
    public static final short ACC_STATIC	  = 0x0008;
    public static final short ACC_FINAL	      = 0x0010;
    public static final short ACC_VOLATILE	  = 0x0040;
    public static final short ACC_TRANSIENT	  = 0x0080;
    public static final short ACC_SYNTHETIC	  = 0x1000;
    public static final short ACC_ENUM	      = 0x4000;

//    public final short acess_flag, name_index, descirptor_index, attorbute_count;
//    public final Attribute_Info[] attributes;

    public byte[] info;

    /*
    field_info {
        u2             access_flags;
        u2             name_index;
        u2             descriptor_index;
        u2             attributes_count;
        attribute_info attributes[attributes_count];
    }
     */
    public Field(short access_flags, short name_index, short descriptor_index, short attributes_count,
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
        this.info[7] =  attr_count_bytes[1];
        int current_index = 0;
        for (int i = 0; i < attributes_count; i++) {
            for (int j = 0; j < attributes[i].info.length; j++) {
                this.info[8 + current_index++] = attributes[i].info[j];
            }
        }
    }


}
