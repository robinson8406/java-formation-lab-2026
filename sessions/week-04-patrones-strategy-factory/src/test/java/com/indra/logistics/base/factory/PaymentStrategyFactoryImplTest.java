package com.indra.logistics.base.factory;

import com.indra.logistics.base.util.PaymentMethod;
import com.indra.logistics.base.strategy.AmexPayment;
import com.indra.logistics.base.strategy.BankTransferPayment;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.DebitCardPayment;
import com.indra.logistics.base.strategy.MastercardPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.PaypalPayment;
import com.indra.logistics.base.strategy.VisaPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStrategyFactoryImplTest {

    private final PaymentStrategyFactory factory = new PaymentStrategyFactoryImpl(java.util.List.of(
            new VisaPayment(),
            new MastercardPayment(),
            new PaypalPayment(),
            new CashPayment(),
            new BankTransferPayment(),
            new DebitCardPayment(),
            new AmexPayment()
    ));

    @ParameterizedTest
    @CsvSource({
            "VISA, VisaPayment",
            "MASTERCARD, MastercardPayment",
            "PAYPAL, PaypalPayment",
            "CASH, CashPayment",
            "BANK_TRANSFER, BankTransferPayment",
            "DEBIT_CARD, DebitCardPayment",
            "AMEX, AmexPayment"
    })
    void getStrategyReturnsStrategyForPaymentMethod(String method, String expectedStrategy) {
        PaymentStrategy strategy = factory.getStrategy(PaymentMethod.valueOf(method));

        assertNotNull(strategy);
        assertEquals(PaymentMethod.valueOf(method), strategy.methodCode());
        assertEquals(expectedStrategy, strategy.getClass().getSimpleName());
    }

    @Test
    void getStrategyReturnsNullForUnregisteredMethod() {
        assertNull(factory.getStrategy(null));
    }
}