package com.indra.retail;

import java.math.BigDecimal;

public class DiscountCalculator {

    private final DiscountStrategyFactory factory;

    public DiscountCalculator(DiscountStrategyFactory factory) {
        this.factory = factory;
    }

    public BigDecimal apply(BigDecimal price, DiscountType type) {
        return factory.getStrategy(type).apply(price);        
    }
}
