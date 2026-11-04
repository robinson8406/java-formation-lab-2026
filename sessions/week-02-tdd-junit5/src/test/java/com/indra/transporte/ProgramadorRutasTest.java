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

    @Nested
    @DisplayName("Cuando consultamos horarios por tipo de bus")
    class CuandoConsultamosHorariosPorTipoBus {

        @Test
        @DisplayName("Debe devolver los horarios del tipo solicitado")
        void debeDevolverLosHorariosDelTipoSolicitado() {
            Bus busElectrico = new Bus("ABC123", "Electric");
            Bus busDiesel = new Bus("XYZ999", "Diesel");
            Ruta rutaElectrica1 = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Ruta rutaElectrica2 = new Ruta("Electric", "R010", "Ciudad C", "Ciudad D");
            Ruta rutaGeneral = new Ruta("General", "R020", "Ciudad E", "Ciudad F");

            Horario horario1 = new Horario(busElectrico, rutaElectrica1,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
            Horario horario2 = new Horario(busElectrico, rutaElectrica2,
                    java.time.LocalTime.of(12, 0), java.time.LocalTime.of(14, 0));
            Horario horario3 = new Horario(busDiesel, rutaGeneral,
                    java.time.LocalTime.of(9, 0), java.time.LocalTime.of(11, 0));

            programador.programar(horario1);
            programador.programar(horario2);
            programador.programar(horario3);

            List<Horario> horarios = programador.consultarHorariosPorTipoBus(busElectrico, "Electric");

            assertEquals(2, horarios.size());
            assertEquals(List.of(horario1, horario2), horarios);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
        void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
            Bus busDesconocido = new Bus("ZZZ000", "Electric");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.consultarHorariosPorTipoBus(busDesconocido, "Electric");
            });

            assertEquals("Bus desconocido", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
            programador.programar(horario);

            UnsupportedTypeException exception = assertThrows(UnsupportedTypeException.class, () -> {
                programador.consultarHorariosPorTipoBus(bus, "Volador");
            });

            assertEquals("Tipo de bus no soportado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cuando el rango del horario es inválido")
    class CuandoElRangoDelHorarioEsInvalido {

        @Test
        @DisplayName("Debe rechazar una hora de llegada menor a la de salida")
        void debeRechazarHorarioRangoInvalido() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(10, 0), java.time.LocalTime.of(8, 0));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("La hora de llegada debe ser mayor a la de salida", exception.getMessage());
        }
    }
}
