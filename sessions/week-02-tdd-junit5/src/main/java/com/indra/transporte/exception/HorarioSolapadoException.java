package com.indra.transporte.exception;

import com.indra.transporte.model.Horario;

public class HorarioSolapadoException extends RuntimeException {
    public HorarioSolapadoException(Horario nuevo, Horario existente) {
        super("Horario solapado. Nuevo=" + nuevo + " | Existente=" + existente);
    }
}
