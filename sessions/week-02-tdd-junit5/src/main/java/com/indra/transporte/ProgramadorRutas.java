package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;

import com.indra.transporte.model.Ruta;
import com.indra.transporte.model.Tipo;
import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        validarHorario(horario);
        validarBus(horario.getBus());
        validarRuta(horario.getRuta());
        validarTipoRutasYBuses(horario);
        validarHorarioSolapado(horario);
        validarHorarioRango(horario);
        horarios.add(horario);
    }

    public void validarHorario(Horario horario) {
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
    }

    public void validarBus(Bus bus) {
        if (bus == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }
        if (bus.getPlaca() == null || bus.getPlaca().isEmpty()) {
            throw new IllegalArgumentException("La placa del bus no puede ser nula o vacía");
        }
        if (bus.getTipo() == null || bus.getTipo().isEmpty()) {
            throw new IllegalArgumentException("El tipo del bus no puede ser nulo o vacío");
        }
    }

    public void validarRuta(Ruta ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta no puede ser nula");
        }
        if (ruta.getCodigo() == null || ruta.getCodigo().isEmpty()) {
            throw new IllegalArgumentException("El código de la ruta no puede ser nulo o vacío");
        }
        if (ruta.getTipo() == null || ruta.getTipo().isEmpty()) {
            throw new IllegalArgumentException("El tipo de la ruta no puede ser nulo o vacío");
        }
        if (ruta.getOrigen() == null || ruta.getOrigen().isEmpty()) {
            throw new IllegalArgumentException("El origen de la ruta no puede ser nulo o vacío");
        }
        if (ruta.getDestino() == null || ruta.getDestino().isEmpty()) {
            throw new IllegalArgumentException("El destino de la ruta no puede ser nulo o vacío");
        }
    }

    public boolean validarTipoRutasYBuses(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        String tipoBus = horario.getBus().getTipo();
        String tipoRuta = horario.getRuta().getTipo();

        if (Tipo.ELECTRIC.getValue().equals(tipoBus) && !Tipo.ELECTRIC.getValue().equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }

    public void validarHorarioSolapado(Horario horario) {
        for (Horario existente : horarios) {
            boolean mismoBus = existente.getBus().getPlaca().equals(horario.getBus().getPlaca());
            if (!mismoBus) {
                continue;
            }

            boolean seSolapan =
                    horario.getHoraSalida().isBefore(existente.getHoraLlegada()) &&
                            horario.getHoraLlegada().isAfter(existente.getHoraSalida());

            if (seSolapan) {
                throw new IllegalArgumentException("Un bus no puede tener horarios solapados.");
            }
        }
    }

    public void validarHorarioRango(Horario horario) {
        if (horario.getHoraSalida().isAfter(horario.getHoraLlegada())) {
            throw new IllegalArgumentException("Un bus no puede tener una hora de llegada menor a la hora de salida.");
        }
    }

    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipo) {


        if (bus == null) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        if (tipo == null || !Tipo.isSupported(tipo)) {
            throw new UnsupportedTypeException("Tipo desconocido: " + tipo);
        }

        boolean busExiste = horarios.stream()
                .map(Horario::getBus)
                .anyMatch(b -> Objects.equals(b.getPlaca(), bus.getPlaca())
                        && Objects.equals(b.getTipo(), bus.getTipo()));


        if (!busExiste) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        return horarios.stream()
                .filter(h -> Objects.equals(h.getBus().getPlaca(), bus.getPlaca())
                        && Objects.equals(h.getBus().getTipo(), bus.getTipo()))
                .toList();
    }
}
