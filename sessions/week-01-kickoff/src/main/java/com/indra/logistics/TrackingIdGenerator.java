package com.indra.logistics;

import java.security.SecureRandom;
import java.util.Objects;

public class TrackingIdGenerator {

    private static final int RANDOM_PART_LENGTH = 8;
    private static final String RANDOM_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final SecureRandom random;

    public TrackingIdGenerator() {
        this(new SecureRandom());
    }

    TrackingIdGenerator(SecureRandom random) {
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    public String generate(String origin, String destination) {
        validateText("origin", origin);
        validateText("destination", destination);

        return "%s-%s-%s".formatted(origin, destination, generateRandomPart());
    }

    private static void validateText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
    }

    private String generateRandomPart() {
        StringBuilder randomPart = new StringBuilder(RANDOM_PART_LENGTH);
        for (int index = 0; index < RANDOM_PART_LENGTH; index++) {
            int characterIndex = random.nextInt(RANDOM_CHARACTERS.length());
            randomPart.append(RANDOM_CHARACTERS.charAt(characterIndex));
        }
        return randomPart.toString();
    }
}