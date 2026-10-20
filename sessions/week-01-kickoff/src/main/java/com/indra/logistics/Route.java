package com.indra.logistics;

public record Route(String origin, String destination) {

    public Route {
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("El origen de la ruta no puede ser nulo ni estar vacío");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("El destino de la ruta no puede ser nulo ni estar vacío");
        }
        origin = origin.trim().toUpperCase();
        destination = destination.trim().toUpperCase();
    }
}
