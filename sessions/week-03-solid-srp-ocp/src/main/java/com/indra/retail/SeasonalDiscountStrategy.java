package com.indra.retail;

import java.math.BigDecimal;

public class SeasonalDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal PRICE_FACTOR = new BigDecimal("0.80");

    @Override
    public DiscountType supports() {
        return DiscountType.SEASONAL;
    }

    @Override
    public BigDecimal apply(BigDecimal price, int customerMonths) {
        return price.multiply(PRICE_FACTOR);
    }
}