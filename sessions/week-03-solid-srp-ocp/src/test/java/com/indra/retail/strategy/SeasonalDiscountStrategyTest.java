package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeasonalDiscountStrategyTest {

    private final SeasonalDiscountStrategy strategy = new SeasonalDiscountStrategy();

    @Test
    @DisplayName("Debe asociarse al tipo de descuento SEASONAL")
    void shouldReturnSeasonalDiscountType() {
        assertEquals(DiscountType.SEASONAL, strategy.getDiscountType());
    }

    @Test
    @DisplayName("Debe aplicar 20% de descuento sobre el precio base")
    void shouldApplyTwentyPercentDiscount() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal result = strategy.apply(price);

        assertEquals(0, new BigDecimal("80.00").compareTo(result));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el precio es nulo")
    void shouldThrowWhenPriceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> strategy.apply(null));
    }
}
