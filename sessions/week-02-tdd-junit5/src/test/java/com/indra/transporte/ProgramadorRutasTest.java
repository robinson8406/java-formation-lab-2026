package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;

public class ProgramadorRutasTest {
    private final ProgramadorRutas programador = new ProgramadorRutas();

    @Test
    @DisplayName("Debe registrar un horario")
    void debeRegistrarUnHorario() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("Electric","R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        programador.programar(horario);

        assertEquals(1, programador.getHorarios().size());
    }

    @Nested
    @DisplayName("Cuando el bus es eléctrico")
    class CuandoBusEsElectrico {

        @Test
        @DisplayName("Debe rechazar rutas no eléctricas")
        void debeRechazarRutasNoElectricas() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.debeValidarTipoRutasYBuses(horario);
            });

            assertEquals("Los buses eléctricos solo pueden ir a rutas eléctricas", exception.getMessage());
        }

        @Test
        @DisplayName("Debe permitir rutas eléctricas")
        void debePermitirRutasElectricas() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

            assertDoesNotThrow(() -> programador.debeValidarTipoRutasYBuses(horario));
        }
    }

    @Nested
    @DisplayName("Cuando el bus no es eléctrico")
    class CuandoBusNoEsElectrico {

        @Test
        @DisplayName("Debe permitir cualquier tipo de ruta")
        void debePermitirCualquierTipoDeRuta() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

            assertDoesNotThrow(() -> programador.debeValidarTipoRutasYBuses(horario));
        }
    }

    @Test
    @DisplayName("Debe devolver los horarios del tipo de ruta solicitado")
    void debeDevolverLosHorariosDelTipoSolicitado() {
        // GIVEN — dos horarios eléctricos y uno general
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta rutaElectrica = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
        Ruta rutaGeneral   = new Ruta("General",  "R002", "Ciudad C", "Ciudad D");

        Horario h1 = new Horario(bus, rutaElectrica, java.time.LocalTime.of(8, 0),  java.time.LocalTime.of(10, 0));
        Horario h2 = new Horario(bus, rutaElectrica, java.time.LocalTime.of(12, 0), java.time.LocalTime.of(14, 0));
        Horario h3 = new Horario(bus, rutaGeneral,   java.time.LocalTime.of(16, 0), java.time.LocalTime.of(18, 0));

        programador.programar(h1);
        programador.programar(h2);
        programador.programar(h3);

        // WHEN
        List<Horario> resultado = programador.getHorariosPorTipo("Electric");

        // THEN
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(h -> "Electric".equals(h.getRuta().getTipo())));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el tipo de bus es desconocido")
    void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
        // GIVEN — bus con tipo no reconocido
        Bus bus = new Bus("XYZ999", "Hydrogen");
        Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                programador.debeValidarTipoRutasYBuses(horario));
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo de ruta es desconocido")
    void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
        // GIVEN — ruta con tipo no reconocido
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("Submarine", "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        // WHEN / THEN
        assertThrows(UnsupportedTypeException.class, () ->
                programador.debeValidarTipoRutasYBuses(horario));
    }
}
