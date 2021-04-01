package com.muratj.noorm;

import java.sql.ResultSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** createTable chain produces DDL SQL and runs it. */
@Tag("unit")
class CreateTableTest {

    private static final String URL = "jdbc:h2:mem:noorm-unit;DB_CLOSE_DELAY=-1";

    private DB db;

    @BeforeEach
    void newConnection() {
        db = DB.connect(URL, "sa", "");
    }

    @AfterEach
    void releaseConnection() {
        db.close();
    }

    @Test
    void buildsCreateTableStatement() {
        String sql = db.createTable("users")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .build();
        assertEquals("CREATE TABLE users (name VARCHAR(50), age INTEGER)", sql);
    }

    @Test
    void singleColumnHasNoSeparator() {
        String sql = db.createTable("users")
                .column("id", "INTEGER")
                .build();
        assertEquals("CREATE TABLE users (id INTEGER)", sql);
    }

    @Test
    void hostileTableNameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> db.createTable("users; DROP TABLE users"));
    }

    @Test
    void hostileColumnNameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> db.createTable("users").column("name; --", "VARCHAR(50)"));
    }

    @Test
    void hostileTypeFragmentRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> db.createTable("users").column("name", "VARCHAR(50); DROP TABLE users"));
        assertThrows(IllegalArgumentException.class,
                () -> db.createTable("users").column("name", "-- comment"));
    }

    @Test
    void typeFragmentWithArgumentsAccepted() {
        String sql = db.createTable("users")
                .column("amount", "DECIMAL(10, 2)")
                .column("note", "VARCHAR(255)")
                .build();
        assertEquals("CREATE TABLE users (amount DECIMAL(10, 2), note VARCHAR(255))", sql);
    }

    @Test
    void zeroColumnsCannotBuild() {
        // CreateTableStep does not expose build(); guard holds for misuse
        assertThrows(IllegalStateException.class,
                () -> ((TableQuery) db.createTable("users")).build());
    }

    @Test
    void buildsCreateTableIfNotExistsStatement() {
        String sql = db.createTableIfNotExists("users")
                .column("id", "INTEGER")
                .build();
        assertEquals("CREATE TABLE IF NOT EXISTS users (id INTEGER)", sql);
    }

    @Test
    void createTableIfNotExistsRunsTwice() {
        db.createTableIfNotExists("idempotent_ddl_test")
                .column("id", "INTEGER")
                .execute();
        db.createTableIfNotExists("idempotent_ddl_test")
                .column("id", "INTEGER")
                .execute();
    }

    @Test
    void createTableOnExistingTableNamesTheProblem() {
        db.createTable("double_ddl_test").column("id", "INTEGER").execute();
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> db.createTable("double_ddl_test").column("id", "INTEGER").execute());
        assertTrue(e.getMessage().contains("already exists"), e.getMessage());
    }

    @Test
    void executesAgainstRealDatabase() throws Exception {
        db.createTable("users_ddl_test")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .execute();
        // verification: the table exists — selecting from it succeeds and has no rows
        try (ResultSet rs = db.select("*").from("users_ddl_test").execute()) {
            assertFalse(rs.next());
        }
    }
}
