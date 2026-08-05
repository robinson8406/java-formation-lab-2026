package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        String trackingId = assertDoesNotThrow(() -> generator.generate("BOG", "MED"));
        assertTrue(trackingId.matches("BOG-MED-[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, "MED"));
    }
}