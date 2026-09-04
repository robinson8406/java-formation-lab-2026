package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PaymentControllerTest {

    private final PaymentController paymentController = new PaymentController();

    @Test
    void getFee_PaymentMethodWithoutAmount_Success() {
        PaymentResult result = paymentController.getFee("CASH");

        assertEquals("CASH", result.method());
        assertEquals(new BigDecimal("100"), result.amount());
        assertEquals(new BigDecimal("0.00"), result.fee());
        assertEquals(new BigDecimal("100.00"), result.total());
    }

    @Test
    void getFee_PaymentMethodWithAmount_Success() {
        PaymentResult result = paymentController.getFee("VISA", new BigDecimal("200.00"));

        assertEquals("VISA", result.method());
        assertEquals(new BigDecimal("7.00"), result.fee());
        assertEquals(new BigDecimal("207.00"), result.total());
    }

    @Test
    void getFee_InvalidPaymentMethod_BadRequest() {
        IllegalArgumentException exception = null;
        try {
            paymentController.getFee("CRYPTO", new BigDecimal("100.00"));
        } catch (IllegalArgumentException ex) {
            exception = ex;
        }
        ResponseEntity<Map<String, String>> response = paymentController.handleUnknownMethod(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "método de pago no soportado"), response.getBody());
    }
}
