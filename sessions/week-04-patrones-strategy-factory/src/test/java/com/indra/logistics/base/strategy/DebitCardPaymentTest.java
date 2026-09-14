package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DebitCardPaymentTest {

    private final DebitCardPayment strategy = new DebitCardPayment();

    @Test
    @DisplayName("DEBIT_CARD debe tener el código de método 'DEBIT_CARD'")
    void methodCode_ReturnsDebitCard() {
        assertEquals("DEBIT_CARD", strategy.methodCode());
    }

    @ParameterizedTest(name = "Monto {0} -> Comisión {1}")
    @CsvSource({
        "100.00, 4.00",
        "50.00, 2.00",
        "250.00, 10.00"
    })
    @DisplayName("Aplica comisión del 4.0% sobre cualquier monto")
    void calculateFee_AppliesFourPercent(String amount, String expectedFee) {
        BigDecimal fee = strategy.calculateFee(new BigDecimal(amount));
        assertEquals(new BigDecimal(expectedFee), fee);
    }

    @Test
    @DisplayName("Monto nulo retorna fee 0.00")
    void calculateFee_NullAmount_ReturnsZero() {
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(null));
    }

    @Test
    @DisplayName("Mensaje de confirmación de tarjeta de débito")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.", strategy.confirmationMessage());
    }
}
