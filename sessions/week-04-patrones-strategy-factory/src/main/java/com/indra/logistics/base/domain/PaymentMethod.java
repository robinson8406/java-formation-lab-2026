package com.indra.logistics.base.domain;

import com.indra.logistics.base.UnknownPaymentMethodException;

public enum PaymentMethod {
    CASH,
    VISA,
    PAYPAL;

    public static PaymentMethod fromMethodCode(String methodCode) {
        return switch (methodCode.toUpperCase()) {
            case "CASH" -> CASH;
            case "VISA" -> VISA;
            case "PAYPAL" -> PAYPAL;
            // Agrega más casos según tus necesidades
            default -> throw new UnknownPaymentMethodException(methodCode);
        };
    }
}