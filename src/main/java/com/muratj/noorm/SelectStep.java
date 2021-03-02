package com.muratj.noorm;

/** A SELECT statement has been opened; only the name of the queried table is legal next. */
public interface SelectStep {

    /** Names the queried table; hostile identifiers are rejected.
     * @throws IllegalArgumentException if the name is not a safe identifier */
    FromStep from(String table);
}
