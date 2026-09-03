package com.indra.retail;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonalDiscountStrategyTest {

    @Test
    void shouldApplyTwentyPercentDiscount() {
        BigDecimal result = new SeasonalDiscountStrategy().apply(new BigDecimal("100"), 0);

        assertEquals(0, new BigDecimal("80").compareTo(result));
    }
}