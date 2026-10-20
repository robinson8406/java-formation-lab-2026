package com.indra.logistics;

import java.security.SecureRandom;
import java.util.Objects;

public class TrackingIdGenerator {

    private static final int RANDOM_PART_LENGTH = 8;
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(String origin, String destination) {
        Route route = new Route(origin, destination);
        return generate(route);
    }

    public String generate(Route route) {
        Objects.requireNonNull(route, "La ruta no puede ser nula");
        String randomSuffix = generateRandomAlphanumeric(RANDOM_PART_LENGTH);
        return route.origin() + "-" + route.destination() + "-" + randomSuffix;
    }

    private String generateRandomAlphanumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(ALPHANUMERIC_CHARS.length());
            builder.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        return builder.toString();
    }
}