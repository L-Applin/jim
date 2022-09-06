package ca.applin.jim.parser;

import java.util.Arrays;

public enum AtKeyword {
    IMPORT("import"),
    UNKNOWN("");

    public final String value;
    AtKeyword(String value) {
        this.value = value;
    }

    public static AtKeyword fromString(String str) {
        return Arrays.stream(AtKeyword.values()).filter(key -> key.value.equals(str))
                .findAny().orElse(UNKNOWN);
    }

}
