package com.indra.retail;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountCalculatorTest {

    private final DiscountCalculator calculator = new DiscountCalculator(List.of(
            new StandardDiscountStrategy(),
            new SeasonalDiscountStrategy(),
            new LoyaltyDiscountStrategy()));

    @Test
    void shouldApplyLoyaltyDiscountOnlyAfterTwelveMonths() {
        assertEquals(0, new BigDecimal("85.00").compareTo(
                calculator.apply(new BigDecimal("100.00"), DiscountType.LOYALTY, 13)));
        assertEquals(0, new BigDecimal("100.00").compareTo(
                calculator.apply(new BigDecimal("100.00"), DiscountType.LOYALTY, 12)));
    }
}