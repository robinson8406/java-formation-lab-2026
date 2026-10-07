package com.indra.logistics;

import java.security.SecureRandom;

public class TrackingIdGenerator {

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     *  
     */
    record Route(String origin, String destination) {};
    private final SecureRandom random = new SecureRandom();

    public String generate(String origin, String destination) {

        Route route = new Route(origin, destination); 
        validateNotBlank(route);
        
        return route.origin.toUpperCase() + "-" + route.destination.toUpperCase() + "-" + randomAlphaNumeric();
    }

    private void validateNotBlank(Route codes){
        if (codes.origin == null || codes.origin.isBlank()) {
            throw new IllegalArgumentException(codes.origin + " No puede ser null o Vacio");
        }
        if (codes.destination == null || codes.destination.isBlank()) {
            throw new IllegalArgumentException(codes.destination + " No puede ser null o Vacio");
        }
    }

    private String randomAlphaNumeric(){
        StringBuilder sb = new StringBuilder(AppsConstants.RANDOM_PART_LENGTH);
        for(int i = 0; i < AppsConstants.RANDOM_PART_LENGTH; i++){
            sb.append(AppsConstants.ALPHANUMERIC.charAt(random.nextInt(AppsConstants.ALPHANUMERIC.length())));
        }

        return sb.toString();
    }
}