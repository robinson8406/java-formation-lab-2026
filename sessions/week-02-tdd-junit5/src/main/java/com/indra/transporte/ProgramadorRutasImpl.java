package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        validarCamposObligatorios(horario);
        validarRangoHorario(horario);
        validarSolapamiento(horario);

        horarios.add(horario);
    }

    private void validarCamposObligatorios(Horario horario) {
        if (horario.getBus() == null || horario.getRuta() == null
                || horario.getHoraSalida() == null || horario.getHoraLlegada() == null) {
            throw new IllegalArgumentException("Bus, ruta y horas del horario son obligatorios");
        }
        if (isBlank(horario.getBus().getPlaca()) || horario.getBus().getType() == null
                || isBlank(horario.getRuta().getTipo()) || isBlank(horario.getRuta().getCodigo())
                || isBlank(horario.getRuta().getOrigen()) || isBlank(horario.getRuta().getDestino())) {
            throw new IllegalArgumentException("Los textos del bus y la ruta no pueden ser nulos o vacíos");
        }
    }

    private void validarRangoHorario(Horario horario) {
        if (!horario.getHoraLlegada().isAfter(horario.getHoraSalida())) {
            throw new IllegalArgumentException("La hora de llegada no puede ser menor o igual a la hora de salida");
        }
    }

    private void validarSolapamiento(Horario nuevoHorario) {
        boolean solapado = horarios.stream()
                .filter(h -> h.getBus().getPlaca().equals(nuevoHorario.getBus().getPlaca()))
                .anyMatch(h -> nuevoHorario.getHoraSalida().isBefore(h.getHoraLlegada())
                        && nuevoHorario.getHoraLlegada().isAfter(h.getHoraSalida()));

        if (solapado) {
            throw new IllegalArgumentException("El horario se solapa con otro ya programado para el bus");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
        if (bus == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }
        if (type == null) {
            throw new com.indra.transporte.exception.UnsupportedTypeException("Tipo de bus no soportado: null");
        }

        Set<Type> tiposSoportados = horarios.stream()
                .map(h -> h.getBus().getType())
                .collect(Collectors.toSet());

        if (!tiposSoportados.contains(type)) {
            throw new com.indra.transporte.exception.UnsupportedTypeException("Tipo de bus no soportado: " + type);
        }

        boolean busExiste = horarios.stream()
                .anyMatch(h -> h.getBus().getPlaca().equals(bus.getPlaca()));
        if (!busExiste) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        return horarios.stream()
                .filter(h -> h.getBus().getPlaca().equals(bus.getPlaca()))
                .filter(h -> type.equals(h.getBus().getType()))
                .toList();
    }

}
