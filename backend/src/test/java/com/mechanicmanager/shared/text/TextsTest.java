package com.mechanicmanager.shared.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TextsTest {

    @Test
    void trimToNullReturnsNullForNull() {
        assertNull(Texts.trimToNull(null));
    }

    @Test
    void trimToNullReturnsNullForEmptyOrBlank() {
        assertNull(Texts.trimToNull(""));
        assertNull(Texts.trimToNull("   "));
    }

    @Test
    void trimToNullTrimsSurroundingSpaces() {
        assertEquals("hola mundo", Texts.trimToNull("  hola mundo "));
    }
}
