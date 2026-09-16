package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DebitCardPaymentTest {

    private final DebitCardPayment payment = new DebitCardPayment();

    @Test
    void methodCode() {
        assertEquals(PaymentMethod.DEBIT_CARD, payment.methodCode());
    }

    @ParameterizedTest
    @CsvSource({"100.00, 4.00", "150.00, 6.00", "80.00, 3.20"})
    void calculateFee(String amount, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), payment.calculateFee(new BigDecimal(amount)));
    }

    @Test
    void confirmationMessage() {
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.",
                payment.confirmationMessage());
    }
}