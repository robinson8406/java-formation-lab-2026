package com.indra.logistics;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrackingIdGeneratorTest {

    private final TrackingIdGenerator generator = new TrackingIdGenerator();

    @Test
    @DisplayName("El ID generado debe tener el formato ORIG-DEST-XXXXXXXX")
    void shouldGenerateIdWithCorrectFormat() throws Exception {
    	String idGen = generator.generate("BOG", "MED");
    	
    	assertTrue(idGen.matches("BOG-MED-[A-Z0-9]{8}"), "Formato esperado ok"+ idGen);
    	
    }

    @Test
    @DisplayName("Debe lanzar excepción si origin es nulo")
    void shouldThrowWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, ()->generator.generate(null, "MED"));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción si destination es nulo")
    void shouldThrowWhenDestinationIsNull() {
        assertThrows(IllegalArgumentException.class, ()->generator.generate("BOG", null));
    }
    
    @DisplayName("Test parametrizado GENERATE correcto sin errores")
    @ParameterizedTest
    @CsvSource({"MED,BOG", "CAR,BOG", "MED,CAR"})
    void parameterizedGenTest(String origin, String destination) throws Exception {

    	String idGen = generator.generate(origin, destination);
    	
    	assertTrue(idGen.matches(origin+"-"+destination+"-[A-Z0-9]{8}"), "Formato esperado ok "+ idGen);
    }
}