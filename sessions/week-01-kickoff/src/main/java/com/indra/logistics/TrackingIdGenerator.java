package com.indra.logistics;

public class TrackingIdGenerator {

    record TrackingId(String origin, String destination, String uniqueId) {
        @Override
        public String toString() {
            return origin + "-" + destination + "-" + uniqueId;
        }
    }

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */
    public String generate(String origin, String destination) throws IllegalArgumentException {
        if (origin == null || origin.isEmpty() ) {
            throw new IllegalArgumentException("El origen no puede ser nulo o vacío");
        }
        if (destination == null || destination.isEmpty() ) {
            throw new IllegalArgumentException("El destino no puede ser nulo o vacío");
        }
        TrackingId trackingId = new TrackingId(origin, destination, generateUniqueAlphaNumericId());
        return trackingId.toString();
    }

    public String generateUniqueAlphaNumericId() {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int randomIndex = (int) (Math.random() * chars.length);
            sb.append(chars[randomIndex]);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        TrackingIdGenerator generator = new TrackingIdGenerator();
        String origin = "BOG";
        String destination = "MED";
        String trackingId = generator.generate(origin, destination);
        System.out.println("Generated Tracking ID: " + trackingId);
    }
}