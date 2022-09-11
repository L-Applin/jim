package ca.applin.jim.bytecode;

public class ByteUtils {

    // big endian
    public static byte[] to_bytes_big(short s) {
        return new byte[] {
            (byte) ((s >> 8) & 0x00FF),
            (byte) ((s     ) & 0x00FF)
        };
    }

    // big endian
    public static byte[] to_bytes_big(int i) {
        return new byte[] {
            (byte) ((i >> 24) & 0x00FF),
            (byte) ((i >> 16) & 0x00FF),
            (byte) ((i >>  8) & 0x00FF),
            (byte) ((i      ) & 0x00FF)
        };
    }

    // big endian
    public static byte[] to_bytes_big(long l) {
        return new byte[] {
            (byte) ((l >> 56) & 0x00FF),
            (byte) ((l >> 48) & 0x00FF),
            (byte) ((l >> 40) & 0x00FF),
            (byte) ((l >> 32) & 0x00FF),
            (byte) ((l >> 24) & 0x00FF),
            (byte) ((l >> 16) & 0x00FF),
            (byte) ((l >>  8) & 0x00FF),
            (byte) ((l      ) & 0x00FF)
        };
    }

}
