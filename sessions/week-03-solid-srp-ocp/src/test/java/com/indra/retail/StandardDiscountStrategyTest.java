package com.indra.retail;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardDiscountStrategyTest {

    @Test
    void shouldApplyFivePercentDiscount() {
        BigDecimal result = new StandardDiscountStrategy().apply(new BigDecimal("100"), 0);

        assertEquals(0, new BigDecimal("95").compareTo(result));
    }
}