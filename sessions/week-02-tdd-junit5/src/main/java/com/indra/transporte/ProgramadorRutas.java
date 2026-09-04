package com.indra.transporte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Horario;
import com.indra.transporte.model.Tipo;

import lombok.Data;

@Data
public class ProgramadorRutas {

    List<Horario> horarios = new ArrayList<>();

    public void programar(Horario horario) {
        validarHorarioBase(horario);
        validarDatosDelBus(horario.getBus());
        validarDatosDeLaRuta(horario.getRuta());
        validarRangoHorario(horario);
        debeValidarTipoRutasYBuses(horario);
        validarHorarioSolapado(horario);
        horarios.add(horario);
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

    private void validarDatosDeLaRuta(com.indra.transporte.model.Ruta ruta) {
        if (ruta.getCodigo() == null || ruta.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código de la ruta no puede estar vacío");
        }
        if ((ruta.getOrigen() == null || ruta.getOrigen().isBlank())
                || (ruta.getDestino() == null || ruta.getDestino().isBlank())) {
            throw new IllegalArgumentException("El origen y destino de la ruta no pueden estar vacíos");
        }
    }

    private void validarRangoHorario(Horario horario) {
        if (horario.getHoraLlegada().isBefore(horario.getHoraSalida())) {
            throw new IllegalArgumentException("La hora de llegada debe ser mayor a la de salida");
        }
    }

    public boolean debeValidarTipoRutasYBuses(Horario horario) {
        String tipoBus = horario.getBus().getTipo();
        String tipoRuta = horario.getRuta().getTipo();

        if (Tipo.ELECTRIC.getValor().equals(tipoBus) && !Tipo.ELECTRIC.getValor().equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
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
