package com.indra.logistics;

import java.util.Random;

public class TrackingIdGenerator {

    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     */
    public String generate(String origin, String destination) {

        final int ID_LENGTH = 8;

        if (isEmpty(origin) || isEmpty(destination)) {
            throw new IllegalArgumentException("Origin or destination cannot be null");
        }

        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();

        StringBuilder id = new StringBuilder();

        for (int i = 0; i < ID_LENGTH; i++) {
            int posicion = random.nextInt(caracteres.length());
            id.append(caracteres.charAt(posicion));
        }
        
        return origin + "-" + destination + "-" + id.toString();
    }

    public boolean isEmpty(String str){
        return str == null || str.isEmpty() || str.isBlank();
    }
}