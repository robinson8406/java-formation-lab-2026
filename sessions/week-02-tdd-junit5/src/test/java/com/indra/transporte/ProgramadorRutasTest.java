package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
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

            UnsupportedTypeException exception = assertThrows(UnsupportedTypeException.class, () -> {
                programador.consultarHorariosPorTipoBus(bus, "TipoInvalido");
            });

            assertEquals("Tipo de bus no soportado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cuando un bus ya tiene horarios programados")
    class CuandoUnBusYaTieneHorariosProgramados {

        @ParameterizedTest
        @CsvSource({
                "08:00,10:00,08:30,10:30,true",
                "08:00,10:00,10:00,11:00,false",
                "08:00,10:00,09:00,09:30,true",
                "08:00,10:00,10:01,11:00,false",
                "08:00,10:00,07:00,07:30,false"
        })
        @DisplayName("Debe validar si dos horarios se solapan o no")
        void debeValidarSolapamientoDeHorarios(String salidaExistente, String llegadaExistente,
                String salidaNueva, String llegadaNueva, boolean debeSolapar) {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta rutaExistente = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            Ruta rutaNueva = new Ruta("General", "R002", "Ciudad A", "Ciudad C");

            Horario horarioExistente = new Horario(bus, rutaExistente,
                    java.time.LocalTime.parse(salidaExistente), java.time.LocalTime.parse(llegadaExistente));
            Horario horarioNuevo = new Horario(bus, rutaNueva,
                    java.time.LocalTime.parse(salidaNueva), java.time.LocalTime.parse(llegadaNueva));

            programador.programar(horarioExistente);

            if (debeSolapar) {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                    programador.programar(horarioNuevo);
                });
                assertEquals("El horario se solapa con otro ya programado", exception.getMessage());
            } else {
                assertDoesNotThrow(() -> programador.programar(horarioNuevo));
            }
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

    @Nested
    @DisplayName("Cuando el horario es nulo o vacío")
    class CuandoElHorarioEsNuloOInvalido {

        @Test
        @DisplayName("Debe rechazar un horario nulo")
        void debeRechazarHorarioNulo() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(null);
            });

            assertEquals("El horario no puede ser nulo", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar un horario con bus nulo")
        void debeRechazarHorarioConBusNulo() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(new Horario(null, new Ruta("General", "R001", "Ciudad A", "Ciudad B"),
                        java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)));
            });

            assertEquals("El bus no puede ser nulo", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar un horario con ruta nula")
        void debeRechazarHorarioConRutaNula() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(new Horario(new Bus("ABC123", "Diesel"), null,
                        java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)));
            });

            assertEquals("La ruta no puede ser nula", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar un bus con placa vacía")
        void debeRechazarBusConPlacaVacia() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(new Horario(new Bus("   ", "Diesel"),
                        new Ruta("General", "R001", "Ciudad A", "Ciudad B"),
                        java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)));
            });

            assertEquals("La placa del bus no puede estar vacía", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar una ruta con código o ubicaciones vacías")
        void debeRechazarRutaConDatosVacios() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(new Horario(new Bus("ABC123", "Diesel"),
                        new Ruta("General", "   ", "   ", "Ciudad B"),
                        java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)));
            });

            assertEquals("El código de la ruta no puede estar vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar un horario con horas nulas")
        void debeRechazarHorarioConHorasNulas() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(new Horario(new Bus("ABC123", "Diesel"),
                        new Ruta("General", "R001", "Ciudad A", "Ciudad B"),
                        null, java.time.LocalTime.of(10, 0)));
            });

            assertEquals("La hora de salida y la de llegada no pueden ser nulas", exception.getMessage());
        }
    }
}
