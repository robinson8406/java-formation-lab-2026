package com.indra.logistics;

import java.security.SecureRandom;
import java.util.Locale;

public class TrackingIdGenerator {

    private static final String TRACKING_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_PART_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public String generate(String origin, String destination) {
        TrackingRoute route = new TrackingRoute(origin, destination);

        StringBuilder randomPart = new StringBuilder(RANDOM_PART_LENGTH);
        for (int index = 0; index < RANDOM_PART_LENGTH; index++) {
            int characterIndex = random.nextInt(TRACKING_CHARACTERS.length());
            randomPart.append(TRACKING_CHARACTERS.charAt(characterIndex));
        }

        return String.format(Locale.ROOT, "%s-%s-%s",
                route.origin().toUpperCase(Locale.ROOT),
                route.destination().toUpperCase(Locale.ROOT),
                randomPart);
    }
}