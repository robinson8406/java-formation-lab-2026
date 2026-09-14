package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BankTransferPaymentTest {

    private final BankTransferPayment strategy = new BankTransferPayment();

    @Test
    @DisplayName("BANK_TRANSFER debe tener el código de método 'BANK_TRANSFER'")
    void methodCode_ReturnsBankTransfer() {
        assertEquals("BANK_TRANSFER", strategy.methodCode());
    }

    @ParameterizedTest(name = "Monto {0} -> Comisión {1}")
    @CsvSource({
        "100.00, 2.50",
        "200.00, 5.00",
        "40.00, 1.00"
    })
    @DisplayName("Aplica comisión del 2.5% sobre cualquier monto")
    void calculateFee_AppliesTwoPointFivePercent(String amount, String expectedFee) {
        BigDecimal fee = strategy.calculateFee(new BigDecimal(amount));
        assertEquals(new BigDecimal(expectedFee), fee);
    }

    @Test
    @DisplayName("Monto nulo retorna fee 0.00")
    void calculateFee_NullAmount_ReturnsZero() {
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(null));
    }

    @Test
    @DisplayName("Mensaje de confirmación de transferencia bancaria")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.", strategy.confirmationMessage());
    }
}
