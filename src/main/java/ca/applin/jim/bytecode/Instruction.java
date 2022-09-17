package ca.applin.jim.bytecode;

import java.util.Arrays;
import java.util.List;

public class Instruction {

    // opcodes

    // Push item from run-time constant pool
    public static final byte ldc            = (byte) 0x12;

    // aload index, aload_<n>: Load reference from local variable
    public static final byte aload         = 0x19,
                             aload_0       = 0x2a,
                             aload_1       = 0x2b,
                             aload_2       = 0x2c,
                             aload_3       = 0x2d;

    // aaload: Load reference from array
    public static final byte aaload        = 0x32;

    // aastore: Store into reference array
    public static final byte aastore       = 0x53;

    // iload index, iload_<n>: Load int from local variable.
    public static final byte iload         = 0x15,
                             iload_0       = 0x1a,
                             iload_1       = 0x1b,
                             iload_2       = 0x1c,
                             iload_3       = 0x1d;

    // Add, Subtract, multiply, divides etc int
    public static final byte iadd          = 0x60,
                             isub          = 0x64,
                             imul          = 0x68,
                             idiv          = 0x6c,
                             irem          = 0x70;

    public static final byte dadd          = 0x63,
                             dsub          = 0x67,
                             dmul          = 0x6b,
                             ddiv          = 0x6f;

    // Convert int to double
    public static final byte i2d           = (byte) 0x87;

    // Push byte to operand stack
    public static final byte bipush        = 0x10;

    // Push long or double from run-time constant pool (wide index)
    public static final byte ldc2_w        = 0x14;


    public static final byte ireturn        = (byte) 0xAC;

    public static final byte return_void    = (byte) 0xb1;

    // Get static field from class
    public static final byte getstatic      = (byte) 0xb2;
    public static final byte invokesvirtual = (byte) 0xb6;
    public static final byte invokespecial  = (byte) 0xb7;


    public static byte[] bytecode(Instruction ...instructions) {
        return bytecode(Arrays.asList(instructions));
    }

    public static byte[] bytecode(List<Instruction> instructions) {
        int total_size = 0;
        for (Instruction instruction: instructions) {
            total_size += instruction.bytes.length;
        }
        byte[] bytes = new byte[total_size];
        int current_index = 0;
        for (Instruction instruction: instructions) {
            System.arraycopy(instruction.bytes, 0, bytes, current_index, instruction.bytes.length);
            current_index += instruction.bytes.length;
        }
        return bytes;
    }


    // Load reference from local variable
    public static Instruction aload_0() { return new Instruction(aload_0, (short) 1); }
    public static Instruction aload_1() { return new Instruction(aload_1, (short) 1); }
    public static Instruction aload_2() { return new Instruction(aload_2, (short) 1); }
    public static Instruction aload_3() { return new Instruction(aload_3, (short) 1); }
    public static Instruction aload(byte n) {
        return switch (n) {
            case 0 -> aload_0();
            case 1 -> aload_1();
            case 2 -> aload_2();
            case 3 -> aload_3();
            default -> new Instruction(new byte[] { aload, n }, (short) 1);
        };
    }

    public static Instruction iload_0() { return new Instruction(iload_0, (short) 1);}
    public static Instruction iload_1() { return new Instruction(iload_1, (short) 1); }
    public static Instruction iload_2() { return new Instruction(iload_2, (short) 1); }
    public static Instruction iload_3() { return new Instruction(iload_3, (short) 1); }
    public static Instruction iload(byte n) {
        return switch (n) {
            case 0 -> iload_0();
            case 1 -> iload_1();
            case 2 -> iload_2();
            case 3 -> iload_3();
            default -> new Instruction(new byte[]{ iload, n }, (short) 1);
        };
    }

    public static Instruction ldc2_w(byte indexbyte1, byte indexbyte2) {
        return new Instruction(new byte[] {ldc2_w, indexbyte1, indexbyte2}, (short) 2);
    }

    public static Instruction iadd() { return new Instruction(iadd, (short) -1); }
    public static Instruction isub() { return new Instruction(isub, (short) -1); }
    public static Instruction imul() { return new Instruction(imul, (short) -1); }
    public static Instruction idiv() { return new Instruction(idiv, (short) -1); }
    public static Instruction ireturn() { return new Instruction(ireturn, (short) -1); }

    public static Instruction dadd() { return new Instruction(dadd, (short) -1); }
    public static Instruction dsub() { return new Instruction(dsub, (short) -1); }
    public static Instruction dmul() { return new Instruction(dmul, (short) -1); }
    public static Instruction ddiv() { return new Instruction(ddiv, (short) -1); }

    public static Instruction i2d() { return new Instruction(i2d, (short) 1); }

    public static Instruction return_void() { return new Instruction(return_void, (short) 0); }

    public static Instruction invokespecial(byte indexbyte1, byte indexbyte2, short arg_size) {
        return new Instruction(new byte[]{ invokespecial, indexbyte1, indexbyte2 }, (short) -arg_size);
    }

    public static Instruction invokevirtual(byte indexbyte1, byte indexbyte2, short args_size) {
        return new Instruction(new byte[] { invokesvirtual, indexbyte1, indexbyte2 }, (short) -args_size);
    }

    public static Instruction getstatic(byte indexbyte1, byte indexbyte2) {
        return new Instruction(new byte[] { getstatic, indexbyte1, indexbyte2}, (short) 1);
    }

    public static Instruction ldc(byte index) {
        return new Instruction(new byte[] { ldc, index }, (short) 1);
    }

    public static Instruction bipush(byte value) {
        return new Instruction(new byte[] { bipush, value }, (short) 1);
    }

    public byte[] bytes;
    public short stack_value_added;

    public Instruction(byte[] bytes, short stack_value_added) {
        this.bytes = bytes;
        this.stack_value_added = stack_value_added;
    }

    public Instruction(byte bytes, short stack_value_added) {
        this(new byte[] { bytes }, stack_value_added);
    }

    public Instruction(byte bytes) {
        this(new byte[] { bytes });
    }

    public Instruction(byte[] bytes) {
        this(bytes, (short) 0);
    }

}
