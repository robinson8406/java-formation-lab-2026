package com.indra.transporte.model;

import java.time.LocalTime;

import com.indra.transporte.exception.UnsupportedTypeException;

public enum TipoTurno {
    MANANA(LocalTime.of(5, 0), LocalTime.of(12, 0)),
    TARDE(LocalTime.of(12, 0), LocalTime.of(18, 0)),
    NOCHE(LocalTime.of(18, 0), LocalTime.of(5, 0));

    private final LocalTime inicio;
    private final LocalTime fin;

    TipoTurno(LocalTime inicio, LocalTime fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public boolean incluye(LocalTime hora) {
        if (inicio.isBefore(fin)) {
            return !hora.isBefore(inicio) && hora.isBefore(fin);
        }
        // turno cruza medianoche (ej. NOCHE 18:00 -> 05:00)
        return !hora.isBefore(inicio) || hora.isBefore(fin);
    }

    public static TipoTurno desde(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new UnsupportedTypeException("El tipo de turno no puede ser nulo o vacío");
        }
        try {
            return TipoTurno.valueOf(nombre.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedTypeException("Tipo de turno desconocido: " + nombre);
        }
    }
}
