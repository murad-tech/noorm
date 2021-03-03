package com.muratj.noorm.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.muratj.noorm.Sql;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs a noorm-built query against a real H2 in-memory database via plain JDBC. */
@Tag("integration")
class SqlIntegrationTest {

    @Test
    void selectsRealRowsFromADatabase() throws Exception {
        String url = "jdbc:h2:mem:noorm-it;DB_CLOSE_DELAY=-1";

        // seed with raw SQL: noorm v0.1 builds reads only
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE users (name VARCHAR(50), age INTEGER)");
            stmt.executeUpdate("INSERT INTO users VALUES ('john', 30), ('anne', 42)");
        }

        String sql = new Sql().select("name", "age").from("users").build();

        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals("john", rs.getString("name"));
            assertEquals(30, rs.getInt("age"));
            assertTrue(rs.next());
            assertEquals("anne", rs.getString("name"));
            assertEquals(42, rs.getInt("age"));
            assertFalse(rs.next());
        }
    }
}
