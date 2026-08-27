package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.exception.UnsupportedTypeException;

import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        if (horario.getBus() == null || horario.getRuta() == null 
                || horario.getHoraSalida() == null || horario.getHoraLlegada() == null) {
            throw new IllegalArgumentException("Los campos del horario no pueden ser nulos");
        }

        // 4. debeRechazarHorarioRangoInvalido: Hora de llegada no puede ser anterior a la salida
        if (horario.getHoraLlegada().isBefore(horario.getHoraSalida())) {
            throw new IllegalArgumentException("La hora de llegada no puede ser menor que la hora de salida");
        }

        // 1. debeValidarTipoRutasYBuses
        debeValidarTipoRutasYBuses(horario);

        // 3. debeRechazarHorarioSolapado: Un bus no puede tener horarios que se solapen
        for (Horario h : horarios) {
            if (h.getBus().getPlaca().equals(horario.getBus().getPlaca())) {
                // Solapamiento si: nuevo_salida < existente_llegada AND nuevo_llegada > existente_salida
                if (horario.getHoraSalida().isBefore(h.getHoraLlegada()) 
                        && horario.getHoraLlegada().isAfter(h.getHoraSalida())) {
                    throw new IllegalArgumentException("El horario se solapa con otro horario programado para el mismo bus");
                }
            }
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

    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new UnsupportedTypeException("El tipo de bus no puede estar vacío");
        }
        if (!"Electric".equalsIgnoreCase(tipo) && !"Diesel".equalsIgnoreCase(tipo)) {
            throw new UnsupportedTypeException("Tipo de bus desconocido: " + tipo);
        }
        if (bus == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }

        // Bus desconocido si no tiene ningún horario registrado
        boolean busExiste = horarios.stream()
                .anyMatch(h -> h.getBus().getPlaca().equals(bus.getPlaca()));
        if (!busExiste) {
            throw new IllegalArgumentException("Bus desconocido: " + bus.getPlaca());
        }

        return horarios.stream()
                .filter(h -> h.getBus().getPlaca().equals(bus.getPlaca()) && h.getBus().getTipo().equalsIgnoreCase(tipo))
                .collect(Collectors.toList());
    }

}
