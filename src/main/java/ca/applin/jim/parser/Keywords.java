package ca.applin.jim.parser;

import java.util.Arrays;
import java.util.stream.Stream;

public enum Keywords {
    IF("if"),
    WHILE("while"),
    FOR("for"),
    CASE("case"),

    TYPE("Type"),
    CLASS("Class"),
    IMPLEMENTATION("Implementation"),

    THIS("this"),
    RETURN("return"),
    STATIC("static"),

    UNKNOWN("");

    public final String value;
    Keywords(String value) {
        this.value = value;
    }

    public static Keywords fromString(String str) {
        return Arrays.stream(Keywords.values()).filter(keyw -> keyw.value.equals(str))
                .findAny().orElse(UNKNOWN);
    }

}
