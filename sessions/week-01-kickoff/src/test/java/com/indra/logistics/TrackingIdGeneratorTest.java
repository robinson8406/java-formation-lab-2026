package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @ParameterizedTest
    @CsvSource({"BOG, MED", "CLO, BGA", "bog, med"})
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat(String origin, String destination) {
        String trackingId = generator.generate(origin, destination);

        assertTrue(trackingId.matches(origin.toUpperCase() + "-"
                + destination.toUpperCase() + "-[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si algún parámetro es nulo o vacío")
    void shouldThrowWhenParameterIsInvalid() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> generator.generate(null, "MED")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> generator.generate("BOG", "")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> generator.generate(" ", "MED"))
        );
    }
}