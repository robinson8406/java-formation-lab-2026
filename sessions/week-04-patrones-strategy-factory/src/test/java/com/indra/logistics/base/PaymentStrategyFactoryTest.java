package com.indra.logistics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.indra.logistics.base.factory.PaymentStrategyFactory;
import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;
import com.indra.logistics.base.strategy.PaymentStrategy;

class PaymentStrategyFactoryTest {

    private final PaymentStrategyFactory factory = new PaymentStrategyFactoryImpl();

    @Test
    void cashStrategy_shouldReturnZeroFeeAndExpectedMessage()
    {
        PaymentStrategy strategy = factory.getStrategy("CASH");

        assertEquals("CASH", strategy.methodCode());
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals("Pago en efectivo registrado, sin comisión.", strategy.confirmationMessage());
    }

    @Test
    void visaStrategy_shouldReturnStandardCommission()
    {
        PaymentStrategy strategy = factory.getStrategy("VISA");

        assertEquals("VISA", strategy.methodCode());
        assertEquals(new BigDecimal("7.00"), strategy.calculateFee(new BigDecimal("200.00")));
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.", strategy.confirmationMessage());
    }

    @Test
    void payPalStrategy_shouldReturnPlatformCommission()
    {
        PaymentStrategy strategy = factory.getStrategy("PAYPAL");

        assertEquals("PAYPAL", strategy.methodCode());
        assertEquals(new BigDecimal("2.00"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", strategy.confirmationMessage());
    }

    @Test
    void bankTransferStrategy_shouldReturnBankCommission()
    {
        PaymentStrategy strategy = factory.getStrategy("BANK_TRANSFER");

        assertEquals("BANK_TRANSFER", strategy.methodCode());
        assertEquals(new BigDecimal("2.50"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.", strategy.confirmationMessage());
    }

    @Test
    void masterCardStrategy_shouldReturnFeeAboveThreshold()
    {
        PaymentStrategy strategy = factory.getStrategy("MASTERCARD");

        assertEquals("MASTERCARD", strategy.methodCode());
        assertEquals(new BigDecimal("3.00"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals("Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.", strategy.confirmationMessage());
    }

    @Test
    void debitCardStrategy_shouldReturnDebitCommission()
    {
        PaymentStrategy strategy = factory.getStrategy("DEBIT_CARD");

        assertEquals("DEBIT_CARD", strategy.methodCode());
        assertEquals(new BigDecimal("4.00"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.", strategy.confirmationMessage());
    }

    @Test
    void amexStrategy_shouldReturnFeeAboveThreshold()
    {
        PaymentStrategy strategy = factory.getStrategy("AMEX");

        assertEquals("AMEX", strategy.methodCode());
        assertEquals(new BigDecimal("3.00"), strategy.calculateFee(new BigDecimal("100.00")));
        assertEquals("Pago con tarjeta American Express procesado, se aplica comisión bancaria.", strategy.confirmationMessage());
    }

    @Test
    void factory_shouldThrowForUnknownMethod()
    {
        UnknownPaymentMethodException exception = assertThrows(
                UnknownPaymentMethodException.class,
                () -> factory.getStrategy("CRISTOMONEDAS")
        );

        assertEquals("método de pago 'CRISTOMONEDAS' no soportado", exception.getMessage());
    }
}
