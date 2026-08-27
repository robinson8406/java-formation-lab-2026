package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Debe devolver los horarios del tipo solicitado")
    void debeDevolverLosHorariosDelTipoSolicitado() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
        programador.programar(horario);

        var resultado = programador.consultarHorariosPorTipoBus(bus, "General");

        assertEquals(1, resultado.size());
        assertEquals(horario, resultado.get(0));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
    void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
        Bus busRegistrado = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(busRegistrado, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
        programador.programar(horario);
        Bus busDesconocido = new Bus("XYZ999", "Diesel");

        assertThrows(IllegalArgumentException.class,
                () -> programador.consultarHorariosPorTipoBus(busDesconocido, "General"));
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
    void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
        fail("Implementar este test para lanzar UnsupportedTypeException cuando el tipo es desconocido");
    }
}
