package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @ParameterizedTest
    @CsvSource({
            "MED, BOG",
            "BOG, MED"
    })
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat(String origin, String destination) {
        String trackingId = generator.generate(origin, destination);

        assertNotNull(trackingId, "El ID de seguimiento no debe ser nulo");
        assertTrue(trackingId.matches("^[A-Z]{3}-[A-Z]{3}-[A-Z0-9]{8}$"),
                "El ID de seguimiento debe tener el formato ORIG-DEST-XXXXXXXX");
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, "MED"),
                "Debe lanzar IllegalArgumentException si origin es nulo");
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination es nulo")
    void shouldThrowWhenDestinationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("BOG", null),
                "Debe lanzar IllegalArgumentException si destination es nulo");
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es vacío")
    void shouldThrowWhenOriginIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("", "MED"),
                "Debe lanzar IllegalArgumentException si origin es vacío");
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination es vacío")
    void shouldThrowWhenDestinationIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("BOG", ""),
                "Debe lanzar IllegalArgumentException si destination es vacío");
    }
}