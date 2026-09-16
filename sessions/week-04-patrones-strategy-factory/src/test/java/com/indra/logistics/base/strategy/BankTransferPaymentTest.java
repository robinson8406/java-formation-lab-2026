package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BankTransferPayment")
class BankTransferPaymentTest {

    private final BankTransferPayment strategy = new BankTransferPayment();

    @Test
    @DisplayName("El código de método debe ser BANK_TRANSFER")
    void methodCode_ReturnsBankTransfer() {
        assertEquals("BANK_TRANSFER", strategy.methodCode());
    }

    @Test
    @DisplayName("La comisión debe ser el 2.5% del monto")
    void calculateFee_AppliesTwoPointFivePercent() {
        assertEquals(new BigDecimal("5.00"), strategy.calculateFee(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("El mensaje debe indicar que se aplicó comisión bancaria")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals(
                "Pago por transferencia bancaria registrado, comisión bancaria aplicada.",
                strategy.confirmationMessage());
    }
}