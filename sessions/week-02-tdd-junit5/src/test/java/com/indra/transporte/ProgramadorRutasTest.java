package com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.indra.transporte.exception.HorarioSolapadoException;
import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.TipoVehiculoRuta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;

import java.time.LocalTime;

public class ProgramadorRutasTest {
    private final ProgramadorRutas programador = new ProgramadorRutas();

    @Test
    @DisplayName("Debe registrar un horario")
    void debeRegistrarUnHorario() {
        Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.ELECTRIC,"R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta,
                LocalTime.of(8, 0), LocalTime.of(10, 0));

        programador.programar(horario);

        assertEquals(1, programador.getHorarios().size());
    }

    @Nested
    @DisplayName("Cuando el bus es eléctrico")
    class CuandoBusEsElectrico {

        @Test
        @DisplayName("Debe rechazar rutas no eléctricas")
        void debeRechazarRutasNoElectricas() {
            Bus bus = new Bus("ABC123", TipoVehiculoRuta.ELECTRIC);
            Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    LocalTime.of(8, 0), LocalTime.of(10, 0));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                programador.debeValidarTipoRutasYBuses(horario);
            });

            assertEquals("Los buses eléctricos solo pueden ir a rutas eléctricas", exception.getMessage());
        }

        @Test
        @DisplayName("Debe permitir rutas eléctricas")
        void debePermitirRutasElectricas() {
            Bus bus = new Bus("ABC123", TipoVehiculoRuta.ELECTRIC);
            Ruta ruta = new Ruta(TipoVehiculoRuta.ELECTRIC, "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    LocalTime.of(8, 0), LocalTime.of(10, 0));

            assertDoesNotThrow(() -> programador.debeValidarTipoRutasYBuses(horario));
        }
    }

    @Nested
    @DisplayName("Cuando el bus no es eléctrico")
    class CuandoBusNoEsElectrico {

        @Test
        @DisplayName("Debe permitir cualquier tipo de ruta")
        void debePermitirCualquierTipoDeRuta() {
            Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
            Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");
            Horario horario = new Horario(bus, ruta,
                    LocalTime.of(8, 0), LocalTime.of(10, 0));

            assertDoesNotThrow(() -> programador.debeValidarTipoRutasYBuses(horario));
        }
    }

    @Test
    @DisplayName("Debe devolver los horarios del tipo solicitado")
    void debeDevolverLosHorariosDelTipoSolicitado() {
        Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");

        Horario h1 = new Horario(bus, ruta,
                LocalTime.of(8, 0), LocalTime.of(10, 0));
        Horario h2 = new Horario(bus, ruta,
                LocalTime.of(11, 0), LocalTime.of(12, 0));

        programador.programar(h1);
        programador.programar(h2);

        var resultado = programador.consultarHorariosPorTipoBus(bus, TipoVehiculoRuta.DIESEL);

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el bus es desconocido")
    void debeLanzarIllegalArgumentExceptionCuandoBusEsDesconocido() {
        Bus busRegistrado = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Bus busDesconocido = new Bus("ZZZ999", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");

        programador.programar(new Horario(busRegistrado, ruta,
                LocalTime.of(8, 0), LocalTime.of(10, 0)));

        assertThrows(IllegalArgumentException.class,
                () -> programador.consultarHorariosPorTipoBus(busDesconocido, TipoVehiculoRuta.DIESEL));
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedTypeException cuando el tipo es desconocido")
    void debeLanzarUnsupportedTypeExceptionCuandoTipoEsDesconocido() {
        Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");

        programador.programar(new Horario(bus, ruta,
                java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)));

        assertThrows(UnsupportedTypeException.class,
                () -> programador.consultarHorariosPorTipoBus(bus, null));
    }

    @Test
    @DisplayName("Debe rechazar un horario solapado para el mismo bus")
    void debeRechazarHorarioSolapado() {
        Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");

        programador.programar(new Horario(
                bus,
                ruta,
                java.time.LocalTime.of(8, 0),
                java.time.LocalTime.of(10, 0)
        ));

        Horario solapado = new Horario(
                bus,
                ruta,
                java.time.LocalTime.of(8, 30),
                java.time.LocalTime.of(10, 30)
        );
        assertThrows(HorarioSolapadoException.class, () -> programador.programar(solapado));
    }

    @Test
    @DisplayName("Debe rechazar horario con rango inválido cuando llegada es menor que salida")
    void debeRechazarHorarioRangoInvalido() {
        Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");

        Horario invalido = new Horario(
                bus,
                ruta,
                java.time.LocalTime.of(10, 0),
                java.time.LocalTime.of(8, 0)
        );

        assertThrows(IllegalArgumentException.class, () -> programador.programar(invalido));
    }

    @Test
    @DisplayName("Debe rechazar horario null")
    void debeRechazarHorarioNull() {
        assertThrows(IllegalArgumentException.class, () -> programador.programar(null));
    }

    @Test
    @DisplayName("Debe rechazar bus con placa vacía")
    void debeRechazarBusConPlacaVacia() {
        Bus bus = new Bus("", TipoVehiculoRuta.DIESEL);
        Ruta ruta = new Ruta(TipoVehiculoRuta.GENERAL, "R001", "Ciudad A", "Ciudad B");
        Horario horario = new Horario(bus, ruta, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
    }

    @Test
    @DisplayName("Debe rechazar horario con ruta null")
    void debeRechazarHorarioConRutaNull() {
        Bus bus = new Bus("ABC123", TipoVehiculoRuta.DIESEL);
        Horario horario = new Horario(bus, null, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));

        assertThrows(IllegalArgumentException.class, () -> programador.programar(horario));
    }

}
