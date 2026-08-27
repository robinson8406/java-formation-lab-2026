package com.indra.transporte.model.Enums;

public enum BusType {
    Electric("Electric"),
    Diesel("Diesel"),
    Gas("Gas");

    private final String type;

    BusType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
