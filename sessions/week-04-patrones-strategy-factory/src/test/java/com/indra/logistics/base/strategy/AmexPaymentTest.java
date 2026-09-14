package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AmexPaymentTest {

    private final AmexPayment strategy = new AmexPayment();

    @Test
    @DisplayName("AMEX debe tener el código de método 'AMEX'")
    void methodCode_ReturnsAmex() {
        assertEquals("AMEX", strategy.methodCode());
    }

    @Nested
    @DisplayName("Cuando el monto es menor a 100")
    class CuandoMontoMenorA100 {

        @Test
        @DisplayName("No aplica comisión (fee 0.00)")
        void calculateFee_UnderThreshold_ReturnsZero() {
            BigDecimal fee = strategy.calculateFee(new BigDecimal("99.99"));
            assertEquals(new BigDecimal("0.00"), fee);
        }

        @Test
        @DisplayName("Mensaje indica que no aplica comisión bancaria")
        void confirmationMessage_UnderThreshold() {
            String msg = strategy.confirmationMessage(new BigDecimal("50.00"));
            assertEquals("Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.", msg);
        }
    }

    @Nested
    @DisplayName("Cuando el monto es mayor o igual a 100")
    class CuandoMontoMayorOIgualA100 {

        @ParameterizedTest(name = "Monto {0} -> Comisión {1}")
        @CsvSource({
            "100.00, 3.00",
            "200.00, 6.00",
            "300.00, 9.00"
        })
        @DisplayName("Aplica comisión del 3.0%")
        void calculateFee_AboveThreshold(String amount, String expectedFee) {
            BigDecimal fee = strategy.calculateFee(new BigDecimal(amount));
            assertEquals(new BigDecimal(expectedFee), fee);
        }

        @Test
        @DisplayName("Mensaje indica que se aplica comisión bancaria")
        void confirmationMessage_AboveThreshold() {
            String msg = strategy.confirmationMessage(new BigDecimal("100.00"));
            assertEquals("Pago con tarjeta American Express procesado, se aplica comisión bancaria.", msg);
        }
    }
}
