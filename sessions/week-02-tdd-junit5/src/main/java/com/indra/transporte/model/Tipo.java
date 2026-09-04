package com.indra.transporte.model;

public enum Tipo {
    ELECTRIC("Electric"),
    DIESEL("Diesel"),
    GENERAL("General");

    private final String valor;

    Tipo(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static Tipo fromValue(String valor) {
        if (valor == null) {
            return null;
        }

        for (Tipo tipo : values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        return null;
    }

    public static boolean isSupported(String valor) {
        return fromValue(valor) != null;
    }
}
