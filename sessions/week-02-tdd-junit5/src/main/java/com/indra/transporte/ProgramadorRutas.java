package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.TipoTurno;

import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        validarHorario(horario);
        validarNoSolapamiento(horario);
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

    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipoTurno) {
        if (bus == null || horarios.stream().noneMatch(h -> h.getBus().equals(bus))) {
            throw new IllegalArgumentException("Bus desconocido: " + bus);
        }
        TipoTurno turno = TipoTurno.desde(tipoTurno);
        return horarios.stream()
                .filter(h -> h.getBus().equals(bus))
                .filter(h -> turno.incluye(h.getHoraSalida()))
                .collect(Collectors.toList());
    }

    private void validarHorario(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        if (horario.getBus() == null) {
            throw new IllegalArgumentException("El bus del horario no puede ser nulo");
        }
        if (horario.getRuta() == null) {
            throw new IllegalArgumentException("La ruta del horario no puede ser nula");
        }
        if (horario.getHoraSalida() == null || horario.getHoraLlegada() == null) {
            throw new IllegalArgumentException("Las horas de salida y llegada no pueden ser nulas");
        }
        if (!horario.getHoraLlegada().isAfter(horario.getHoraSalida())) {
            throw new IllegalArgumentException("La hora de llegada debe ser posterior a la hora de salida");
        }
    }

    private void validarNoSolapamiento(Horario nuevo) {
        boolean solapa = horarios.stream()
                .filter(h -> h.getBus().equals(nuevo.getBus()))
                .anyMatch(h -> nuevo.getHoraSalida().isBefore(h.getHoraLlegada())
                        && h.getHoraSalida().isBefore(nuevo.getHoraLlegada()));
        if (solapa) {
            throw new IllegalArgumentException("El horario se solapa con uno ya programado para este bus");
        }
    }

}
