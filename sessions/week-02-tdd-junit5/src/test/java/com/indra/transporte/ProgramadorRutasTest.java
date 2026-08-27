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
        
        java.util.List<Horario> resultados = programador.consultarHorariosPorTipoBus(bus, "Diesel");
        assertEquals(1, resultados.size());
        assertEquals(horario, resultados.get(0));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
    void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
        Bus busDesconocido = new Bus("XYZ789", "Diesel");
        assertThrows(IllegalArgumentException.class, () -> {
            programador.consultarHorariosPorTipoBus(busDesconocido, "Diesel");
        });
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
    void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
        programador.programar(horario);

        assertThrows(com.indra.transporte.exception.UnsupportedTypeException.class, () -> {
            programador.consultarHorariosPorTipoBus(bus, "Volador");
        });
    }

    @Nested
    @DisplayName("Cuando un bus ya tiene horarios programados")
    class CuandoUnBusYaTieneHorariosProgramados {

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
            Horario horarioExistente = new Horario(bus, ruta,
                    java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
            programador.programar(horarioExistente);
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.CsvSource({
            "08:30, 09:30, 'Solapamiento total interno'",
            "07:30, 08:30, 'Solapamiento al inicio'",
            "09:30, 10:30, 'Solapamiento al final'",
            "07:00, 11:00, 'Solapamiento total externo'"
        })
        @DisplayName("Debe rechazar nuevos horarios que se solapen")
        void debeRechazarHorariosSolapados(String horaInicio, String horaFin, String escenario) {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("General", "R002", "Ciudad B", "Ciudad C");
            Horario horarioSolapado = new Horario(bus, ruta,
                    java.time.LocalTime.parse(horaInicio), java.time.LocalTime.parse(horaFin));

            assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horarioSolapado);
            }, () -> "Fallo esperado en: " + escenario);
        }

        @Test
        @DisplayName("Debe permitir programar un horario no solapado (después del existente)")
        void debePermitirHorarioNoSolapadoDespues() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("General", "R002", "Ciudad B", "Ciudad C");
            Horario horarioValido = new Horario(bus, ruta,
                    java.time.LocalTime.of(10, 0), java.time.LocalTime.of(12, 0));

            assertDoesNotThrow(() -> programador.programar(horarioValido));
        }

        @Test
        @DisplayName("Debe permitir programar un horario no solapado (antes del existente)")
        void debePermitirHorarioNoSolapadoAntes() {
            Bus bus = new Bus("ABC123", "Diesel");
            Ruta ruta = new Ruta("General", "R002", "Ciudad B", "Ciudad C");
            Horario horarioValido = new Horario(bus, ruta,
                    java.time.LocalTime.of(6, 0), java.time.LocalTime.of(8, 0));

            assertDoesNotThrow(() -> programador.programar(horarioValido));
        }
    }

    @Test
    @DisplayName("Debe rechazar horario con rango inválido (llegada antes de salida)")
    void debeRechazarHorarioRangoInvalido() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        
        Horario horarioInvalido = new Horario(bus, ruta,
                java.time.LocalTime.of(10, 0), java.time.LocalTime.of(8, 0));
        
        assertThrows(IllegalArgumentException.class, () -> {
            programador.programar(horarioInvalido);
        });
    }

    @Test
    @DisplayName("Debe rechazar horario cuando tiene parámetros nulos")
    void debeRechazarParametrosNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            programador.programar(null);
        });

        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("General", "R001", "Ciudad A", "Ciudad B");
        
        Horario horarioSinSalida = new Horario(bus, ruta, null, java.time.LocalTime.of(10, 0));
        assertThrows(IllegalArgumentException.class, () -> {
            programador.programar(horarioSinSalida);
        });
    }
}
