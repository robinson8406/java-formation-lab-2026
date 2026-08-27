package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        String trackingId = generator.generate("BOG", "MED");

        assertTrue(trackingId.matches("BOG-MED-[A-Z0-9]{8}"));
    }

    @ParameterizedTest(name = "Debe rechazar origin={0}, destination={1}")
    @MethodSource("invalidLocations")
    void shouldThrowWhenLocationIsNullOrBlank(String origin, String destination) {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(origin, destination));
    }

    @Test
    @DisplayName("Debe conservar los códigos de origen y destino")
    void shouldKeepLocationCodesInGeneratedId() {
        String trackingId = generator.generate("BOG", "MED");

        assertEquals("BOG", trackingId.substring(0, 3));
        assertEquals("MED", trackingId.substring(4, 7));
    }

    private static Stream<Arguments> invalidLocations() {
        return Stream.of(
                arguments(null, "MED"),
                arguments("", "MED"),
                arguments("   ", "MED"),
                arguments("BOG", null),
                arguments("BOG", ""),
                arguments("BOG", "   ")
        );
    }
}