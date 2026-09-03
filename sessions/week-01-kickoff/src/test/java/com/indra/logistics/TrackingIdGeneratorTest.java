package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        String trackingId = generator.generate("bog", "med");

        assertTrue(trackingId.matches("^BOG-MED-[A-Z0-9]{8}$"),
                () -> "El ID '" + trackingId + "' no cumple con el formato esperado");
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, "MED"));
    }

    @ParameterizedTest(name = "origin={0}, destination={1}")
    @CsvSource({
            "bog, med",
            "MEX, cdmx",
            "sCL, LiM"
    })
    @DisplayName("Debe generar un ID válido para múltiples combinaciones de origin/destination")
    void shouldGenerateValidIdForMultipleRouteCombinations(String origin, String destination) {
        String trackingId = generator.generate(origin, destination);
        String[] parts = trackingId.split("-");

        assertEquals(3, parts.length);
        assertEquals(origin.toUpperCase(), parts[0]);
        assertEquals(destination.toUpperCase(), parts[1]);
        assertTrue(parts[2].matches("[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si destination es nulo")
    void shouldThrowWhenDestinationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("BOG", null));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si origin es solo espacios en blanco")
    void shouldThrowWhenOriginIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("   ", "MED"));
    }

    @Test
    @DisplayName("Debe recortar espacios al inicio y al final antes de generar el ID")
    void shouldTrimWhitespaceFromOriginAndDestination() {
        String trackingId = generator.generate("  BOG  ", "  MED  ");

        assertTrue(trackingId.matches("^BOG-MED-[A-Z0-9]{8}$"));
    }

    @Test
    @DisplayName("Dos IDs generados para la misma ruta deben ser distintos entre sí")
    void shouldGenerateDifferentIdsForConsecutiveCalls() {
        String firstId = generator.generate("BOG", "MED");
        String secondId = generator.generate("BOG", "MED");

        assertNotEquals(firstId, secondId);
    }
}