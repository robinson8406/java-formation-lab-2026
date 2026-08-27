package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
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

    private static final java.util.Set<String> TIPOS_SOPORTADOS = java.util.Set.of("Electric", "Diesel", "General");

    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipo) {
        boolean busConocido = horarios.stream().anyMatch(h -> h.getBus().equals(bus));
        if (!busConocido) {
            throw new IllegalArgumentException("El bus es desconocido");
        }
        if (!TIPOS_SOPORTADOS.contains(tipo)) {
            throw new UnsupportedTypeException("El tipo de ruta es desconocido: " + tipo);
        }

        List<Horario> resultado = new ArrayList<>();
        for (Horario horario : horarios) {
            if (horario.getBus().equals(bus) && tipo.equals(horario.getRuta().getTipo())) {
                resultado.add(horario);
            }
        }
        return resultado;
    }

}
