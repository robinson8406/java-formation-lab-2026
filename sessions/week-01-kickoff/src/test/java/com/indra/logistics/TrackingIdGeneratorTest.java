package com.indra.logistics;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        String trackingId = generator.generate("BOG", "MED");

        assertNotNull(trackingId);
        assertTrue(trackingId.matches("^BOG-MED-[A-Z0-9]{8}$"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(null, "MED")
        );

        assertTrue(exception.getMessage().contains("El origen de la ruta no puede ser nulo"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Debe lanzar excepción si origin es nulo, vacío o espacios en blanco")
    void shouldThrowWhenOriginIsInvalid(String invalidOrigin) {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(invalidOrigin, "MED")
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination es nulo")
    void shouldThrowWhenDestinationIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate("BOG", null)
        );

        assertTrue(exception.getMessage().contains("El destino de la ruta no puede ser nulo"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Debe lanzar excepción si destination es nulo, vacío o espacios en blanco")
    void shouldThrowWhenDestinationIsInvalid(String invalidDestination) {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate("BOG", invalidDestination)
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si la ruta es nula")
    void shouldThrowWhenRouteIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> generator.generate((Route) null)
        );
    }

    @Test
    @DisplayName("Debe normalizar códigos de origen y destino a mayúsculas y sin espacios")
    void shouldNormalizeOriginAndDestination() {
        String trackingId = generator.generate("  bog  ", "  med  ");

        assertTrue(trackingId.matches("^BOG-MED-[A-Z0-9]{8}$"));
    }

    @Test
    @DisplayName("Debe generar IDs únicos en invocaciones consecutivas sin colisiones")
    void shouldGenerateUniqueIdsOnConsecutiveInvocations() {
        int totalIds = 1000;
        Set<String> generatedIds = new HashSet<>(totalIds);

        for (int i = 0; i < totalIds; i++) {
            String trackingId = generator.generate("BOG", "MED");
            generatedIds.add(trackingId);
        }

        assertEquals(totalIds, generatedIds.size());
    }

    @ParameterizedTest
    @CsvSource({
            "BOG, MED",
            "CLO, BAQ",
            "MAD, BCN",
            "JFK, LHR"
    })
    @DisplayName("Debe generar IDs válidos para múltiples combinaciones de rutas parametrizadas")
    void shouldGenerateValidIdForMultipleRoutes(String origin, String destination) {
        String trackingId = generator.generate(origin, destination);

        String expectedPrefix = origin + "-" + destination + "-";
        assertTrue(trackingId.startsWith(expectedPrefix));
        assertEquals(expectedPrefix.length() + 8, trackingId.length());
        assertTrue(trackingId.matches("^" + origin + "-" + destination + "-[A-Z0-9]{8}$"));
    }

    @Test
    @DisplayName("Debe soportar generación mediante el Record Route de Java 21")
    void shouldGenerateIdUsingRouteRecord() {
        Route route = new Route("CLO", "CTG");
        String trackingId = generator.generate(route);

        assertTrue(trackingId.matches("^CLO-CTG-[A-Z0-9]{8}$"));
    }
}