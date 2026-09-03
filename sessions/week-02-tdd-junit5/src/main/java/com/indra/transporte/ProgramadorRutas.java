package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.exception.HorarioSolapadoException;
import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import com.indra.transporte.model.TipoVehiculoRuta;
import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        validarParametros(horario);
        validarHorarioSolapado(horario);
        validarRangoHorario(horario);
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

    private Horario buscarHorarioSolapado(Horario nuevo) {
        return horarios.stream()
                .filter(h -> h.getBus().equals(nuevo.getBus()))
                .filter(h ->
                        nuevo.getHoraSalida().isBefore(h.getHoraLlegada())
                                && nuevo.getHoraLlegada().isAfter(h.getHoraSalida())
                )
                .findFirst()
                .orElse(null);
    }

    private void validarHorarioSolapado(Horario horario) {
        Horario conflicto = buscarHorarioSolapado(horario);
        if (conflicto != null) {
            throw new HorarioSolapadoException(horario, conflicto);
        }
    }

    private void validarRangoHorario(Horario nuevo) {
        if (nuevo.getHoraLlegada().isBefore(nuevo.getHoraSalida())) {
            throw new IllegalArgumentException("Rango horario inválido");
        }
    }

    private void validarParametros(Horario nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("Horario no puede ser null");
        }
        if (nuevo.getBus() == null){
            throw new IllegalArgumentException("Bus no puede ser null");
        }
        if (nuevo.getRuta() == null){
            throw new IllegalArgumentException("Ruta no puede ser null");
        }
        if (nuevo.getBus().getPlaca() == null || nuevo.getBus().getPlaca().isBlank()) {
            throw new IllegalArgumentException("La placa del bus no puede ser vacía");
        }
    }

}
