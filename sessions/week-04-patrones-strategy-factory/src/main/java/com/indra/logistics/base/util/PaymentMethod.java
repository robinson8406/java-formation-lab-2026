package com.indra.logistics.base.util;

import com.indra.logistics.base.UnknownPaymentMethodException;

public enum PaymentMethod {
    VISA,
    MASTERCARD,
    PAYPAL,
    CASH,
    BANK_TRANSFER,
    DEBIT_CARD,
    AMEX;

    public static PaymentMethod get(String value) {
        try {
            return PaymentMethod.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new UnknownPaymentMethodException(value);
        }
    }


}
