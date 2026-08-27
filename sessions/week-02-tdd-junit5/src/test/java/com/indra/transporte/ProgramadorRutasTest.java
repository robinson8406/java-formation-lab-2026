package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;
import com.indra.transporte.exception.UnsupportedTypeException;

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
    @DisplayName("Al consultar horarios por tipo de turno")
    class AlConsultarHorariosPorTipoBus {

        private final Bus bus = new Bus("ABC123", "Diesel");
        private final Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");

        @Test
        @DisplayName("Debe devolver únicamente los horarios del turno solicitado")
        void debeDevolverLosHorariosDelTipoSolicitado() {
            Horario manana = new Horario(bus, ruta, LocalTime.of(6, 0), LocalTime.of(8, 0));
            Horario tarde = new Horario(bus, ruta, LocalTime.of(14, 0), LocalTime.of(16, 0));
            programador.programar(manana);
            programador.programar(tarde);

            List<Horario> resultado = programador.consultarHorariosPorTipoBus(bus, "MANANA");

            assertEquals(1, resultado.size());
            assertTrue(resultado.contains(manana));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
        void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
            Bus busDesconocido = new Bus("ZZZ999", "Diesel");

            assertThrows(IllegalArgumentException.class,
                    () -> programador.consultarHorariosPorTipoBus(busDesconocido, "MANANA"));
        }

        @Test
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo de turno es desconocido")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
            programador.programar(new Horario(bus, ruta, LocalTime.of(6, 0), LocalTime.of(8, 0)));

            assertThrows(UnsupportedTypeException.class,
                    () -> programador.consultarHorariosPorTipoBus(bus, "MADRUGADA"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "  " })
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo de turno es nulo o vacío")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsVacio(String tipoInvalido) {
            programador.programar(new Horario(bus, ruta, LocalTime.of(6, 0), LocalTime.of(8, 0)));

            assertThrows(UnsupportedTypeException.class,
                    () -> programador.consultarHorariosPorTipoBus(bus, tipoInvalido));
        }
    }

    @Nested
    @DisplayName("Al programar un horario solapado")
    class AlProgramarHorarioSolapado {

        private final Bus bus = new Bus("ABC123", "Diesel");
        private final Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");

        @ParameterizedTest(name = "existente 08:00-10:00 vs nuevo {0}-{1}")
        @CsvSource({
                "08:30, 10:30",
                "07:30, 09:00",
                "08:00, 10:00",
                "07:00, 11:00"
        })
        @DisplayName("Debe rechazar horarios que se solapen con uno ya programado")
        void debeRechazarHorarioSolapado(String salida, String llegada) {
            programador.programar(new Horario(bus, ruta, LocalTime.of(8, 0), LocalTime.of(10, 0)));
            Horario nuevo = new Horario(bus, ruta, LocalTime.parse(salida), LocalTime.parse(llegada));

            assertThrows(IllegalArgumentException.class, () -> programador.programar(nuevo));
        }

        @Test
        @DisplayName("Debe permitir horarios contiguos sin solapamiento")
        void debePermitirHorarioContiguo() {
            programador.programar(new Horario(bus, ruta, LocalTime.of(8, 0), LocalTime.of(10, 0)));
            Horario contiguo = new Horario(bus, ruta, LocalTime.of(10, 0), LocalTime.of(11, 0));

            assertDoesNotThrow(() -> programador.programar(contiguo));
        }
    }

    @Nested
    @DisplayName("Al programar un horario con rango de horas inválido")
    class AlProgramarHorarioConRangoInvalido {

        private final Bus bus = new Bus("ABC123", "Diesel");
        private final Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");

        @ParameterizedTest(name = "salida {0} - llegada {1}")
        @CsvSource({
                "10:00, 08:00",
                "10:00, 10:00"
        })
        @DisplayName("Debe rechazar cuando la llegada no es posterior a la salida")
        void debeRechazarHorarioRangoInvalido(String salida, String llegada) {
            Horario horario = new Horario(bus, ruta, LocalTime.parse(salida), LocalTime.parse(llegada));

            assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
        }
    }

    @Nested
    @DisplayName("Al programar un horario con parámetros nulos")
    class AlProgramarHorarioConParametrosNulos {

        private final Bus bus = new Bus("ABC123", "Diesel");
        private final Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");

        @Test
        @DisplayName("Debe rechazar un horario nulo")
        void debeRechazarHorarioNulo() {
            assertThrows(IllegalArgumentException.class, () -> programador.programar(null));
        }

        @Test
        @DisplayName("Debe rechazar un horario sin bus")
        void debeRechazarHorarioSinBus() {
            Horario horario = new Horario(null, ruta, LocalTime.of(8, 0), LocalTime.of(10, 0));

            assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
        }

        @Test
        @DisplayName("Debe rechazar un horario sin ruta")
        void debeRechazarHorarioSinRuta() {
            Horario horario = new Horario(bus, null, LocalTime.of(8, 0), LocalTime.of(10, 0));

            assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
        }

        @Test
        @DisplayName("Debe rechazar un horario sin hora de salida")
        void debeRechazarHorarioSinHoraSalida() {
            Horario horario = new Horario(bus, ruta, null, LocalTime.of(10, 0));

            assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
        }

        @Test
        @DisplayName("Debe rechazar un horario sin hora de llegada")
        void debeRechazarHorarioSinHoraLlegada() {
            Horario horario = new Horario(bus, ruta, LocalTime.of(8, 0), null);

            assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
        }
    }
}
