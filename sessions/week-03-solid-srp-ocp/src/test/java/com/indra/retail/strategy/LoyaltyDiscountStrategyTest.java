package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoyaltyDiscountStrategyTest {

    private final LoyaltyDiscountStrategy strategy = new LoyaltyDiscountStrategy();

    @Test
    @DisplayName("Debe asociarse al tipo de descuento LOYALTY")
    void shouldReturnLoyaltyDiscountType() {
        assertEquals(DiscountType.LOYALTY, strategy.getDiscountType());
    }

    @Test
    @DisplayName("Debe aplicar 15% de descuento sobre el precio base para clientes con más de 12 meses")
    void shouldApplyFifteenPercentDiscount() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);

        assertEquals(0, new BigDecimal("85.00").compareTo(result));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es nulo")
    void shouldThrowWhenPriceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> strategy.apply(null));
    }
}
