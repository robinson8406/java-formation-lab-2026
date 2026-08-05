package com.indra.logistics;

import java.security.SecureRandom;

public class TrackingIdGenerator {

    private static final int RANDOM_PART_LENGTH = 8;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */
    public String generate(String origin, String destination) {
        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException("origin no puede ser nulo o vacío");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("destination no puede ser nulo o vacío");
        }

        return origin.toUpperCase() + "-" + destination.toUpperCase() + "-" + randomSuffix();
    }

    private String randomSuffix() {
        StringBuilder suffix = new StringBuilder(RANDOM_PART_LENGTH);
        for (int index = 0; index < RANDOM_PART_LENGTH; index++) {
            suffix.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return suffix.toString();
    }
}