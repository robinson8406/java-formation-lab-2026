package com.indra.retail;

import java.math.BigDecimal;

public class StandardDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal PRICE_FACTOR = new BigDecimal("0.95");

    @Override
    public DiscountType supports() {
        return DiscountType.STANDARD;
    }

    @Override
    public BigDecimal apply(BigDecimal price, int customerMonths) {
        return price.multiply(PRICE_FACTOR);
    }
}