package com.indra.retail;

import java.math.BigDecimal;

public class LoyaltyDiscountStrategy implements DiscountStrategy {

    private static final int MINIMUM_MONTHS = 12;
    private static final BigDecimal PRICE_FACTOR = new BigDecimal("0.85");

    @Override
    public DiscountType supports() {
        return DiscountType.LOYALTY;
    }

    @Override
    public BigDecimal apply(BigDecimal price, int customerMonths) {
        return customerMonths > MINIMUM_MONTHS ? price.multiply(PRICE_FACTOR) : price;
    }
}