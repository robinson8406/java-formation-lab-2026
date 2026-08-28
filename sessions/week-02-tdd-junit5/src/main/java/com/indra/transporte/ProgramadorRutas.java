package com.indra.transporte;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import java.util.List;

public interface ProgramadorRutas {

    List<Horario> getHorarios();

    void programar(Horario horario);

    boolean debeValidarTipoRutasYBuses(Horario horario);

    List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipo);

}
