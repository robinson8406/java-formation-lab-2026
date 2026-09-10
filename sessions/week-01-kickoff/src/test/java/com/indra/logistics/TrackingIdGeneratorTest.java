package com.indra.logistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrackingIdGeneratorTest")
public class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();
    private static final String ID_REGEX_PATTERN = "[A-Z0-9]{8}";

    @Test
    @DisplayName("Valid generate")
    void testValidGenerate() {
        String origin = "ORI";
        String destination = "DEST";
        String result = generator.generate(origin, destination);
        
        assertTrue(result.startsWith(origin + "-" + destination + "-"));
        assertTrue(result.length() >= origin.length() + destination.length() + 1 + 8);
        String expectedPattern = origin + "-" + destination + "-" + ID_REGEX_PATTERN;
        assertTrue(result.matches(expectedPattern), String.format("ID format mismatch\nExpected: %s\nActual: %s", expectedPattern, result));
    }

    @Test
    @DisplayName("Null origin throws exception")
    void testNullOriginThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(null, "DEST");
        });
    }

    @Test
    @DisplayName("Empty origin throws exception")
    void testEmptyOriginThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("", "DEST");
        });
    }

    @Test
    @DisplayName("Null destination throws exception")
    void testNullDestinationThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("ORI", null);
        });
    }

    @Test
    @DisplayName("Empty destination throws exception")
    void testEmptyDestinationThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("ORI", "");
        });
    }

    @Test
    @DisplayName("Both null parameters throw exception")
    void testBothNullParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(null, null);
        });
    }

    @Test
    @DisplayName("Both empty parameters throw exception")
    void testBothEmptyParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("", "");
        });
    }
}