package com.indra.transporte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Ruta;
import com.indra.transporte.model.Tipo;

public class ProgramadorRutas {

    private final List<Horario> horarios = new ArrayList<>();

    public List<Horario> getHorarios() {
        return Collections.unmodifiableList(horarios);
    }

    public void programar(Horario horario) {
        validarHorarioBase(horario);
        validarDatosDelBus(horario.getBus());
        validarDatosDeLaRuta(horario.getRuta());
        validarRangoHorario(horario);
        debeValidarTipoRutasYBuses(horario);
        validarHorarioSolapado(horario);
        horarios.add(horario);
    }

    private void validarHorarioSolapado(Horario nuevoHorario) {
        for (Horario horarioExistente : horarios) {
            if (!Objects.equals(horarioExistente.getBus().getPlaca(), nuevoHorario.getBus().getPlaca())) {
                continue;
            }

            boolean seSolapa = nuevoHorario.getHoraSalida().isBefore(horarioExistente.getHoraLlegada())
                    && nuevoHorario.getHoraLlegada().isAfter(horarioExistente.getHoraSalida());

            if (seSolapa) {
                throw new IllegalArgumentException("El horario se solapa con otro ya programado");
            }
        }
    }

    private void validarHorarioBase(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        if (horario.getBus() == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }
        if (horario.getRuta() == null) {
            throw new IllegalArgumentException("La ruta no puede ser nula");
        }
        if (horario.getHoraSalida() == null || horario.getHoraLlegada() == null) {
            throw new IllegalArgumentException("La hora de salida y la de llegada no pueden ser nulas");
        }
    }

    private void validarDatosDelBus(Bus bus) {
        if (bus.getPlaca() == null || bus.getPlaca().isBlank()) {
            throw new IllegalArgumentException("La placa del bus no puede estar vacía");
        }
    }

    private void validarDatosDeLaRuta(Ruta ruta) {
        if (ruta.getCodigo() == null || ruta.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código de la ruta no puede estar vacío");
        }
    }

    private void validarRangoHorario(Horario horario) {
        if (!horario.getHoraLlegada().isAfter(horario.getHoraSalida())) {
            throw new IllegalArgumentException("La hora de llegada debe ser mayor a la de salida");
        }
    }

    public boolean debeValidarTipoRutasYBuses(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }
        if (horario.getBus() == null) {
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }
        if (horario.getRuta() == null) {
            throw new IllegalArgumentException("La ruta no puede ser nula");
        }

        String tipoBus = horario.getBus().getTipo();
        String tipoRuta = horario.getRuta().getTipo();

        if (tipoBus == null || (!Tipo.ELECTRIC.getValor().equalsIgnoreCase(tipoBus) && !Tipo.DIESEL.getValor().equalsIgnoreCase(tipoBus))) {
            throw new IllegalArgumentException("Tipo de bus desconocido: " + tipoBus);
        }

        if (tipoRuta == null || (!Tipo.ELECTRIC.getValor().equalsIgnoreCase(tipoRuta) && !Tipo.GENERAL.getValor().equalsIgnoreCase(tipoRuta))) {
            throw new UnsupportedTypeException("Tipo de ruta desconocido: " + tipoRuta);
        }

        if (Tipo.ELECTRIC.getValor().equalsIgnoreCase(tipoBus) && !Tipo.ELECTRIC.getValor().equalsIgnoreCase(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }

    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipo) {
        if (bus == null) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        if (tipo == null || !Tipo.isSupported(tipo)) {
            throw new UnsupportedTypeException("Tipo de bus no soportado");
        }

        boolean busExistente = horarios.stream()
                .map(Horario::getBus)
                .anyMatch(b -> Objects.equals(b.getPlaca(), bus.getPlaca())
                        && Objects.equals(b.getTipo(), bus.getTipo()));

        if (!busExistente) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        return horarios.stream()
                .filter(h -> Objects.equals(h.getBus().getPlaca(), bus.getPlaca()))
                .filter(h -> Objects.equals(h.getBus().getTipo(), tipo))
                .toList();
    }

}
