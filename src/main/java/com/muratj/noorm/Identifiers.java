package com.muratj.noorm;

import java.util.regex.Pattern;

/**
 * Validates identifiers before they can reach a SQL string: hostile input
 * (quotes, semicolons, comments, whitespace) throws instead of being rendered.
 */
public final class Identifiers {

    private static final Pattern SEGMENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_$]*");

    private Identifiers() {
    }

    /** Returns identifier unchanged if safe, otherwise throws. The bare {@code "*"} is allowed. */
    public static String validate(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier must be non-empty");
        }
        if (identifier.equals("*")) {
            return "*";
        }
        for (String segment : identifier.split("\\.", -1)) {
            if (!SEGMENT.matcher(segment).matches()) {
                throw new IllegalArgumentException("Not a safe identifier: '" + identifier + "'");
            }
        }
        return identifier;
    }
}
