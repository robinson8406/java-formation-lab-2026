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

import java.util.List;

public class ProgramadorRutasTest {
    private final ProgramadorRutas programador = new ProgramadorRutas();

    @Test
    @DisplayName("Debe registrar un horario")
    void debeRegistrarUnHorario() {
        Bus bus = new Bus("ABC123", "Diesel");
        Ruta ruta = new Ruta("Electric","R001", "Ciudad A", "Ciudad B");
        Horario horario = Horario.builder()
                .bus(bus)
                .ruta(ruta)
                .horaSalida(java.time.LocalTime.of(8, 0))
                .horaLlegada(java.time.LocalTime.of(10, 0))
                .build();

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
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

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
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

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
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            assertDoesNotThrow(() -> programador.debeValidarTipoRutasYBuses(horario));
        }
    }

    @Nested
    @DisplayName("Consulta de rutas por horario y tipo de bus")
    class ProgramarRutasTest {

        @Test
        @DisplayName("Debe devolver los horarios del tipo solicitado")
        void debeDevolverLosHorariosDelTipoSolicitado() {
            Bus electricBus = new Bus("A123", "Electric");
            Bus dieselBus = new Bus("B456", "Diesel");

            Ruta electricRoute = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Ruta dieselRoute = new Ruta("General", "R002", "Ciudad C", "Ciudad D");

            Horario horarioCra8 = Horario.builder()
                    .bus(electricBus)
                    .ruta(electricRoute)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            Horario horarioCr5 = Horario.builder()
                    .bus(electricBus)
                    .ruta(electricRoute)
                    .horaSalida(java.time.LocalTime.of(12, 0))
                    .horaLlegada(java.time.LocalTime.of(14, 0))
                    .build();

            Horario horarioCra3 = Horario.builder()
                    .bus(dieselBus)
                    .ruta(electricRoute)
                    .horaSalida(java.time.LocalTime.of(9, 30))
                    .horaLlegada(java.time.LocalTime.of(11, 0))
                    .build();

            programador.programar(horarioCra8);
            programador.programar(horarioCr5);
            programador.programar(horarioCra3);

            List<Horario> listHorarios = programador.consultarHorariosPorTipoBus(electricBus,"Electric");
            assertEquals(2, listHorarios.size());
            assertEquals(List.of(horarioCra8, horarioCr5), listHorarios);

        }

    }
    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
    void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
        fail("Implementar este test para lanzar IllegalArgumentException cuando el bus es desconocido");
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
    void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
        fail("Implementar este test para lanzar UnsupportedTypeException cuando el tipo es desconocido");
    }



}
