package com.indra.logistics;

import java.util.Locale;
import java.util.UUID;

public class TrackingIdGenerator {

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * 
     * @param origin      código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */

    private static final int RANDOM_PART_LENGTH = 8;

    public String generate(String origin, String destination) {
        validateRequiredParameter(origin, "origin");
        validateRequiredParameter(destination, "destination");
        String randomPart = UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, RANDOM_PART_LENGTH)
                .toUpperCase(Locale.ROOT);

        return String.format("%s-%s-%s", origin.toUpperCase(Locale.ROOT), destination.toUpperCase(Locale.ROOT),
                randomPart);
    }

    private static void validateRequiredParameter(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(parameterName + " is required");
        }
    }
}