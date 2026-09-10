package com.indra.logistics;

import java.util.UUID;

public class TrackingIdGenerator {
    public String generate(String origin, String destination) {
        if (origin == null || origin.isEmpty()) {
            throw new IllegalArgumentException("Origin cannot be null or empty");
        }
        if (destination == null || destination.isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be null or empty");
        }
        String trimmedOrigin = origin.trim();
        String trimmedDestination = destination.trim();
        // Generar un ID de 8 caracteres hexadecimales
        return trimmedOrigin + "-" + trimmedDestination + "-" + 
               UUID.randomUUID().toString().toUpperCase().substring(0, 8);
    }
}