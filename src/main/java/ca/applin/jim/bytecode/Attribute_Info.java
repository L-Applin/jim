package ca.applin.jim.bytecode;

public class Attribute_Info {

    public byte[] info;

    /**
     * <pre>
     * attribute_info {
     *     u2 attribute_name_index;
     *     u4 attribute_length;
     *     u1 info[attribute_length];
     * }
     * </pre>
     */
    public Attribute_Info(short attribute_name_index, int attribute_length, byte[] info) {
        this.info = new byte[ attribute_length + 6 ];
        byte[] attr_name_index_bytes = ByteUtils.to_bytes_big(attribute_name_index);
        byte[] attr_length_bytes = ByteUtils.to_bytes_big(attribute_length);
        this.info[0] = attr_name_index_bytes[0];
        this.info[1] = attr_name_index_bytes[1];
        this.info[2] = attr_length_bytes[0];
        this.info[3] = attr_length_bytes[1];
        this.info[4] = attr_length_bytes[2];
        this.info[5] = attr_length_bytes[3];
        System.arraycopy(info, 0, this.info, 6, attribute_length);
    }

    /**
     * <pre>
     * ConstantValue_attribute {
     *     u2 attribute_name_index;
     *     u4 attribute_length;
     *     u2 constantvalue_index;
     * }
     * </pre>
     */
    public static Attribute_Info constant_value(
            short attribute_name_index, short constantvalue_index) {
        return new Attribute_Info(attribute_name_index, 2, ByteUtils.to_bytes_big(constantvalue_index));
    }

    /**
     * <pre>
     * Code_attribute {
     *     u2 attribute_name_index;
     *     u4 attribute_length;
     *     u2 max_stack;
     *     u2 max_locals;
     *     u4 code_length;
     *     u1 code[code_length];
     *     u2 exception_table_length;
     *     {   u2 start_pc;
     *         u2 end_pc;
     *         u2 handler_pc;
     *         u2 catch_type;
     *     } exception_table[exception_table_length];
     *     u2 attributes_count;
     *     attribute_info attributes[attributes_count];
     * }
     * </pre>
     */
    public static Attribute_Info code(
            int    attribute_name_index,
            short  max_stack,
            short  max_local,
            byte[] code,
            Exception_Table[] exception_table,
            Attribute_Info[] attributes) {

        int total_attr_info_length = 0;

        for (Attribute_Info attribute : attributes) {
            total_attr_info_length += attribute.info.length;
        }

        int total_byte_size = 2 + 2 + 4 + code.length + 2 + exception_table.length * 8 + 2 + total_attr_info_length;
        byte[] bytes = new byte[total_byte_size];

        int current_index = 0;
        byte[] max_stack_bytes = ByteUtils.to_bytes_big(max_stack);
        bytes[current_index++] = max_stack_bytes[0];
        bytes[current_index++] = max_stack_bytes[1];

        byte[] max_local_bytes = ByteUtils.to_bytes_big(max_local);
        bytes[current_index++] = max_local_bytes[0];
        bytes[current_index++] = max_local_bytes[1];

        byte[] code_length_bytes = ByteUtils.to_bytes_big(code.length);
        bytes[current_index++] = code_length_bytes[0];
        bytes[current_index++] = code_length_bytes[1];
        bytes[current_index++] = code_length_bytes[2];
        bytes[current_index++] = code_length_bytes[3];

        System.arraycopy(code, 0, bytes, 8, code.length);
        current_index += code.length;

        byte[] exception_table_length = ByteUtils.to_bytes_big((short) exception_table.length);
        bytes[current_index] = exception_table_length[0]; current_index += 1;
        bytes[current_index] = exception_table_length[0]; current_index += 1;
        for (Exception_Table exceptionTable : exception_table) {
            System.arraycopy(exceptionTable.info, 0, bytes, current_index, 8);
            current_index += 8;
        }

        byte[] attributes_count = ByteUtils.to_bytes_big((short) attributes.length);
        bytes[current_index] = attributes_count[0]; current_index += 1;
        bytes[current_index] = attributes_count[0]; current_index += 1;
        for (Attribute_Info attribute: attributes) {
            System.arraycopy(attribute.info, 0, bytes, current_index, attribute.info.length);
            current_index += attribute.info.length;
        }

        return new Code_Attribute((short) attribute_name_index, bytes.length, bytes);
    }

    public static class Exception_Table {
        public byte[] info;
        public Exception_Table(short start_pc, short end_pc, short handler_pc, short catch_type) {
            byte[] start_bytes = ByteUtils.to_bytes_big(start_pc);
            byte[] end_bytes = ByteUtils.to_bytes_big(end_pc);
            byte[] handler_bytes = ByteUtils.to_bytes_big(handler_pc);
            byte[] catch_bytes = ByteUtils.to_bytes_big(catch_type);
            this.info = new byte[8];
            this.info[0] = start_bytes[0];
            this.info[1] = start_bytes[1];
            this.info[2] = end_bytes[0];
            this.info[3] = end_bytes[1];
            this.info[4] = handler_bytes[0];
            this.info[5] = handler_bytes[1];
            this.info[6] = catch_bytes[0];
            this.info[7] = catch_bytes[1];
        }
    }
}
