package com.indra.logistics;
import java.security.SecureRandom;

public class TrackingIdGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int RANDOM_PART_LENGTH = 8;
	private final SecureRandom random = new SecureRandom();

	public String generate(String origin, String destination) {
		validateNotBlank(origin, "origin");
		validateNotBlank(destination, "destination");
		return origin.toUpperCase() + "-" + destination.toUpperCase() + "-" + randomAlphanumeric();
	}

	private void validateNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be null or blank");
		}
	}

	private String randomAlphanumeric() {
		StringBuilder sb = new StringBuilder(RANDOM_PART_LENGTH);
		for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
			sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
		}
		return sb.toString();
	}
}