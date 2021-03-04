package com.muratj.noorm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Entry point of the noorm query builder. {@link #connect} opens the JDBC
 * connection the caller supplies; config files stay the user's business.
 * The connection stays open until {@link #close()}, so a ResultSet returned
 * by {@code execute()} stays readable.
 */
public final class DB implements AutoCloseable {

    private final Connection connection;

    private DB(Connection connection) {
        this.connection = connection;
    }

    /** Opens a JDBC connection to the given URL.
     * @throws IllegalStateException when the connection cannot be opened */
    public static DB connect(String url, String username, String password) {
        try {
            return new DB(DriverManager.getConnection(url, username, password));
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to " + url, e);
        }
    }

    /** Starts a SELECT; an empty column list defaults to all columns ({@code *}). */
    public SelectStep select(String... columns) {
        return Query.withColumns(columns.length == 0 ? "*" : String.join(", ", columns), connection);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot close connection", e);
        }
    }
}
