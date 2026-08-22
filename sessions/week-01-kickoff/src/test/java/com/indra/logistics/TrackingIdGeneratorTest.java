package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
        var id = generator.generate(new GenerateParam("BOG", "MED"));

        assertTrue(id.matches("BOG-MED-[A-Z0-9]{8}"),
                "Formato esperado: BOG-MED-XXXXXXXX, recibido: " + id);
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        var param = new GenerateParam(null, "MED");
        var exception = assertThrows(IllegalArgumentException.class, () -> generator.generate(param));

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si destination es vacío")
    void shouldThrowWhenDestinationIsBlank() {
        var param = new GenerateParam("BOG", "");

        assertThrows(IllegalArgumentException.class, () -> generator.generate(param));
    }

    @Test
    @DisplayName("Dos IDs generados para el mismo origen/destino deben ser distintos")
    void shouldGenerateUniqueIds() {
        var param = new GenerateParam("BOG", "MED");
        var id1 = generator.generate(param);
        var id2 = generator.generate(param);

        assertNotEquals(id1, id2, "Los IDs deben ser únicos");
    }

    // BONUS: test parametrizado con @ParameterizedTest
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Debe lanzar excepción para cualquier valor inválido de origin")
    void shouldThrowForInvalidOrigin(String invalidOrigin) {
        var param = new GenerateParam(invalidOrigin, "MED");

        assertThrows(IllegalArgumentException.class, () -> generator.generate(param));
    }

}