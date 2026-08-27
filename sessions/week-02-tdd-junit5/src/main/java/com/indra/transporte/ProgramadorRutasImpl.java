package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import com.indra.transporte.model.Type;
import lombok.Data;

@Data
public class ProgramadorRutasImpl implements ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    @Override
    public void programar(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        horarios.add(horario);
    }

    @Override
    public boolean debeValidarTipoRutasYBuses(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        Type tipoBus = horario.getBus().getType();
        String tipoRuta = horario.getRuta().getTipo();

        if (Type.ELECTRIC.equals(tipoBus) && !"Electric".equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }

    @Override
    public List<Horario> consultarHorariosPorTipoBus(Bus bus, Type type) {
        return List.of();
    }

}
