package com.indra.logistics.base.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.AmexPayment;
import com.indra.logistics.base.strategy.BankTransferPayment;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.DebitCardPayment;
import com.indra.logistics.base.strategy.MastercardPayment;
import com.indra.logistics.base.strategy.PayPalPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.VisaPayment;

class PaymentStrategyFactoryTest {

    private final PaymentStrategyFactory factory = new PaymentStrategyFactoryImpl();

    @Test
    void getStrategy_Cash_ReturnsCashPayment() {
        PaymentStrategy strategy = factory.getStrategy("CASH");
        assertNotNull(strategy);
        assertInstanceOf(CashPayment.class, strategy);
    }

    @Test
    void getStrategy_Visa_ReturnsVisaPayment() {
        PaymentStrategy strategy = factory.getStrategy("VISA");
        assertNotNull(strategy);
        assertInstanceOf(VisaPayment.class, strategy);
    }

    @Test
    void getStrategy_PayPal_ReturnsPayPalPayment() {
        PaymentStrategy strategy = factory.getStrategy("PAYPAL");
        assertNotNull(strategy);
        assertInstanceOf(PayPalPayment.class, strategy);
    }

    @Test
    void getStrategy_BankTransfer_ReturnsBankTransferPayment() {
        PaymentStrategy strategy = factory.getStrategy("BANK_TRANSFER");
        assertNotNull(strategy);
        assertInstanceOf(BankTransferPayment.class, strategy);
    }

    @Test
    void getStrategy_Mastercard_ReturnsMastercardPayment() {
        PaymentStrategy strategy = factory.getStrategy("MASTERCARD");
        assertNotNull(strategy);
        assertInstanceOf(MastercardPayment.class, strategy);
    }

    @Test
    void getStrategy_DebitCard_ReturnsDebitCardPayment() {
        PaymentStrategy strategy = factory.getStrategy("DEBIT_CARD");
        assertNotNull(strategy);
        assertInstanceOf(DebitCardPayment.class, strategy);
    }

    @Test
    void getStrategy_Amex_ReturnsAmexPayment() {
        PaymentStrategy strategy = factory.getStrategy("AMEX");
        assertNotNull(strategy);
        assertInstanceOf(AmexPayment.class, strategy);
    }

    @Test
    void getStrategy_UnknownMethod_ThrowsUnknownPaymentMethodException() {
        UnknownPaymentMethodException exception = assertThrows(
                UnknownPaymentMethodException.class,
                () -> factory.getStrategy("CRYPTO")
        );
        assertEquals("método de pago 'CRYPTO' no soportado", exception.getMessage());
    }

    @Test
    void constructorWithList_RegistersStrategiesCorrectly() {
        PaymentStrategy customStrategy = new CashPayment();
        PaymentStrategyFactory customFactory = new PaymentStrategyFactoryImpl(List.of(customStrategy));

        PaymentStrategy resolved = customFactory.getStrategy("CASH");
        assertNotNull(resolved);
        assertInstanceOf(CashPayment.class, resolved);
    }
}

