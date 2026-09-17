package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void process_CashPayment_ReturnsCorrectResult() {
        PaymentRequest request = new PaymentRequest(new BigDecimal("100.00"), "CASH");
        PaymentResult result = paymentService.process(request);

        assertEquals("CASH", result.method());
        assertEquals(new BigDecimal("100.00"), result.amount());
        assertEquals(new BigDecimal("0.00"), result.fee());
        assertEquals(new BigDecimal("100.00"), result.total());
        assertEquals("Pago en efectivo registrado, sin comisión.", result.message());
    }

    @Test
    void process_VisaPayment_ReturnsCorrectResult() {
        PaymentRequest request = new PaymentRequest(new BigDecimal("200.00"), "VISA");
        PaymentResult result = paymentService.process(request);

        assertEquals("VISA", result.method());
        assertEquals(new BigDecimal("200.00"), result.amount());
        assertEquals(new BigDecimal("7.00"), result.fee());
        assertEquals(new BigDecimal("207.00"), result.total());
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", result.message());
    }

    @Test
    void process_UnknownMethod_ThrowsUnknownPaymentMethodException() {
        PaymentRequest request = new PaymentRequest(new BigDecimal("100.00"), "CRYPTO");

        assertThrows(UnknownPaymentMethodException.class, () -> paymentService.process(request));
    }

    @Test
    void creditCardTariff_ReturnsExpectedValue() {
        assertEquals(BigDecimal.valueOf(0.03), paymentService.creditCardTariff());
    }
}

