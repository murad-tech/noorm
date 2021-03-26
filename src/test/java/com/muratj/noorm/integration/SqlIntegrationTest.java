package com.muratj.noorm.integration;

import java.sql.ResultSet;

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

    @BeforeEach
    void seedUsers() {
        try (DB seed = DB.connect(URL, "sa", "")) {
            seed.createTableIfNotExists("users")
                    .column("name", "VARCHAR(50)")
                    .column("age", "INTEGER")
                    .execute();
            seed.insertInto("users")
                    .value("name", "john")
                    .value("age", 30)
                    .execute();
            seed.insertInto("users")
                    .value("name", "ann")
                    .value("age", 42)
                    .execute();
        }
    }

    @Test
    void selectExecuteReturnsSeededRows() throws Exception {
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
