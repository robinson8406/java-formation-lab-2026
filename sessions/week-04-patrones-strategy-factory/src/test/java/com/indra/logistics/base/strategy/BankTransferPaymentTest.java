package com.indra.logistics.base.strategy;

import com.indra.logistics.base.util.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankTransferPaymentTest {

    private final BankTransferPayment payment = new BankTransferPayment();

    @Test
    void methodCode() {
        assertEquals(PaymentMethod.BANK_TRANSFER, payment.methodCode());
    }

    @ParameterizedTest
    @CsvSource({"100.00, 2.50", "150.00, 3.75", "80.00, 2.00"})
    void calculateFee(String amount, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), payment.calculateFee(new BigDecimal(amount)));
    }

    @Test
    void confirmationMessage() {
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.",
                payment.confirmationMessage());
    }
}