# Jim (working title)

Programming langage project to have fun explorating different concept related to type system and functional programming. Compiled to JVM Bytecode.

**Everything subject to change.**

## Inspiration
- Haskell type system, 
- Kotlin/Haskell syntax 
- Various Rust ideas 
- Java (semantics, ecosystem)

## Development milestones
- [x] hello world
- [ ] local variables
- [ ] all control flow:
  - [ ] for
  - [ ] if
  - [ ] case
- [ ] function call
- [ ] type definition
  - [ ] type alise
  - [ ] struct types
  - [ ] sum types
- [ ] type class and implementation
- [ ] self-hosted??????

... and much more

## Examples
Working `Hello World!` in `src/test/resources/Simple.jim`:
- run the test `src/test/java/ca/applin/jim/compiler/CompilerTest.java` which will try to compile `src/test/resources/Simple.jim`
- It should have create `target/generated-test-sources/classes/jim/Simple.class`
- you can run this class file simple by (from root):
```console
cd target/generated-test-sources/classes
java jim.Simple
```


## requirements
**[java 19](https://openjdk.org/projects/jdk/19/)**: Preview features are enabled with maven in the [pom](./pom.xml)

**[jib](https://github.com/L-Applin/jib)**: You can clone the jib repository and `mvn clean install` so that the lib is available in you rlocal maven repository.

## Language Features

- Runs on the JVM, garbage collected and all.

- Core support for functional types:

```
String -> Int
[A] -> Int
(A, B) -> Pair A B
(A -> B, Maybe A) -> Maybe B
(A -> Maybe B, Maybe A) -> Maybe B
```

- Functions clearly show what their type is : 
```
count_numbers :: String -> Int = str -> {
  total: Int = 0; 
  for str {
    if is_numeric(it) total++;
  }
  return total;
}
```

- Type classes (instead of interface):
```
Maybe A :: Type = Just A | Nothing ;

Functor F :: Class {
  fmap :: (A -> B, F A) -> F B ;
}

Maybe A :: Implementation Functor A {
   fmap :: (A -> B, Maybe A) -> Maybe B = 
   (f, ma) -> case ma {
        Just a -> Just(f(a));
        Nothing -> Nothing();
   }
}

maybeStr := Just("Hello, World!);
println(fmap(str -> str.length, maybeStr));
>>> Just(13)
```

- Algebraic data types:
```
// Sum type:
Either A B :: Type = Left A | Right B ; 

// product type (struct):
Pair   A B :: Type = { 
    left: A;
    right: B;
}

Simple_Pair A B :: Type = (A, B) ;

pair   := Pair(left="Oh my!", right=69) // `pair` variable is infered as type 'Pair String Int'
either := Left("Hello!")                // `either` variable  is infered as 'Either String _', not enough info to concluce the type Either second type parameter
simp   := ("Some tuple", 42)            // `simp` variable inferred as type '(String, Int)'

either2: Either String Int      = Left("Hello!")          // now we have wnough info to know that either 2 is of type 'Either String Int'
simp2:   Simple_Pair String Int = ("some other tuple, 69) // explicit, type is known to be 'Simple_Pair String Int'
```

- Recursive Types:
```
Lisp_List A :: Type = Nil | Cons A (List A)
```

##TODOs
- null value??
- Macro? Rust macros are cool...
- Code block?
