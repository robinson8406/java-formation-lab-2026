package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import com.indra.transporte.model.Tipo;
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

        if (Tipo.ELECTRIC.getValor().equals(tipoBus) && !Tipo.ELECTRIC.getValor().equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }




    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipoBus) {

        return horarios.stream()
                .filter(h -> Objects.equals(h.getBus().getPlaca(),bus.getPlaca()))
                .filter(h->Objects.equals(h.getBus().getTipo(),tipoBus))
                .toList();
    }







}
