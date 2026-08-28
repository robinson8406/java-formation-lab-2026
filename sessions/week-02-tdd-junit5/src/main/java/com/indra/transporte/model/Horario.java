package com.indra.transporte.model;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class Horario {
    private final Bus bus;
    private final Ruta ruta;
    private final LocalTime horaSalida;
    private final LocalTime horaLlegada;
}
