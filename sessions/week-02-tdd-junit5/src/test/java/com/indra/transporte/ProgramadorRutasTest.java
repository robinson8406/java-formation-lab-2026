package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.indra.transporte.exception.UnsupportedTypeException;
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
        Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
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

            List<Horario> listHorarios = programador.consultarHorariosPorTipoBus(electricBus, "Electric");
            assertEquals(2, listHorarios.size());
            assertEquals(List.of(horarioCra8, horarioCr5), listHorarios);

        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
        void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
            Bus unknownBus = new Bus("XYZ789", "Diesel");
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.consultarHorariosPorTipoBus(unknownBus, "Diesel");
            });
            assertEquals("Bus desconocido", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
        void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
            Bus bus = new Bus("ABC123", "UnknownType");
            UnsupportedTypeException exception = assertThrows(UnsupportedTypeException.class, () -> {
                programador.consultarHorariosPorTipoBus(bus, "UnknownType");
            });
            assertEquals("Tipo de bus desconocido", exception.getMessage());
        }
    }


    @Nested
    @DisplayName("Validaciones generales de horarios")
    class validacionesGeneralesDeHorarios {

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el horario es nulo")
        void debeLanzarIllegalArgumentExceptionCuandoHorarioEsNulo() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(null);
            });
            assertEquals("El horario no puede ser nulo", exception.getMessage());
        }


        @Test
        @DisplayName("Debe rechazar un horario con bus nulo")
        void debeRechazarUnHorarioConBusNulo() {
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = Horario.builder()
                    .bus(null)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("El bus no puede ser nulo", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar un horario con ruta nula")
        void debeRechazarUnHorarioConRutaNula() {
            Bus bus = new Bus("ABC123", "Electric");
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(null)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("La ruta no puede ser nula", exception.getMessage());
        }


        @Test
        @DisplayName("Debe rechazar un bus con placa vacía")
        void debeRechazarUnBusConPlacaVacia() {
            Bus bus = new Bus("", "Electric");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("La placa del bus no puede estar vacía", exception.getMessage());

        }

        @Test
        @DisplayName("Debe rechazar una ruta con código o ubicaciones vacías")
        void debeRechazarUnaRutaConCodigoOUbicacionesVacias() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("Electric", "", "", "");
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("El código de la ruta no puede estar vacío", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar un horario con horas nulas")
        void debeRechazarUnHorarioConHorasNulas() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(null)
                    .horaLlegada(null)
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("La hora de salida y la de llegada no pueden ser nulas", exception.getMessage());
        }
    }


    @Nested
    @DisplayName("Cuando se validan los rangos de horas ")
    class ValidacionDeRangosDeHoras {

        @Test
        @DisplayName("Debe rechazar un horario con hora de salida mayor a la de llegada")
        void debeRechazarUnHorarioConHoraDeSalidaMayorALaDeLlegada() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad B");
            Horario horario = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(10, 0))
                    .horaLlegada(java.time.LocalTime.of(8, 0))
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario);
            });

            assertEquals("La hora de salida no puede ser mayor a la de llegada", exception.getMessage());
        }


        @Test
        @DisplayName("Debe rechachar solapamiento de horarios para el mismo bus")
        void debeRechacharSolapamientoDeHorariosParaElMismoBus() {
            Bus bus = new Bus("ABC123", "Electric");
            Ruta ruta = new Ruta("Electric", "R001", "Ciudad A", "Ciudad    B");
            Horario horario1 = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(8, 0))
                    .horaLlegada(java.time.LocalTime.of(10, 0))
                    .build();

            Horario horario2 = Horario.builder()
                    .bus(bus)
                    .ruta(ruta)
                    .horaSalida(java.time.LocalTime.of(9, 0))
                    .horaLlegada(java.time.LocalTime.of(11, 0))
                    .build();

            programador.programar(horario1);
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.programar(horario2);
            });
            assertEquals("El horario se solapa con otro horario existente para el mismo bus", exception.getMessage());

        }
    }




}
