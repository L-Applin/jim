package ca.applin.jim.bytecode;

/*
Code_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 max_stack;
    u2 max_locals;
    u4 code_length;
    u1 code[code_length];
    u2 exception_table_length;
    {   u2 start_pc;
        u2 end_pc;
        u2 handler_pc;
        u2 catch_type;
    } exception_table[exception_table_length];
    u2 attributes_count;
    attribute_info attributes[attributes_count];
}
 */
public class Code_Attribute extends Attribute_Info {

    public Code_Attribute(short attribute_name_index, int attribute_length, byte[] info) {
        super(attribute_name_index, attribute_length, info);
    }

}
