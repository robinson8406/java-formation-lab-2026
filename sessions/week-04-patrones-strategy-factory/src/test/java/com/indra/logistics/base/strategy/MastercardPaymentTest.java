package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MastercardPaymentTest {

    private final MastercardPayment payment = new MastercardPayment();

    @Test
    void methodCode() {
        assertEquals(PaymentMethod.MASTERCARD, payment.methodCode());
    }

    @ParameterizedTest
    @CsvSource({"80.00, 0", "100.00, 3.00", "150.00, 4.50"})
    void calculateFee(String amount, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), payment.calculateFee(new BigDecimal(amount)));
    }

    @Test
    void confirmationMessage() {
        assertEquals("Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.",
                payment.confirmationMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "80.00, 'Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.'",
            "100.00, 'Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.'"
    })
    void confirmationMessageByAmount(String amount, String expectedMessage) {
        assertEquals(expectedMessage, payment.confirmationMessage(new BigDecimal(amount)));
    }
}