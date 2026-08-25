package com.indra.logistics;

import java.util.Random;

public class TrackingIdGenerator {


    private Random random = new Random();
    /**
     * Genera un ID de seguimiento con formato ORIG-DEST-XXXXXXXX
     * @param origin  código de origen (ej: "BOG")
     * @param destination código de destino (ej: "MED")
     * @return ID único de seguimiento
     * @throws Exception 
     */
    public String generate(String origin, String destination) throws Exception {
    	if(origin == null || destination == null) {
    		throw new IllegalArgumentException("fields cannot be null");
    	}
    	String random = generateRandom(8);
    	return origin.concat("-").concat(destination).concat("-").concat(random);
    }
    
    private String generateRandom(int reqLength) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder reqString = new StringBuilder();

        for (int i = 0; i < reqLength; i++) {
            int index = random.nextInt(alphabet.length());
            reqString.append(alphabet.charAt(index));
        }
        return reqString.toString();
    }
}