package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaypalPaymentTest {

    private final PaypalPayment strategy = new PaypalPayment();

    @Test
    @DisplayName("PAYPAL debe tener el código de método 'PAYPAL'")
    void methodCode_ReturnsPaypal() {
        assertEquals("PAYPAL", strategy.methodCode());
    }

    @ParameterizedTest(name = "Monto {0} -> Comisión {1}")
    @CsvSource({
        "100.00, 2.00",
        "50.00, 1.00",
        "250.00, 5.00"
    })
    @DisplayName("Aplica comisión del 2.0% sobre cualquier monto")
    void calculateFee_AppliesTwoPercent(String amount, String expectedFee) {
        BigDecimal fee = strategy.calculateFee(new BigDecimal(amount));
        assertEquals(new BigDecimal(expectedFee), fee);
    }

    @Test
    @DisplayName("Monto nulo retorna fee 0.00")
    void calculateFee_NullAmount_ReturnsZero() {
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(null));
    }

    @Test
    @DisplayName("Mensaje de confirmación de PayPal")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", strategy.confirmationMessage());
    }
}
