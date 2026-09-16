package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VisaPaymentTest {

    private final VisaPayment payment = new VisaPayment();

    @Test
    void methodCode() {
        assertEquals(PaymentMethod.VISA, payment.methodCode());
    }

    @ParameterizedTest
    @CsvSource({"80.00, 0", "100.00, 3.50", "150.00, 5.25"})
    void calculateFee(String amount, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), payment.calculateFee(new BigDecimal(amount)));
    }

    @Test
    void confirmationMessage() {
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.",
                payment.confirmationMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "80.00, 'Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.'",
            "100.00, 'Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.'"
    })
    void confirmationMessageByAmount(String amount, String expectedMessage) {
        assertEquals(expectedMessage, payment.confirmationMessage(new BigDecimal(amount)));
    }
}