package com.indra.logistics.base.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.PaymentStrategy;

@SpringBootTest
@DisplayName("PaymentStrategyFactoryImpl")
class PaymentStrategyFactoryImplTest {

    private final PaymentStrategyFactory factory;

    PaymentStrategyFactoryImplTest(PaymentStrategyFactory factory) {
        this.factory = factory;
    }

    @ParameterizedTest(name = "El método {0} debe resolver una estrategia con su mismo código")
    @DisplayName("Debe resolver correctamente cada método de pago soportado")
    @ValueSource(strings = {"CASH", "VISA", "MASTERCARD", "AMEX", "PAYPAL", "BANK_TRANSFER", "DEBIT_CARD"})
    void getStrategy_KnownMethod_ReturnsMatchingStrategy(String methodCode) {
        PaymentStrategy strategy = factory.getStrategy(methodCode);
        assertEquals(methodCode, strategy.methodCode());
    }

    @Test
    @DisplayName("Para VISA con monto menor al umbral, la comisión debe quedar exonerada")
    void getStrategy_Visa_AmountBelowThreshold_FeeIsWaived() {
        PaymentStrategy strategy = factory.getStrategy("VISA");

        BigDecimal fee = strategy.calculateFee(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("0.00"), fee);
        assertEquals(
                "Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.",
                strategy.confirmationMessage());
    }

    @Test
    @DisplayName("Para VISA con monto igual o mayor al umbral, debe aplicarse la comisión bancaria")
    void getStrategy_Visa_AmountAtOrAboveThreshold_FeeIsApplied() {
        PaymentStrategy strategy = factory.getStrategy("VISA");

        BigDecimal fee = strategy.calculateFee(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("7.00"), fee);
        assertEquals(
                "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }

    @Test
    @DisplayName("Para CASH, la comisión debe ser siempre cero sin importar el monto")
    void getStrategy_Cash_FeeIsAlwaysZero() {
        PaymentStrategy strategy = factory.getStrategy("CASH");

        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("50.00")));
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("500.00")));
    }

    @Test
    @DisplayName("Un método de pago desconocido debe lanzar UnknownPaymentMethodException")
    void getStrategy_UnknownMethod_ThrowsUnknownPaymentMethodException() {
        assertThrows(UnknownPaymentMethodException.class, () -> factory.getStrategy("CRYPTO"));
    }
}