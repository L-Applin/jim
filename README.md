# Jim (working title)

Programming langage project to have fun explorating different concept related to type system and functional programming. Compiled to JVM Bytecode.

Haskell type system, Kotlin/Haskell syntax, Java performance, semantics and ecosystem

**Everything subject to change.**

## stages
- [x] hello world
- [ ] local variables
- [ ] all control flow:
  - [ ] for
  - [ ] if
  - [ ] case
... and much more

## Examples
Working Helllo world:
- run the test `src/test/java/ca/applin/jim/compiler/CompilerTest.java` which will try to compile `src/test/resources/Simple.jim`
- It should have create `target/generated-test-sources/classes/jim/Simple.class`
- you can run this class file simple by (from root):
```console
cd target/generated-test-sources/classes
java jim.Simple
```

## requirements
[ji](https://github.com/L-Applin/jib) is required. You can clone the jib repository and `mvn clean install` so that the lib is available to locally.

## Features
Core support for functional types:

```
String -> Int
[A] -> Int
(A, B) -> Pair A B
(A -> B, Maybe A) -> Maybe B
(A -> Maybe B, Maybe A) -> Maybe B
```

Functions should clearly show what their type is : 
```
count_numbers :: String -> Int = str -> {
  total: Int = 0; 
  for str {
    if is_numeric(it) total++;
  }
  return total;
}
```

Type classes (instead of interface):

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

Core support for algebraic types:
```
Either A B :: Type = Left A | Right B ; // Sum type
Pair   A B :: Type = { // product type
    left: A;
    right: B;
}
Simple_Pair A B :: Type = (A, B) ;

pair := Pair(left="Oh my!", right=69) // pair variable is infered as type 'Pair String Int'
either := Left("Hello!") // either is infered as 'Either String _', not enough info to concluce the type Either second type parameter
either2: Either String Int = Left("Hello!") // now we have wnough info to know that either 2 is of type 'Either String Int'
simp := ("Some tuple", 42) // inferred as type '(String, Int)'
simp2: Simple_Pair String Int = ("some other tuple, 69) // explicitly type is known to be 'Simple_Pair String Int'
```

Recursive Types:
```
Lisp_List A :: Type = Nil | Cons A (List A)
```

##TODOs
- null value??
- Macro? Rust macros are cool...
- Code block?
