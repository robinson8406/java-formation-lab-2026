package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DebitCardPayment")
class DebitCardPaymentTest {

    private final DebitCardPayment strategy = new DebitCardPayment();

    @Test
    @DisplayName("El código de método debe ser DEBIT_CARD")
    void methodCode_ReturnsDebitCard() {
        assertEquals("DEBIT_CARD", strategy.methodCode());
    }

    @Test
    @DisplayName("La comisión debe ser el 4% del monto")
    void calculateFee_AppliesFourPercent() {
        assertEquals(new BigDecimal("8.00"), strategy.calculateFee(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("El mensaje debe indicar que se aplicó comisión bancaria")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals(
                "Pago con tarjeta de débito procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }
}