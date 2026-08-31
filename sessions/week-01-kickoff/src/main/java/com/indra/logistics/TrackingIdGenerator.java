package com.indra.logistics;

import java.security.SecureRandom;

public class TrackingIdGenerator {

    private static final int RANDOM_SUFFIX_LENGTH = 8;
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */
    public String generate(String origin, String destination) {
        
        var trackingRoute = new TrackingRoute(origin, destination);

        String randomSuffix = generateRandomAlphanumericSuffix(RANDOM_SUFFIX_LENGTH);

        return String.format("%s-%s-%s", trackingRoute.getOrigin(), trackingRoute.getDestination(), randomSuffix);
    }

    private String generateRandomAlphanumericSuffix(int length) {

        StringBuilder suffixBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            suffixBuilder.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        
        return suffixBuilder.toString();
    }
}