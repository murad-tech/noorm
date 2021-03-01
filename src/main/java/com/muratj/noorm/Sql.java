package com.muratj.noorm;

/**
 * Entry point: fluent SELECT chain producing SQL strings.
 */
public final class Sql {

    /** Starts a SELECT; an empty column list defaults to all columns ({@code *}). */
    public SelectStep select(String... columns) {
        return Query.withColumns(columns.length == 0
                ? "*"
                : String.join(", ", columns));
    }
}
