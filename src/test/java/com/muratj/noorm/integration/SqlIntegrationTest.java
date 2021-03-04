package com.muratj.noorm.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.muratj.noorm.DB;

/** A query built through noorm runs against a real H2 database via its own connection. */
@Tag("integration")
class SqlIntegrationTest {

    private static final String URL = "jdbc:h2:mem:noorm-it;DB_CLOSE_DELAY=-1";
    private Connection seedConnection;
    private Statement seedStatement;

    @BeforeEach
    void seedUsers() throws SQLException {
        // seed with raw SQL: noorm builds reads only
        seedConnection = DriverManager.getConnection(URL, "sa", "");
        seedStatement = seedConnection.createStatement();
        seedStatement.execute("CREATE TABLE users (name VARCHAR(50), age INTEGER)");
        seedStatement.execute("INSERT INTO users VALUES ('john', 30), ('ann', 42)");
    }

    @AfterEach
    void teardownSeed() throws SQLException {
        seedStatement.close();
        seedConnection.close();
    }

    @Test
    void selectExecuteReturnsSeededRows() throws SQLException {
        try (DB db = DB.connect(URL, "sa", "");
             ResultSet rs = db.select("name", "age").from("users").execute()) {
            assertTrue(rs.next());
            assertEquals("john", rs.getString("name"));
            assertEquals(30, rs.getInt("age"));
            assertTrue(rs.next());
            assertEquals("ann", rs.getString("name"));
            assertEquals(42, rs.getInt("age"));
            assertFalse(rs.next());
        }
    }

}
