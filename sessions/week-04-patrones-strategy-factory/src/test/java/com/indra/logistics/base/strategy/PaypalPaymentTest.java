package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaypalPaymentTest {

    private final PaypalPayment payment = new PaypalPayment();

    @Test
    void methodCode() {
        assertEquals(PaymentMethod.PAYPAL, payment.methodCode());
    }

    @ParameterizedTest
    @CsvSource({"100.00, 2.00", "150.00, 3.00", "80.00, 1.60"})
    void calculateFee(String amount, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), payment.calculateFee(new BigDecimal(amount)));
    }

    @Test
    void confirmationMessage() {
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.",
                payment.confirmationMessage());
    }
}