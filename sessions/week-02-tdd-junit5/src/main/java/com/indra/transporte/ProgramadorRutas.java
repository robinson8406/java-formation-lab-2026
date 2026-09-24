package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Horario;

import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        horarios.add(horario);
    }

    public boolean debeValidarTipoRutasYBuses(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        String tipoBus = horario.getBus().getTipo();
        String tipoRuta = horario.getRuta().getTipo();

        if ("Electric".equals(tipoBus) && !"Electric".equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }

    public List<Horario> consultarHorariosPorTipoBus(String bus, String tipoBus) {
         if (tipoBus == null) {
            throw new UnsupportedTypeException("El tipo de bus no puede ser nulo");
        }
         if (bus == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }
        //valida exista bus
        horarios.stream()
                .filter(h -> bus.equals(h.getBus().getPlaca()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El bus no existe"));

        //valida exista tipo
        horarios.stream()
                .filter(h -> tipoBus.equals(h.getBus().getTipo()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedTypeException("El tipo de bus no existe"));

        return horarios.stream()
                       .filter(h -> tipoBus.equals(h.getBus().getTipo()))
                       .toList();

    }

    public boolean debeRechazarHorarioSolapado(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        for (Horario h : horarios) {
            if (h.getRuta().equals(horario.getRuta()) && h.getBus().equals(horario.getBus()) &&
                h.getHoraSalida().isBefore(horario.getHoraLlegada()) && h.getHoraLlegada().isAfter(horario.getHoraSalida())) {
                throw new IllegalArgumentException("El horario se solapa con otro existente");
            }
        }
        return true;
    }

    public boolean debeRechazarHorarioRangoInvalido(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        if (horario.getHoraSalida().isAfter(horario.getHoraLlegada())) {
            throw new IllegalArgumentException("El horario tiene un rango inválido");
        }
        return true;
    }
}
