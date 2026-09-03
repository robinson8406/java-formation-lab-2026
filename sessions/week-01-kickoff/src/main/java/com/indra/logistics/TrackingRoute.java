package com.indra.logistics;

public record TrackingRoute(
        String origin,
        String destination
) {

    public TrackingRoute {
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("Origin no puede ser nulo ni vacío");
        }

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination no puede ser nulo ni vacío");
        }

        origin = origin.trim().toUpperCase();
        destination = destination.trim().toUpperCase();
    }
}
