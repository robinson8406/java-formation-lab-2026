package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CashPaymentTest {

    private final CashPayment strategy = new CashPayment();

    @Test
    @DisplayName("CASH debe tener el código de método 'CASH'")
    void methodCode_ReturnsCash() {
        assertEquals("CASH", strategy.methodCode());
    }

    @Test
    @DisplayName("CASH debe retornar fee 0.00 independientemente del monto")
    void calculateFee_ReturnsZero() {
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("500.00")));
    }

    @Test
    @DisplayName("CASH debe retornar mensaje de confirmación sin comisión")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago en efectivo registrado, sin comisión.", strategy.confirmationMessage());
    }
}
