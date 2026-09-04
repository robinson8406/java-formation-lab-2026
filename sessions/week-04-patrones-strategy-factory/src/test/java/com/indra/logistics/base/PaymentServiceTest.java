package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void shouldCalculateVisaFeeAndTotal() {
        PaymentResult result = paymentService.process(request("VISA", "100.00"));

        assertPayment(result, "VISA", "100.00", "3.50", "103.50");
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void shouldCalculatePaypalFeeAndTotal() {
        PaymentResult result = paymentService.process(request("PAYPAL", "250.00"));

        assertPayment(result, "PAYPAL", "250.00", "5.00", "255.00");
    }

    @Test
    void shouldRegisterCashWithoutFee() {
        PaymentResult result = paymentService.process(request("CASH", "80.00"));

        assertPayment(result, "CASH", "80.00", "0.00", "80.00");
        assertEquals("Pago en efectivo registrado, sin comisión.", result.message());
    }

    @Test
    void shouldCalculateBankTransferFee() {
        PaymentResult result = paymentService.process(request("BANK_TRANSFER", "200.00"));

        assertPayment(result, "BANK_TRANSFER", "200.00", "5.00", "205.00");
    }

    @Test
    void shouldRoundFeeToTwoDecimalPlaces() {
        PaymentResult result = paymentService.process(request("AMEX", "100.01"));

        assertPayment(result, "AMEX", "100.01", "3.20", "103.21");
    }

    @Test
    void shouldRejectUnsupportedPaymentMethod() {
        PaymentRequest request = request("CRYPTO", "100.00");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
            () -> paymentService.process(request));

        assertEquals("método de pago no soportado (el if-else no lo contempla)", exception.getMessage());
    }

    private static PaymentRequest request(String method, String amount) {
        return new PaymentRequest(new BigDecimal(amount), method);
    }

    private static void assertPayment(
            PaymentResult result, String method, String amount, String fee, String total) {
        assertEquals(method, result.method());
        assertEquals(new BigDecimal(amount), result.amount());
        assertEquals(new BigDecimal(fee), result.fee());
        assertEquals(new BigDecimal(total), result.total());
    }
}
