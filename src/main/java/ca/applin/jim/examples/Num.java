package ca.applin.jim.examples;

import java.util.Map;

public interface Num {

    Map<Class<?>, NumImpl<?>> implementations = Map.of(
            Size_t.class, new $Implementation__Num__Size_t()
    );

    @SuppressWarnings("unchecked")
    static <T> NumImpl<T> getImplementation(Class<T> clazz) {
        return (NumImpl<T>) implementations.get(clazz);
    }


    interface NumImpl<T> {
        T add(T a, T b);
        T sub(T a, T b);
        T mul(T a, T b);
        T div(T a, T b);
    }

    class $Implementation__Num__Size_t implements NumImpl<Size_t> {

        @Override
        public Size_t add(Size_t a, Size_t b) {
            return new Size_t(a.size() + b.size());
        }

        @Override
        public Size_t sub(Size_t a, Size_t b) {
            return new Size_t(a.size() - b.size());
        }

        @Override
        public Size_t mul(Size_t a, Size_t b) {
            return new Size_t(a.size() * b.size());
        }

        @Override
        public Size_t div(Size_t a, Size_t b) {
            return new Size_t(a.size() / b.size());
        }
    }
}