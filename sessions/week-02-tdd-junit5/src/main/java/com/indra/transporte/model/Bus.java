package com.indra.transporte.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Bus {

    @EqualsAndHashCode.Include
    private final String placa;

    private final TipoVehiculoRuta tipo;
}
