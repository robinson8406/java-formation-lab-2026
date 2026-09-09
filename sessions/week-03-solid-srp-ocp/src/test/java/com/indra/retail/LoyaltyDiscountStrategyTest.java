package com.indra.retail;

import com.indra.retail.discount.LoyaltyDiscountStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LoyaltyDiscountStrategy")
class LoyaltyDiscountStrategyTest {

    private final LoyaltyDiscountStrategy strategy = new LoyaltyDiscountStrategy();

    @Test
    @DisplayName("Aplica el 15% de descuento cuando la antigüedad supera los 12 meses")
    void appliesFifteenPercentAfterTwelveMonths() {
        BigDecimal result = strategy.apply(BigDecimal.valueOf(200), 24);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(170.00)));
    }

    @Test
    @DisplayName("No aplica descuento cuando la antigüedad no es suficiente")
    void noDiscountIfNotEnoughSeniority() {
        BigDecimal result = strategy.apply(BigDecimal.valueOf(200), 6);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(200)));
    }
}