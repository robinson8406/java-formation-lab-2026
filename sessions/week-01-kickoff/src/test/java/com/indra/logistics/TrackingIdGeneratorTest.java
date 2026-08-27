package com.indra.logistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdGeneratorTest {

    private static final Pattern TRACKING_ID_PATTERN = Pattern.compile("^[A-Z]+-[A-Z]+-[A-Z0-9]{8}$");

    private final TrackingIdGenerator trackingIdGenerator = new TrackingIdGenerator();

    @Test
    @DisplayName("Should generate a tracking id with expected format")
    void shouldGenerateTrackingIdWithExpectedFormat() {
        String trackingId = trackingIdGenerator.generate("CTG", "BOG");
        assertTrue(TRACKING_ID_PATTERN.matcher(trackingId).matches());
    }

    @Test
    @DisplayName("Should normalize origin and destination to uppercase")
    void shouldNormalizeOriginAndDestinationToUppercase() {
        String trackingId = trackingIdGenerator.generate(" CTG ", "BOG");
        assertTrue(trackingId.matches("^CTG-BOG-[A-Z0-9]{8}$"));
    }

    @ParameterizedTest(name = "Should throw IllegalArgumentException for origin={0}, destination={1}")
    @MethodSource("invalidParameters")
    void shouldThrowIllegalArgumentExceptionWhenOriginOrDestinationIsNullOrBlank(String origin, String destination) {
        assertThrows(IllegalArgumentException.class, () -> trackingIdGenerator.generate(origin, destination));
    }

    private static Stream<Arguments> invalidParameters() {
        return Stream.of(
                Arguments.of(null, "BOG"),
                Arguments.of("CTG", null),
                Arguments.of("", "BOG"),
                Arguments.of("CTG", ""),
                Arguments.of("   ", "BOG"),
                Arguments.of("CTG", "   ")
        );
    }
}