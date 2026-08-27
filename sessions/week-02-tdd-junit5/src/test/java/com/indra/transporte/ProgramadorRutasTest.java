package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.indra.transporte.model.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;

class ProgramadorRutasTest {

    private final ProgramadorRutas programador = new ProgramadorRutasImpl();

    @Test
    @DisplayName("Debe registrar un horario")
    void debeRegistrarUnHorario() {
        var bus = new Bus("ABC123", Type.DIESEL);
        var ruta = new Ruta("Electric","R001", "Ciudad A", "Ciudad B");
        var horario = new Horario(bus, ruta,
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
            var bus = new Bus("ABC123", Type.ELECTRIC);
            var ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            var horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.debeValidarTipoRutasYBuses(horario);
            });

            assertEquals("Los buses eléctricos solo pueden ir a rutas eléctricas", exception.getMessage());
        }

        @Test
        @DisplayName("Debe permitir rutas eléctricas")
        void debePermitirRutasElectricas() {
            var bus = new Bus("ABC123", Type.ELECTRIC);
            var ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            var horario = new Horario(bus, ruta,
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
            Bus bus = new Bus("ABC123", Type.DIESEL);
            Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

            assertDoesNotThrow(() -> programador.debeValidarTipoRutasYBuses(horario));
        }
    }

    @Test
    @DisplayName("Debe devolver los horarios del tipo solicitado")
    void debeDevolverLosHorariosDelTipoSolicitado() {
        var busElectrico = new Bus("ABC987", Type.ELECTRIC);
        var busDiesel = new Bus("ABC123", Type.DIESEL);
        var rutaElectrica = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
        var rutaDiesel = new Ruta("General", "R002", "Ciudad C", "Ciudad D");
        var horarioElectrico = new Horario(busElectrico,
                rutaElectrica,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(12, 0));
        var horarioDiesel = new Horario(busDiesel,
                rutaDiesel,
                java.time.LocalTime.of(11, 0), java.time.LocalTime.of(14, 0));

        programador.programar(horarioElectrico);
        programador.programar(horarioDiesel);

        assertEquals(java.util.List.of(horarioElectrico),
                programador.consultarHorariosPorTipoBus(busElectrico, Type.ELECTRIC));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
    void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
        var busProgramado = new Bus("ABC123", Type.ELECTRIC);
        var busDesconocido = new Bus("ABC999", Type.ELECTRIC);
        var horario = new Horario(busProgramado,
                new Ruta("Electric", "R001", "Ciudad A", "Ciudad B"),
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        programador.programar(horario);

        assertThrows(IllegalArgumentException.class, () ->
                programador.consultarHorariosPorTipoBus(busDesconocido, Type.ELECTRIC));
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
    void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
        var bus = new Bus("ABC123", Type.ELECTRIC);
        var ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
        var horario = new Horario(bus,
                ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        programador.programar(horario);

        assertThrows(com.indra.transporte.exception.UnsupportedTypeException.class, () ->
                programador.consultarHorariosPorTipoBus(bus, Type.HYBRID));
    }

    @Test
    @DisplayName("Debe rechazar horario solapado para el mismo bus")
    void debeRechazarHorarioSolapado() {
        var bus = new Bus("ABC123", Type.DIESEL);
        var ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        var horarioExistente = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
        var horarioSolapado = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 30), java.time.LocalTime.of(10, 30));

        programador.programar(horarioExistente);

        var exception = assertThrows(IllegalArgumentException.class, () -> programador.programar(horarioSolapado));
        assertEquals("El horario se solapa con otro ya programado para el bus", exception.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar horario con rango inválido")
    void debeRechazarHorarioRangoInvalido() {
        var bus = new Bus("ABC123", Type.DIESEL);
        var ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        var horarioInvalido = new Horario(bus, ruta,
                java.time.LocalTime.of(10, 0), java.time.LocalTime.of(8, 0));

        var exception = assertThrows(IllegalArgumentException.class, () -> programador.programar(horarioInvalido));
        assertEquals("La hora de llegada no puede ser menor o igual a la hora de salida", exception.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar parámetros nulos del horario")
    void debeRechazarParametrosNulosDelHorario() {
        var ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        var horarioSinBus = new Horario(null, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        var exception = assertThrows(IllegalArgumentException.class, () -> programador.programar(horarioSinBus));
        assertEquals("Bus, ruta y horas del horario son obligatorios", exception.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar parámetros vacíos del horario")
    void debeRechazarParametrosVaciosDelHorario() {
        var bus = new Bus("ABC123", Type.DIESEL);
        var rutaConTipoVacio = new Ruta("", "R001", "Ciudad A", "Ciudad B");
        var horario = new Horario(bus, rutaConTipoVacio,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        var exception = assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
        assertEquals("Los textos del bus y la ruta no pueden ser nulos o vacíos", exception.getMessage());
    }

}
