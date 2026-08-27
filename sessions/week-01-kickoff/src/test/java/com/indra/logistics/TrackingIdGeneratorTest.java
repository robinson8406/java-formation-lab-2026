package com.indra.logistics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {

        String id = generator.generate("BOG", "MED");
        assertNotNull(id);
        assertTrue(id.matches("BOG-MED-[A-Z0-9]{8}"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        
        assertThrows(IllegalArgumentException.class, () -> generator.generate("", ""));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("", "MED"));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, "MED"));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("BOG", null));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, null));

    }
}