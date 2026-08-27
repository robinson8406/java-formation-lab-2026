package com.indra.transporte.model;

import java.time.LocalTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Schedule {
    private final LocalTime horaSalida;
    private final LocalTime horaLlegada;
}
