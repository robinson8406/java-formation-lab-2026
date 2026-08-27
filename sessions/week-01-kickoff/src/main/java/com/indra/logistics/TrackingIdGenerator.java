package com.indra.logistics;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;

public class TrackingIdGenerator {

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */
    private static final int RANDOM_SUFFIX_LENGTH = 8;
    private static final String UPPERCASE_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate(String origin, String destination) {
        TrackingRoute route = new TrackingRoute(origin, destination);
        return "%s-%s-%s".formatted(route.origin(), route.destination(), generateRandomSuffix());
    }

    private String generateRandomSuffix() {
        StringBuilder suffix = new StringBuilder(RANDOM_SUFFIX_LENGTH);
        for (int index = 0; index < RANDOM_SUFFIX_LENGTH; index++) {
            int randomIndex = RANDOM.nextInt(UPPERCASE_ALPHANUMERIC.length());
            suffix.append(UPPERCASE_ALPHANUMERIC.charAt(randomIndex));
        }
        return suffix.toString();
    }

    private record TrackingRoute(String origin, String destination) {

        private TrackingRoute {
            origin = normalizeAndValidate(origin, "origin");
            destination = normalizeAndValidate(destination, "destination");
        }

        private static String normalizeAndValidate(String value, String fieldName) {
            if (Objects.isNull(value) || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " must not be null or blank");
            }
            return value.trim().toUpperCase(Locale.ROOT);
        }
    }
}