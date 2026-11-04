package com.indra.transporte.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Bus {
    private final String placa;
    private final Tipo tipo;

    public Bus(String placa, String tipo) {
        this.placa = placa;
        this.tipo = Tipo.fromValue(tipo);
    }

    public Bus(String placa, Tipo tipo) {
        this.placa = placa;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo != null ? tipo.getValor() : null;
    }
}
