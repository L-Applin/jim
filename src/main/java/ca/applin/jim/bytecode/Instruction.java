package ca.applin.jim.bytecode;

import java.util.Arrays;
import java.util.List;

public class Instruction {

    // opcodes

    // aload index, aload_<n>: Load reference from local variable
    public static final byte aload         = 0x19,
                             aload_0      =  0x2a,
                             aload_1      =  0x2b,
                             aload_2      =  0x2c,
                             aload_3      =  0x2d;

    // aaload: Load reference from array
    public static final byte aaload       =  0x32;

    // aastore: Store into reference array
    public static final byte aastore      =  0x53;

    // iload index, iload_<n>: Load int from local variable.
    public static final byte iload         = 0x15,
                             iload_0      =  0x1a,
                             iload_1      =  0x1b,
                             iload_2      =  0x1c,
                             iload_3      =  0x1d;


    public static final byte iadd         =  0x60;
    public static final byte ireturn      =  (byte) 0xAC;
    public static final byte return_void  =  (byte) 0xb1;

    public static final byte invokespecial = (byte) 0xb7;


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


    public static Instruction aload_0() { return new Instruction(aload_0); }
    public static Instruction aload_1() { return new Instruction(aload_1); }
    public static Instruction aload_2() { return new Instruction(aload_2); }
    public static Instruction aload_3() { return new Instruction(aload_3); }
    public static Instruction aload(byte n) {
        return switch (n) {
            case 0 -> aload_0();
            case 1 -> aload_1();
            case 2 -> aload_2();
            case 3 -> aload_3();
            default -> new Instruction(new byte[] { aload, n });
        };
    }

    public static Instruction iload_0() { return new Instruction(iload_0);}
    public static Instruction iload_1() { return new Instruction(iload_1); }
    public static Instruction iload_2() { return new Instruction(iload_2); }
    public static Instruction iload_3() { return new Instruction(iload_3); }
    public static Instruction iload(byte n) {
        return switch (n) {
            case 0 -> iload_0();
            case 1 -> iload_1();
            case 2 -> iload_2();
            case 3 -> iload_3();
            default -> new Instruction(new byte[]{ iload, n });
        };
    }

    public static Instruction iadd() { return new Instruction(iadd); }

    public static Instruction ireturn() { return new Instruction(ireturn); }

    public static Instruction return_void() { return new Instruction(return_void); }

    public static Instruction invokespecial(byte indexbyte1, byte indexbyte2) {
        return new Instruction(new byte[]{ invokespecial, indexbyte1, indexbyte2 });
    }

    public byte[] bytes;

    public Instruction(byte bytes) {
        this.bytes = new byte[] { bytes };
    }
    public Instruction(byte[] bytes) {
        this.bytes = bytes;
    }

}
