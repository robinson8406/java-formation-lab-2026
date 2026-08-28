package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import com.indra.transporte.model.Type;

public class ProgramadorRutasImpl implements ProgramadorRutas {

    private final List<Horario> horarios = new ArrayList<>();

    @Override
    public List<Horario> getHorarios() {
        return List.copyOf(horarios);
    }

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
        if (horario.bus() == null || horario.ruta() == null
                || horario.horaSalida() == null || horario.horaLlegada() == null) {
            throw new IllegalArgumentException("Bus, ruta y horas del horario son obligatorios");
        }
        if (isBlank(horario.bus().placa()) || horario.bus().type() == null
                || isBlank(horario.ruta().tipo()) || isBlank(horario.ruta().codigo())
                || isBlank(horario.ruta().origen()) || isBlank(horario.ruta().destino())) {
            throw new IllegalArgumentException("Los textos del bus y la ruta no pueden ser nulos o vacíos");
        }
    }

    private void validarRangoHorario(Horario horario) {
        if (!horario.horaLlegada().isAfter(horario.horaSalida())) {
            throw new IllegalArgumentException("La hora de llegada no puede ser menor o igual a la hora de salida");
        }
    }

    private void validarSolapamiento(Horario nuevoHorario) {
        boolean solapado = horarios.stream()
                .filter(h -> h.bus().placa().equals(nuevoHorario.bus().placa()))
                .anyMatch(h -> nuevoHorario.horaSalida().isBefore(h.horaLlegada())
                        && nuevoHorario.horaLlegada().isAfter(h.horaSalida()));

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
        Type tipoBus = horario.bus().type();
        String tipoRuta = horario.ruta().tipo();

        if (Type.ELECTRIC.equals(tipoBus) && !Type.ELECTRIC.getName().equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }

    @Override
    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipo) {
        if (bus == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }
        if (tipo == null) {
            throw new UnsupportedTypeException("Tipo de bus no soportado: null");
        }

        validarTipoSoportado(tipo);

        boolean busExiste = horarios.stream()
                .anyMatch(h -> h.bus().placa().equals(bus.placa()));
        if (!busExiste) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        return horarios.stream()
                .filter(h -> h.bus().placa().equals(bus.placa()))
                .filter(h -> Type.valueOf(tipo).equals(h.bus().type()))
                .toList();
    }

    private static void validarTipoSoportado(String tipo) {
        try {
            Type.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedTypeException("Tipo de bus no soportado: " + tipo);
        }
    }

}
