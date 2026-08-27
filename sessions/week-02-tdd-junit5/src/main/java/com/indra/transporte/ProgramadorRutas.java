package com.indra.transporte;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Type;

import java.util.List;

public interface ProgramadorRutas {

    List<Horario> getHorarios();

    void programar(Horario horario);

    boolean debeValidarTipoRutasYBuses(Horario horario);

    List<Horario> consultarHorariosPorTipoBus(Bus bus, Type type);

}
