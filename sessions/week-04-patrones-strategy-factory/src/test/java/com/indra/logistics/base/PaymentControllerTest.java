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
    void getFee_PaymentMethodWithAmount_PayPal_Success() {
        PaymentResult result = paymentController.getFee("PAYPAL", new BigDecimal("100.00"));

        assertEquals("PAYPAL", result.method());
        assertEquals(new BigDecimal("2.00"), result.fee());
        assertEquals(new BigDecimal("102.00"), result.total());
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", result.message());
    }
    
    @Test
    void getFee_PaymentMethodWithAmount_BankTransfer_Success() {
        PaymentResult result = paymentController.getFee("BANK_TRANSFER", new BigDecimal("100.00"));

        assertEquals("BANK_TRANSFER", result.method());
        assertEquals(new BigDecimal("2.50"), result.fee());
        assertEquals(new BigDecimal("102.50"), result.total());
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount_MasterCard_Success() {
        PaymentResult result = paymentController.getFee("MASTERCARD", new BigDecimal("200.00"));

        assertEquals("MASTERCARD", result.method());
        assertEquals(new BigDecimal("6.00"), result.fee());
        assertEquals(new BigDecimal("206.00"), result.total());
        assertEquals("Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount_DebitCard_Success() {
        PaymentResult result = paymentController.getFee("DEBIT_CARD", new BigDecimal("150.00"));

        assertEquals("DEBIT_CARD", result.method());
        assertEquals(new BigDecimal("6.00"), result.fee());
        assertEquals(new BigDecimal("156.00"), result.total());
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void getFee_PaymentMethodWithAmount_Amex_Success() {
        PaymentResult result = paymentController.getFee("AMEX", new BigDecimal("300.00"));

        assertEquals("AMEX", result.method());
        assertEquals(new BigDecimal("9.00"), result.fee());
        assertEquals(new BigDecimal("309.00"), result.total());
        assertEquals("Pago con tarjeta American Express procesado, se aplica comisión bancaria.", result.message());
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
