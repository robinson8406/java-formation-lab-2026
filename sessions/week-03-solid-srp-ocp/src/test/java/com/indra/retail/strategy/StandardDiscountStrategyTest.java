package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardDiscountStrategyTest {

    private final StandardDiscountStrategy strategy = new StandardDiscountStrategy();

    @Test
    @DisplayName("Debe asociarse al tipo de descuento STANDARD")
    void shouldReturnStandardDiscountType() {
        assertEquals(DiscountType.STANDARD, strategy.getDiscountType());
    }

    @Test
    @DisplayName("Debe aplicar 5% de descuento sobre el precio base")
    void shouldApplyFivePercentDiscount() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);

        assertEquals(0, new BigDecimal("95.00").compareTo(result));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es nulo")
    void shouldThrowWhenPriceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> strategy.apply(null));
    }
}
