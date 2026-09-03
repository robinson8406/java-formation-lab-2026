package com.indra.logistics;

import java.security.SecureRandom;

public class TrackingIdGenerator {

    private static final String ALPHANUMERIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_SUFFIX_LENGTH = 8;
    private static final String ID_SEPARATOR = "-";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */
    public String generate(String origin, String destination) {
        TrackingRoute route = new TrackingRoute(origin, destination);
        return route.origin() + ID_SEPARATOR + route.destination() + ID_SEPARATOR
                + generateRandomSuffix();
    }

    private String generateRandomSuffix() {
        StringBuilder suffix = new StringBuilder(RANDOM_SUFFIX_LENGTH);
        for (int i = 0; i < RANDOM_SUFFIX_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(ALPHANUMERIC_CHARACTERS.length());
            suffix.append(ALPHANUMERIC_CHARACTERS.charAt(randomIndex));
        }
        return suffix.toString();
    }
}