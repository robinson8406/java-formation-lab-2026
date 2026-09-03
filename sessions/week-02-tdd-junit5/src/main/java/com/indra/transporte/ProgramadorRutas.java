package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import com.indra.transporte.model.TipoVehiculoRuta;
import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        boolean solapado = horarios.stream()
                .filter(h -> h.getBus().equals(horario.getBus()))
                .anyMatch(h ->
                        horario.getHoraSalida().isBefore(h.getHoraLlegada())
                                && horario.getHoraLlegada().isAfter(h.getHoraSalida())
                );

        if (solapado) {
            throw new IllegalArgumentException("Horario solapado para el bus");
        }

        horarios.add(horario);

        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        horarios.add(horario);
    }

    public boolean debeValidarTipoRutasYBuses(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        TipoVehiculoRuta tipoBus = horario.getBus().getTipo();
        TipoVehiculoRuta tipoRuta = horario.getRuta().getTipo();

        if (TipoVehiculoRuta.ELECTRIC.equals(tipoBus) && !TipoVehiculoRuta.ELECTRIC.equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }

    public List<Horario> consultarHorariosPorTipoBus(Bus bus, TipoVehiculoRuta tipo) {
        validarBusConocido(bus);
        validarTipoSoportado(tipo);

        return horarios.stream()
                .filter(h -> h.getBus().equals(bus))
                .filter(h -> h.getBus().getTipo().equals(tipo))
                .toList();
    }

    private void validarBusConocido(Bus bus) {
        boolean busExiste = horarios.stream().anyMatch(h -> h.getBus().equals(bus));
        if (!busExiste) {
            throw new IllegalArgumentException("Bus desconocido");
        }
    }

    private void validarTipoSoportado(TipoVehiculoRuta tipo) {
        if (tipo == null) {
            throw new UnsupportedTypeException("Tipo desconocido");
        }
    }

}
