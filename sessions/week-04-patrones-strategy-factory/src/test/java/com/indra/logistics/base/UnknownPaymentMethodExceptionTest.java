package com.indra.logistics.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnknownPaymentMethodExceptionTest {

    @Test
    void testExceptionMessage() {
        String method = "INVALID_METHOD";
        UnknownPaymentMethodException exception = new UnknownPaymentMethodException(method);
        assertEquals("método de pago 'INVALID_METHOD' no soportado", exception.getMessage());
    }

}