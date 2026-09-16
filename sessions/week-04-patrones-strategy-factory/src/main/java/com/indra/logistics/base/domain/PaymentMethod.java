package com.indra.logistics.base.domain;

import com.indra.logistics.base.UnknownPaymentMethodException;

public enum PaymentMethod {
    CASH,
    VISA,
    PAYPAL,
    BANK_TRANSFER,
    MASTERCARD,
    DEBIT_CARD,
    AMEX;

    public static PaymentMethod fromMethodCode(String methodCode) {
        return switch (methodCode.toUpperCase()) {
            case "CASH" -> CASH;
            case "VISA" -> VISA;
            case "PAYPAL" -> PAYPAL;
            case "BANK_TRANSFER" -> BANK_TRANSFER;
            case "MASTERCARD" -> MASTERCARD;
            case "DEBIT_CARD" -> DEBIT_CARD;
            case "AMEX" -> AMEX;
            // Agrega más casos según tus necesidades
            default -> throw new UnknownPaymentMethodException(methodCode);
        };
    }
}