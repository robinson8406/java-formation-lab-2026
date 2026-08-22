package com.indra.logistics;

import java.security.SecureRandom;

public class TrackingIdGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_PART_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param param parámetros de generación (origin y destination)
     * @return ID único de seguimiento
     */
    public String generate(GenerateParam param) {
        validateNotBlank(param.origin(), "origin");
        validateNotBlank(param.destination(), "destination");

        return param.origin().toUpperCase() + "-" + param.destination().toUpperCase() + "-" + randomAlphanumeric();
    }

    private void validateNotBlank(String value, String fieldName) {
        if (null == value || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    private String randomAlphanumeric() {
        var sb = new StringBuilder(RANDOM_PART_LENGTH);

        for (var i = 0; i < RANDOM_PART_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }

        return sb.toString();
    }

}