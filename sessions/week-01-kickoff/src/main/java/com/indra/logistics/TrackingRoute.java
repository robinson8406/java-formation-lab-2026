package com.indra.logistics;

public record TrackingRoute(String origin, String destination) {

    public static final String ERROR_ORIGIN_INVALID = "El origen no puede ser nulo o vacío";
    public static final String ERROR_DESTINATION_INVALID = "El destino no puede ser nulo o vacío";
    public TrackingRoute {

        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException(ERROR_ORIGIN_INVALID);
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(ERROR_DESTINATION_INVALID);
        }

        origin = normalize(origin);
        destination = normalize(destination);
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase();
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }
}
