package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;

@DisplayName("ProgramadorRutas")
public class ProgramadorRutasTest {

    private static final String ELECTRICO = "Electric";
    private static final String GENERAL = "General";
    private static final String DIESEL = "Diesel";
    private static final String PLACA = "ABC123";
    private final ProgramadorRutas programador = new ProgramadorRutas();

    private static Bus bus(String placa, String tipo) {
        return new Bus(placa, tipo);
    }

    private static Ruta ruta(String tipo) {
        return new Ruta(tipo, "R001", "Ciudad A", "Ciudad B");
    }

    private static LocalTime hora(String valor) {
        return LocalTime.parse(valor);
    }

    private static Horario horario(Bus bus, Ruta ruta, LocalTime salida, LocalTime llegada) {
        return new Horario(bus, ruta, salida, llegada);
    }

    @Test
    @DisplayName("Debe registrar un horario")
    void debeRegistrarUnHorario() {
        programador.programar(horario(bus(PLACA, DIESEL), ruta(ELECTRICO), hora("08:00"), hora("10:00")));

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
    @DisplayName("Debe rechazar un horario nulo")
    void debeRechazarHorarioNulo() {
        assertThrows(IllegalArgumentException.class, () -> programador.programar(null));
    }

    @Test
    @DisplayName("Debe rechazar un horario sin bus")
    void debeRechazarBusNulo() {
        Horario sinBus = horario(null, ruta(GENERAL), hora("08:00"), hora("10:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(sinBus));
    }

    @Test
    @DisplayName("Debe rechazar un horario sin ruta")
    void debeRechazarRutaNula() {
        Horario sinRuta = horario(bus(PLACA, DIESEL), null, hora("08:00"), hora("10:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(sinRuta));
    }

    @Test
    @DisplayName("Debe rechazar un horario sin hora de salida")
    void debeRechazarHoraSalidaNula() {
        Horario sinSalida = horario(bus(PLACA, DIESEL), ruta(GENERAL), null, hora("10:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(sinSalida));
    }

    @Test
    @DisplayName("Debe rechazar un horario sin hora de llegada")
    void debeRechazarHoraLlegadaNula() {
        Horario sinLlegada = horario(bus(PLACA, DIESEL), ruta(GENERAL), hora("08:00"), null);
        assertThrows(IllegalArgumentException.class, () -> programador.programar(sinLlegada));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Debe rechazar una placa de bus nula, vacía o en blanco")
    void debeRechazarPlacaNulaOVacia(String placa) {
        Horario h = horario(bus(placa, DIESEL), ruta(GENERAL), hora("08:00"), hora("10:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(h));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Debe rechazar un tipo de bus nulo, vacío o en blanco")
    void debeRechazarTipoBusNuloOVacio(String tipoBus) {
        Horario h = horario(bus(PLACA, tipoBus), ruta(GENERAL), hora("08:00"), hora("10:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(h));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Debe rechazar un tipo de ruta nulo, vacío o en blanco")
    void debeRechazarTipoRutaNuloOVacio(String tipoRuta) {
        Horario h = horario(bus(PLACA, DIESEL), ruta(tipoRuta), hora("08:00"), hora("10:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(h));
    }

    @Test
    @DisplayName("Debe rechazar un horario cuya llegada es anterior a la salida")
    void debeRechazarHorarioRangoInvalido() {
        Horario invertido = horario(bus(PLACA, DIESEL), ruta(GENERAL), hora("10:00"), hora("08:00"));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(invertido));
    }

    @ParameterizedTest(name = "salida {0} y llegada {1} debe rechazarse")
    @CsvSource({
            "10:00, 08:00",
            "10:00, 10:00"
    })
    @DisplayName("Debe rechazar cuando la llegada no es posterior a la salida")
    void debeRechazarHorarioConLlegadaNoPosteriorALaSalida(String salida, String llegada) {
        Horario h = horario(bus(PLACA, DIESEL), ruta(GENERAL), hora(salida), hora(llegada));
        assertThrows(IllegalArgumentException.class, () -> programador.programar(h));
    }

    @Nested
    @DisplayName("Cuando un bus ya tiene horarios programados")
    class CuandoUnBusYaTieneHorariosProgramados {

        @BeforeEach
        void programarHorarioBase() {
            programador.programar(horario(bus(PLACA, DIESEL), ruta(GENERAL), hora("08:00"), hora("10:00")));
        }

        @Test
        @DisplayName("Debe rechazar un horario que se solapa con el existente")
        void debeRechazarHorarioSolapado() {
            Horario solapado = horario(bus(PLACA, DIESEL), ruta(GENERAL), hora("08:30"), hora("10:30"));
            assertThrows(IllegalArgumentException.class, () -> programador.programar(solapado));
            assertEquals(1, programador.getHorarios().size());
        }

        @ParameterizedTest(name = "nuevo {2}-{3} sobre existente {0}-{1}")
        @CsvSource({
                "08:00, 10:00, 08:30, 10:30",
                "08:00, 10:00, 07:30, 08:30",
                "08:00, 10:00, 07:00, 11:00",
                "08:00, 10:00, 08:00, 10:00",
                "08:00, 10:00, 09:00, 09:30"
        })
        @DisplayName("Debe rechazar cualquier intersección de intervalos para el mismo bus")
        void debeRechazarSolapamientoParametrizado(String salidaExistente, String llegadaExistente,
                String salidaNueva, String llegadaNueva) {
            Horario nuevo = horario(bus(PLACA, DIESEL), ruta(GENERAL), hora(salidaNueva), hora(llegadaNueva));
            assertThrows(IllegalArgumentException.class, () -> programador.programar(nuevo));
        }

        @ParameterizedTest(name = "nuevo {0}-{1} es contiguo")
        @CsvSource({
                "10:00, 12:00",
                "06:00, 08:00"
        })
        @DisplayName("Debe permitir horarios contiguos que no se solapan")
        void debePermitirHorariosContiguos(String salida, String llegada) {
            Horario contiguo = horario(bus(PLACA, DIESEL), ruta(GENERAL), hora(salida), hora(llegada));
            assertDoesNotThrow(() -> programador.programar(contiguo));
            assertEquals(2, programador.getHorarios().size());
        }

        @Test
        @DisplayName("Debe permitir el mismo horario para un bus distinto")
        void debePermitirMismoHorarioParaOtroBus() {
            Horario otroBus = horario(bus("XYZ999", DIESEL), ruta(GENERAL), hora("08:00"), hora("10:00"));
            assertDoesNotThrow(() -> programador.programar(otroBus));
            assertEquals(2, programador.getHorarios().size());
        }
    }

    @Nested
    @DisplayName("Cuando se consultan horarios por tipo de bus")
    class CuandoSeConsultanHorarios {

        @BeforeEach
        void programarHorariosMixtos() {
            programador.programar(horario(bus(PLACA, DIESEL), ruta(ELECTRICO), hora("08:00"), hora("10:00")));
            programador.programar(horario(bus(PLACA, DIESEL), ruta(GENERAL), hora("11:00"), hora("12:00")));
        }

        @Test
        @DisplayName("Debe devolver solo los horarios del tipo solicitado")
        void debeDevolverLosHorariosDelTipoSolicitado() {
            List<Horario> resultado = programador.consultarHorariosPorTipoBus(bus(PLACA, DIESEL), ELECTRICO);

            assertEquals(1, resultado.size());
            assertEquals(ELECTRICO, resultado.get(0).getRuta().getTipo());
        }

        @Test
        @DisplayName("Debe devolver lista vacía si el bus no tiene horarios de ese tipo")
        void debeDevolverListaVaciaSiNoHayHorariosDeEseTipo() {
            Horario soloGeneral = horario(bus("XYZ999", DIESEL), ruta(GENERAL), hora("13:00"), hora("14:00"));
            programador.programar(soloGeneral);

            List<Horario> resultado = programador.consultarHorariosPorTipoBus(bus("XYZ999", DIESEL), ELECTRICO);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
        void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
            Bus desconocido = bus("NOEXISTE", DIESEL);
            assertThrows(IllegalArgumentException.class,
                    () -> programador.consultarHorariosPorTipoBus(desconocido, ELECTRICO));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es nulo")
        void debeLanzarIllegalArgumentExceptionCuandoBusEsNulo() {
            assertThrows(IllegalArgumentException.class,
                    () -> programador.consultarHorariosPorTipoBus(null, ELECTRICO));
        }

        @Test
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
            assertThrows(UnsupportedTypeException.class,
                    () -> programador.consultarHorariosPorTipoBus(bus(PLACA, DIESEL), "Hidrogeno"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es nulo o vacío")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsNuloOVacio(String tipo) {
            assertThrows(UnsupportedTypeException.class,
                    () -> programador.consultarHorariosPorTipoBus(bus(PLACA, DIESEL), tipo));
        }

        @Test
        @DisplayName("Flujo completo: programar, rechazar solapado, aceptar contiguo y consultar")
        void flujoCompleto() {
            programador.programar(horario(bus(PLACA, DIESEL), ruta(ELECTRICO), hora("15:00"), hora("16:00")));

            assertThrows(IllegalArgumentException.class, () -> programador.programar(
                    horario(bus(PLACA, DIESEL), ruta(ELECTRICO), hora("11:00"), hora("13:00"))));

            assertEquals(2, programador.consultarHorariosPorTipoBus(bus(PLACA, DIESEL), ELECTRICO).size());
        }
    }

}
