package com.muratj.noorm;

/** A SELECT statement has been opened; choose the table or swap in a COUNT
 * projection. */
public interface SelectStep {

    /** Names the queried table; hostile identifiers are rejected.
     * @throws IllegalArgumentException if the name is not a safe identifier */
    FromStep from(String table);

    /** Replaces the column projection with {@code COUNT(column)} — only legal
     * on an empty {@code select()} (columns not yet chosen); cannot combine
     * with DISTINCT.
     * @throws IllegalStateException when columns are already chosen
     * @throws IllegalArgumentException when the column name is not a safe identifier */
    SelectStep count(String column);

    /** Replaces the column projection with {@code COUNT(*)} — same rules as
     * {@link #count(String)}. */
    SelectStep countAll();
}
