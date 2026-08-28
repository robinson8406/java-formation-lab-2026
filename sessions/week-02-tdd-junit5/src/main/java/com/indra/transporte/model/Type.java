package com.indra.transporte.model;

import lombok.Getter;

@Getter
public enum Type {

    DIESEL("Diesel"),
    ELECTRIC("Electric");

    private final String name;

    Type(String name) {
        this.name = name;
    }

}
