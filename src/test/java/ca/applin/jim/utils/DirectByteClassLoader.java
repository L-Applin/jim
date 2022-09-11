package ca.applin.jim.utils;

public class DirectByteClassLoader extends ClassLoader {

    private final byte[] bytes;

    public DirectByteClassLoader(byte[] bytes) {
        super();
        this.bytes = bytes;
    }

    @Override
    public Class<?> findClass(final String name) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
