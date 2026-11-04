package com.indra.transporte.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Ruta {
    private final Tipo tipo;
    private final String codigo;
    private final String origen;
    private final String destino;

    public Ruta(String tipo, String codigo, String origen, String destino) {
        this.tipo = Tipo.fromValue(tipo);
        this.codigo = codigo;
        this.origen = origen;
        this.destino = destino;
    }

    public Ruta(Tipo tipo, String codigo, String origen, String destino) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.origen = origen;
        this.destino = destino;
    }

    public String getTipo() {
        return tipo != null ? tipo.getValor() : null;
    }
}
