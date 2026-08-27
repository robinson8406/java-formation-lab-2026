package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        String trackingId = generator.generate("BOG", "MED");
        assertNotNull(trackingId);
        assertTrue(trackingId.matches("BOG-MED-[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(null, "MED"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination es nulo")
    void shouldThrowWhenDestinationIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate("BOG", null));
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin está vacío")
    void shouldThrowWhenOriginIsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate("", "MED"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination está vacío")
    void shouldThrowWhenDestinationIsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate("BOG", ""));
    }
}