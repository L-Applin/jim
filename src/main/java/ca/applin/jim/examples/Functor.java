package ca.applin.jim.examples;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Functor {

    // is it required? I think so .....
    interface Kind2<A, B> { }

    // for demo purpose
    record Maybe<T>(T elem)        implements Kind2<T, Maybe<T>> { }
    record FList<T>(List<T> elems) implements Kind2<T, FList<T>> { }

    Map<Class<?>, FunctorImpl<?, ?>> implementations = Map.of(
            Maybe.class, new $Impl__Functor__Maybe<>(),
            FList.class, new $Impl__Functor__FList<>()
    );

    @SuppressWarnings("unchecked")
    static <T extends FunctorImpl<?, ?>> T ofClass(Class<?> cls) {
       return (T) implementations.get(cls);
    }

    interface FunctorImpl<A, B extends Kind2<A, ?>> {
        <C> Kind2<C, ?> fmap(Function<A, C> f, B toMap);
    }

    class $Impl__Functor__Maybe<T> implements FunctorImpl<T, Maybe<T>> {
        public <C> Maybe<C> fmap(Function<T, C> f, Maybe<T> toMap) {
            return new Maybe<>(f.apply(toMap.elem()));
        }
    }

    class $Impl__Functor__FList<T> implements FunctorImpl<T, FList<T>> {
        public <C> FList<C> fmap(Function<T, C> f, FList<T> toMap) {
            return new FList<>(toMap.elems().stream().map(f).toList());
        }
    }

    // demo...
    static void main(String[] args) {
//      ms := Just("inside");
//      mi := fmap(str -> str.len, ms);
//      print(mi);
//      would become:
        Maybe<String> ms = new Maybe<>("inside");
        $Impl__Functor__Maybe<String> impl = Functor.ofClass(Maybe.class);
        Maybe<Integer> mi = impl.fmap(String::length, ms);
        System.out.println(mi);

        FList<String> strs = new FList<>(List.of("Hello", ", ", "world!"));
        $Impl__Functor__FList<String> flistStrImpl = Functor.ofClass(FList.class);
        $Impl__Functor__FList<Integer> mInt = Functor.ofClass(FList.class);
        FList<Integer> fi = flistStrImpl.fmap(String::length, strs);
        FList<String> fstr = mInt.fmap(i -> (i + " ").repeat(i), new FList<>(List.of(12)));
        System.out.println(fi);
        System.out.println(fstr);
    }

}

