package com.indra.transporte.model;

public enum Tipo {


    ELECTRIC("Electric"),
    DIESEL("Diesel");

    private final String value;

    Tipo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isSupported(String tipo) {
        for (Tipo t : Tipo.values()) {
            if (t.getValue().equals(tipo)) {
                return true;
            }
        }
        return false;
    }

}
