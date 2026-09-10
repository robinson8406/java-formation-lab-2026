package com.indra.retail;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoyaltyDiscountStrategyTest {

    @Test
    void shouldNotDiscountCustomersWithTwelveMonthsOrLess() {
        BigDecimal result = new LoyaltyDiscountStrategy().apply(new BigDecimal("100"), 12);

        assertEquals(0, new BigDecimal("100").compareTo(result));
    }
}