package com.indra.transporte.model.Enums;

public enum RouteType {
    Electric("Electric"),
    Diesel("Diesel"),
    Gas("Gas");

    private final String type;

    RouteType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
