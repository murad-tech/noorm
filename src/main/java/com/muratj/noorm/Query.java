package com.muratj.noorm;

/** Immutable chain state; validation scope grows with later tasks. */
final class Query implements SelectStep, FromStep {

    private final String projection;
    private final String table;

    private Query(String projection, String table) {
        this.projection = projection;
        this.table = table;
    }

    static Query withColumns(String projection) {
        return new Query(projection, null);
    }

    @Override
    public FromStep from(String table) {
        return new Query(projection, Identifiers.validate(table));
    }

    @Override
    public String build() {
        return "SELECT " + projection + " FROM " + table;
    }
}
