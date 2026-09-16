package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() {
       
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
       
    }
}