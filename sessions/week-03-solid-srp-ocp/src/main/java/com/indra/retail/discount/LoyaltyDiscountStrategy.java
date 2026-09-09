package com.indra.retail.discount;

import java.math.BigDecimal;

public class LoyaltyDiscountStrategy implements DiscountStrategy {

    private static final int MIN_MONTHS_REQUIRED = 12;
    private static final BigDecimal RATE = BigDecimal.valueOf(0.85);

    @Override
    public BigDecimal apply(BigDecimal price, int monthsAsCustomer) {
        return monthsAsCustomer > MIN_MONTHS_REQUIRED
                ? price.multiply(RATE)
                : price;
    }
}