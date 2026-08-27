package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.model.Schedule;

import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Schedule> horarios = new ArrayList<>();

    public void programar(Schedule horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        horarios.add(horario);
    }

    public boolean debeValidarTipoRutasYBuses(Schedule horario) {
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

}
