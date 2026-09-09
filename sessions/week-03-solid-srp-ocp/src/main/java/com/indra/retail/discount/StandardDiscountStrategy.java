package com.indra.retail.discount;

import java.math.BigDecimal;

public class StandardDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal RATE = BigDecimal.valueOf(0.95);

    @Override
    public BigDecimal apply(BigDecimal price, int monthsAsCustomer) {
        return price.multiply(RATE);
    }
}