package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @ParameterizedTest(name = "Ruta: {0} -> {1}")
    @CsvSource({
            "BOG, MED",
            "caL, BOg",
            "  PaS  ,  PpY  ",
            "cal, std",
            "bog, MED",
            "Gua,  med"
    })
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat(String origin, String destination) {
        String trackingId = generator.generate(origin, destination);

        String expectedOrigin = origin.trim().toUpperCase();
        String expectedDestination = destination.trim().toUpperCase();

        String fortmatRegex = String.format("%s-%s-[A-Z0-9]{8}$", expectedOrigin, expectedDestination);

        assertTrue(trackingId.matches(fortmatRegex), "El ID generado " + trackingId + " no tiene el formato esperado");
    }

    @ParameterizedTest(name = "Origen inválido: ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull(String invalidOrigin) {
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(invalidOrigin, "MED");
        });

        assertEquals(TrackingRoute.ERROR_ORIGIN_INVALID, exception.getMessage());

    }

    @ParameterizedTest(name = "Destino inválido: ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ",  ""})
    @DisplayName("Debe lanzar excepción si destination es nulo o vacio")
    void shouldThrowWhenDestinationIsNull(String invalidDestination) {
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("BOG", invalidDestination);
        });

        assertEquals(TrackingRoute.ERROR_DESTINATION_INVALID, exception.getMessage());

    }
}