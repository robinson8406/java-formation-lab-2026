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
        validarHorarioVacio(horario);
        validarDatosDelBus(horario.getBus());
        validarDatosDeLaRuta(horario.getRuta());
        horarios.add(horario);
    }

    public boolean debeValidarTipoRutasYBuses(Horario horario) {
        if (horario == null) {
            throw new IllegalArgumentException("Horario no puede ser nulo");
        }
        String tipoBus = horario.getBus().getTipo();
        String tipoRuta = horario.getRuta().getTipo();

        if (Tipo.ELECTRIC.getValor().equals(tipoBus) && !Tipo.ELECTRIC.getValor().equals(tipoRuta)) {
            throw new IllegalArgumentException("Los buses eléctricos solo pueden ir a rutas eléctricas");
        }
        return true;
    }




    public List<Horario> consultarHorariosPorTipoBus(Bus bus, String tipoBus) {

        validarTipoDelBus(tipoBus);
        validarExistenciaBus(bus);

        return horarios.stream()
                .filter(h -> Objects.equals(h.getBus().getPlaca(),bus.getPlaca()))
                .filter(h->Objects.equals(h.getBus().getTipo(),tipoBus))
                .toList();
    }


    private void validarExistenciaBus(Bus bus) {
        if (bus == null) {
            throw new IllegalArgumentException("Bus desconocido");
        }

        boolean busExistente = horarios.stream()
                .map(Horario::getBus)
                .anyMatch(b -> Objects.equals(b.getPlaca(),bus.getPlaca() )&& Objects.equals(b.getTipo(),bus.getTipo()));

        if (!busExistente) {
            throw new IllegalArgumentException("Bus desconocido");
        }

    }

    private void validarTipoDelBus(String tipo) {
        if (tipo == null || !Tipo.isSupported(tipo)) {
            throw new UnsupportedTypeException("Tipo de bus desconocido");
        }
    }


    private void validarHorarioVacio(Horario horario){

        if(horario == null){
            throw new IllegalArgumentException("El horario no puede ser nulo");
        }

        if(horario.getBus()==null){
            throw new IllegalArgumentException("El bus no puede ser nulo");
        }

        if(horario.getRuta()==null){
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
        if ((ruta.getOrigen() == null || ruta.getOrigen().isBlank())
                || (ruta.getDestino() == null || ruta.getDestino().isBlank())) {
            throw new IllegalArgumentException("El origen y destino de la ruta no pueden estar vacíos");
        }
    }

}
