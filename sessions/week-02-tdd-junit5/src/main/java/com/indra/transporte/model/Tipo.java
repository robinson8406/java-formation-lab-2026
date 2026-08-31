package com.indra.transporte.model;

import lombok.Getter;

@Getter
public enum Tipo {
    ELECTRIC("Electric"),
    DIESEL("Diesel"),
    GENERAL("General");

    private final String name;

    Tipo(String name) {
        this.name = name;
    }

    public static Tipo fromValue(String name) {
        if (name == null) {
            return null;
        }

        for (Tipo tipo : values()) {
            if (tipo.getName().equalsIgnoreCase(name)) {
                return tipo;
            }
        }

        return null;
    }

    public static boolean isSupported(String name) {
        return fromValue(name) != null;
    }

}
