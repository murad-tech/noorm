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

/** selectDistinct and count projections. */
@Tag("unit")
class DistinctCountTest {

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
    void selectDistinctBuildsDistinctProjection() {
        String sql = db.selectDistinct("name", "age").from("users").build();
        assertEquals("SELECT DISTINCT name, age FROM users", sql);
    }

    @Test
    void emptySelectDistinctDefaultsToStar() {
        String sql = db.selectDistinct().from("users").build();
        assertEquals("SELECT DISTINCT * FROM users", sql);
    }

    @Test
    void countBuildsCountFunction() {
        String sql = db.select().count("id").from("users").build();
        assertEquals("SELECT COUNT(id) FROM users", sql);
    }

    @Test
    void countAllBuildsCountStar() {
        String sql = db.select().countAll().from("users").build();
        assertEquals("SELECT COUNT(*) FROM users", sql);
    }

    @Test
    void countAfterColumnsThrows() {
        assertThrows(IllegalStateException.class, () ->
                db.select("name").count("id"));
    }

    @Test
    void secondCountThrows() {
        assertThrows(IllegalStateException.class, () ->
                db.select().count("id").countAll());
    }

    @Test
    void countValidatesColumnIdentifier() {
        assertThrows(IllegalArgumentException.class, () ->
                db.select().count("id; --"));
    }

    @Test
    void countOnDistinctThrows() {
        assertThrows(IllegalStateException.class, () ->
                db.selectDistinct().count("id"));
    }

    @Test
    void executesCountRows() throws Exception {
        db.createTable("counter_test")
                .column("name", "VARCHAR(50)")
                .column("age", "INTEGER")
                .execute();
        db.insertInto("counter_test").value("name", "john").value("age", 30).execute();
        db.insertInto("counter_test").value("name", "ann").value("age", 42).execute();
        db.insertInto("counter_test").value("name", "fred").value("age", 22).execute();

        try (ResultSet rs = db.select().countAll().from("counter_test").execute()) {
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
            assertFalse(rs.next());
        }
    }

    @Test
    void executesDistinctDeduplication() throws Exception {
        db.createTable("distinct_test")
                .column("name", "VARCHAR(50)")
                .execute();
        db.insertInto("distinct_test").value("name", "john").execute();
        db.insertInto("distinct_test").value("name", "john").execute();
        db.insertInto("distinct_test").value("name", "ann").execute();

        try (ResultSet rs = db.selectDistinct("name").from("distinct_test").execute()) {
            assertTrue(rs.next());
            assertEquals("ann", rs.getString(1));
            assertTrue(rs.next());
            assertEquals("john", rs.getString(1));
            assertFalse(rs.next());
        }
    }
}
