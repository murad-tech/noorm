package com.muratj.noorm;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Spec boundary: hostile identifiers never reach the SQL string. */
@Tag("unit")
class IdentifiersTest {

    @Test
    void acceptsSimpleIdentifier() {
        assertEquals("users", Identifiers.validate("users"));
    }

    @Test
    void acceptsQualifiedNames() {
        assertEquals("u.name", Identifiers.validate("u.name"));
    }

    @Test
    void acceptsOnlyTheExactBareStar() {
        assertEquals("*", Identifiers.validate("*"));
    }

    @Test
    void rejectsSemicolonAndCommentInjection() {
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate("users; DROP TABLE users"));
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate("users--"));
    }

    @Test
    void rejectsQuotesSpacesAndLeadingDigit() {
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate("u'sers"));
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate("users name"));
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate("7lives"));
    }

    @Test
    void rejectsEmptyAndNull() {
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate(""));
        assertThrows(IllegalArgumentException.class, () -> Identifiers.validate(null));
    }
}
