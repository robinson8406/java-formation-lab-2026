package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class PaymentControllerTest {

    private final PaymentController paymentController;

    PaymentControllerTest(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @Test
    void getFee_PaymentMethodWithoutAmount_Success() {
        PaymentResult result = paymentController.getFee("CASH");

        assertEquals("CASH", result.method());
        assertEquals(new BigDecimal("100"), result.amount());
        assertEquals(new BigDecimal("0.00"), result.fee());
        assertEquals(new BigDecimal("100.00"), result.total());
        assertEquals("Pago en efectivo registrado, sin comisión.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount_Success() {
        PaymentResult result = paymentController.getFee("VISA", new BigDecimal("200.00"));

        assertEquals("VISA", result.method());
        assertEquals(new BigDecimal("7.00"), result.fee());
        assertEquals(new BigDecimal("207.00"), result.total());
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void getFee_InvalidPaymentMethod_BadRequest() {
        UnknownPaymentMethodException exception = null;
        try {
            paymentController.getFee("CRYPTO", new BigDecimal("100.00"));
        } catch (UnknownPaymentMethodException ex) {
            exception = ex;
        }
        ResponseEntity<Map<String, String>> response = paymentController.handleUnknownMethod(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "método de pago 'CRYPTO' no soportado"), response.getBody());
    }
}
