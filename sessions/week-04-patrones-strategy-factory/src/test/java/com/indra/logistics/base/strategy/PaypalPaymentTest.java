package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaypalPayment")
class PaypalPaymentTest {

    private final PaypalPayment strategy = new PaypalPayment();

    @Test
    @DisplayName("El código de método debe ser PAYPAL")
    void methodCode_ReturnsPaypal() {
        assertEquals("PAYPAL", strategy.methodCode());
    }

    @Test
    @DisplayName("La comisión debe ser el 2% del monto")
    void calculateFee_AppliesTwoPercent() {
        assertEquals(new BigDecimal("4.00"), strategy.calculateFee(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("El mensaje debe indicar que se aplicó comisión de plataforma")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", strategy.confirmationMessage());
    }
}