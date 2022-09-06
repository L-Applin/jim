package ca.applin.jim.lexer;

import java.util.Set;

public class SpecialChars {

    public static final SpecialChars DEFAULT_SPECIAL_CHARS = new SpecialChars(
            Set.of('(',')','[', ']', '{','}','=','+','-','*','/','%','<','>','^','|',
                    '&',',','.','!','@','#','$','~','`','?',':',';','"','\'', '\\', '\n'),
            Set.of('\t', ' ')
    );

    private Set<Character> chars, skippable;

    public SpecialChars(Set<Character> chars, Set<Character> skippable) {
        this.chars = chars;
        this.skippable = skippable;
    }

    public boolean isSpecial(char c) {
        return chars.contains(c);
    }


    public boolean isSkippable(char c) {
        return skippable.contains(c);
    }

}
