package com.indra.transporte.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Ruta {
    private final TipoVehiculoRuta tipo;
    private final String codigo;
    private final String origen;
    private final String destino;
}
