package com.indra.logistics.base;

public class UnknownPaymentMethodException extends RuntimeException {

    public UnknownPaymentMethodException(String method) {
        super("método de pago '%s' no soportado".formatted(method));
    }
}
