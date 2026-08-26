package com.indra.logistics;

public record TrackingRoute(String origin, String destination) {

    public TrackingRoute {
        validateParameter(origin, "origin");
        validateParameter(destination, "destination");
    }

    private static void validateParameter(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(parameterName + " must not be null or blank");
        }
    }
}