package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Tipo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

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

    @Test
    @DisplayName("Debe rechazar un horario con rango inválido.")
    void debeRechazarHorarioRangoInvalido() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                java.time.LocalTime.of(10, 0), java.time.LocalTime.of(8, 0));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> programador.validarHorarioRango(horario)
        );

        assertEquals(
                "Un bus no puede tener una hora de llegada menor a la hora de salida.",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Debe rechazar un horario solapado para el mismo bus.")
    void debeRechazarHorarioSolapado() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
        Horario horario1 = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        Horario horario2 = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 30), java.time.LocalTime.of(10, 30));

        assertDoesNotThrow(() -> programador.programar(horario1));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> programador.validarHorarioSolapado(horario2)
        );

        assertEquals(
                "Un bus no puede tener horarios solapados.",
                ex.getMessage()
        );
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
                programador.validarTipoRutasYBuses(horario);
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

            assertDoesNotThrow(() -> programador.validarTipoRutasYBuses(horario));
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

            assertDoesNotThrow(() -> programador.validarTipoRutasYBuses(horario));
        }
    }

    @Nested
    @DisplayName("Cuando se  consulta los horarios por tipo de bus")
    class CuandoConsultaHorariosPorTipoBus {

        @Test
        @DisplayName("Debe devolver los horarios del tipo solicitado")
        void debeDevolverLosHorariosDelTipoSolicitado() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
            programador.programar(horario);

            List<Horario> horarios = programador.consultarHorariosPorTipoBus(bus, "Diesel");
            assertEquals(1, horarios.size());

        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
        void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
            Bus bus = new Bus("ABC123", "Diesel");
            Bus bus2 = new Bus("ABC99999", "Diesel");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
            programador.programar(horario);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> programador.consultarHorariosPorTipoBus(bus2, "Diesel")
            );
        }

        @Test
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
            programador.programar(horario);

            assertThrows(
                    UnsupportedTypeException.class,
                    () -> programador.consultarHorariosPorTipoBus(bus, "8798798")
            );
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("casosInvalidosValidarHorario")
    @DisplayName("validarHorario: debe lanzar IllegalArgumentException en entradas inválidas")
    void validarHorarioDebeLanzarExcepcionEnCasosInvalidos(
            String nombreCaso,
            Horario horario,
            String mensajeEsperado) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> programador.validarHorario(horario)
        );
        assertEquals(mensajeEsperado, ex.getMessage());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("casosInvalidosValidarBus")
    @DisplayName("validarBus: debe lanzar IllegalArgumentException en entradas inválidas")
    void validarBusDebeLanzarExcepcionEnCasosInvalidos(
            String nombreCaso,
            Bus bus,
            String mensajeEsperado) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> programador.validarBus(bus)
        );
        assertEquals(mensajeEsperado, ex.getMessage());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("casosInvalidosValidarRuta")
    @DisplayName("validarRuta: debe lanzar IllegalArgumentException en entradas inválidas")
    void validarRutaDebeLanzarExcepcionEnCasosInvalidos(
            String nombreCaso,
            Ruta ruta,
            String mensajeEsperado) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> programador.validarRuta(ruta)
        );
        assertEquals(mensajeEsperado, ex.getMessage());
    }

    static Stream<Arguments> casosInvalidosValidarRuta() {
        return Stream.of(
                Arguments.of(
                        "ruta nula",
                        null,
                        "La ruta no puede ser nula"
                ),
                Arguments.of(
                        "código nulo",
                        new Ruta("Electric", null, "Ciudad A", "Ciudad B"),
                        "El código de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "código vacío",
                        new Ruta("Electric", "", "Ciudad A", "Ciudad B"),
                        "El código de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "tipo nulo",
                        new Ruta((Tipo) null, "R001", "Ciudad A", "Ciudad B"),
                        "El tipo de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "tipo vacío",
                        new Ruta("", "R001", "Ciudad A", "Ciudad B"),
                        "El tipo de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "origen nulo",
                        new Ruta("Electric", "R001", null, "Ciudad B"),
                        "El origen de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "origen vacío",
                        new Ruta("Electric", "R001", "", "Ciudad B"),
                        "El origen de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "destino nulo",
                        new Ruta("Electric", "R001", "Ciudad A", null),
                        "El destino de la ruta no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "destino vacío",
                        new Ruta("Electric", "R001", "Ciudad A", ""),
                        "El destino de la ruta no puede ser nulo o vacío"
                )
        );
    }

    static Stream<Arguments> casosInvalidosValidarBus() {
        return Stream.of(
                Arguments.of(
                        "bus nulo",
                        null,
                        "El bus no puede ser nulo"
                ),
                Arguments.of(
                        "placa nula",
                        new Bus(null, "Diesel"),
                        "La placa del bus no puede ser nula o vacía"
                ),
                Arguments.of(
                        "placa vacía",
                        new Bus("", "Diesel"),
                        "La placa del bus no puede ser nula o vacía"
                ),
                Arguments.of(
                        "tipo nulo",
                        new Bus("ABC123", (Tipo) null),
                        "El tipo del bus no puede ser nulo o vacío"
                ),
                Arguments.of(
                        "tipo vacío",
                        new Bus("ABC123", ""),
                        "El tipo del bus no puede ser nulo o vacío"
                )
        );
    }

    static Stream<Arguments> casosInvalidosValidarHorario() {
        Bus busValido = new Bus("ABC123", "Diesel");
        Ruta rutaValida = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");

        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "horario nulo",
                        null,
                        "El horario no puede ser nulo"
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "bus nulo",
                        new Horario(null, rutaValida, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)),
                        "El bus del horario no puede ser nulo"
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "ruta nula",
                        new Horario(busValido, null, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)),
                        "La ruta del horario no puede ser nula"
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "hora salida nula",
                        new Horario(busValido, rutaValida, null, java.time.LocalTime.of(10, 0)),
                        "Las horas de salida y llegada no pueden ser nulas"
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "hora llegada nula",
                        new Horario(busValido, rutaValida, java.time.LocalTime.of(8, 0), null),
                        "Las horas de salida y llegada no pueden ser nulas"
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        "hora salida y llegada nulas",
                        new Horario(busValido, rutaValida, null, null),
                        "Las horas de salida y llegada no pueden ser nulas"
                )
        );
    }
}
