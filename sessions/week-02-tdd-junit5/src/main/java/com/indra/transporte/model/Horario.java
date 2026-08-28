package com.indra.transporte.model;

import java.time.LocalTime;

public record Horario(Bus bus, Ruta ruta, LocalTime horaSalida, LocalTime horaLlegada) {
}
