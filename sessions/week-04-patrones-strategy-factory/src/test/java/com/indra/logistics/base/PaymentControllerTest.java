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
    void getFee_PaymentMethodWithAmount1_Success() {
        PaymentResult result = paymentController.getFee("MASTER_CARD", new BigDecimal("200.00"));

        assertEquals("MASTER_CARD", result.method());
        assertEquals(new BigDecimal("6.00"), result.fee());
        assertEquals(new BigDecimal("206.00"), result.total());
        assertEquals("Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount2_Success() {
        PaymentResult result = paymentController.getFee("AMEX", new BigDecimal("200.00"));

        assertEquals("AMEX", result.method());
        assertEquals(new BigDecimal("6.00"), result.fee());
        assertEquals(new BigDecimal("206.00"), result.total());
        assertEquals("Pago con tarjeta American Express procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount3_Success() {
        PaymentResult result = paymentController.getFee("BANK_TRANSFER", new BigDecimal("200.00"));

        assertEquals("BANK_TRANSFER", result.method());
        assertEquals(new BigDecimal("5.00"), result.fee());
        assertEquals(new BigDecimal("205.00"), result.total());
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount4_Success() {
        PaymentResult result = paymentController.getFee("DEBIT_CARD", new BigDecimal("200.00"));

        assertEquals("DEBIT_CARD", result.method());
        assertEquals(new BigDecimal("8.00"), result.fee());
        assertEquals(new BigDecimal("208.00"), result.total());
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount5_Success() {
        PaymentResult result = paymentController.getFee("PAYPAL", new BigDecimal("200.00"));

        assertEquals("PAYPAL", result.method());
        assertEquals(new BigDecimal("4.00"), result.fee());
        assertEquals(new BigDecimal("204.00"), result.total());
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", result.message());
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
