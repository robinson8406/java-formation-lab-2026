package com.indra.retail;

public class OrderNotifier {

    public String notifyCustomer(String customerEmail, String message) {
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException("customerEmail no puede ser nulo o vacío");
        }
        return "[EMAIL a " + customerEmail + "] " + message;
    }
}
