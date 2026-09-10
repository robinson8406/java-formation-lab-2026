package com.indra.retail;

import java.math.BigDecimal;

public class Loyalty implements DiscountCalculator {

    @Override
    public Money apply(Money price) {
        return Money.of(price.amount().multiply(BigDecimal.valueOf(0.85)));
    }

}
