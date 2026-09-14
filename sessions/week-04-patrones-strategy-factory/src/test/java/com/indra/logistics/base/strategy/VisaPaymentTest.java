package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VisaPaymentTest {

    private final VisaPayment strategy = new VisaPayment();

    @Test
    @DisplayName("VISA debe tener el código de método 'VISA'")
    void methodCode_ReturnsVisa() {
        assertEquals("VISA", strategy.methodCode());
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
            assertEquals("Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.", msg);
        }
    }

    @Nested
    @DisplayName("Cuando el monto es mayor o igual a 100")
    class CuandoMontoMayorOIgualA100 {

        @ParameterizedTest(name = "Monto {0} -> Comisión {1}")
        @CsvSource({
            "100.00, 3.50",
            "200.00, 7.00",
            "150.00, 5.25"
        })
        @DisplayName("Aplica comisión del 3.5% (tarifa base 3% + 0.5%)")
        void calculateFee_AboveThreshold(String amount, String expectedFee) {
            BigDecimal fee = strategy.calculateFee(new BigDecimal(amount));
            assertEquals(new BigDecimal(expectedFee), fee);
        }

        @Test
        @DisplayName("Mensaje indica que se aplica comisión bancaria")
        void confirmationMessage_AboveThreshold() {
            String msg = strategy.confirmationMessage(new BigDecimal("200.00"));
            assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", msg);
        }
    }
}
