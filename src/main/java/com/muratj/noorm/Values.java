package com.muratj.noorm;

/** Formats literals so the generated SQL is safe against quote injection. */
final class Values {

    private Values() {
    }

    static String literal(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String) {
            return quote((String) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
