package com.indra.retail.discount;

import java.math.BigDecimal;

public class SeasonalDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal RATE = BigDecimal.valueOf(0.80);

    @Override
    public BigDecimal apply(BigDecimal price, int monthsAsCustomer) {
        return price.multiply(RATE);
    }
}