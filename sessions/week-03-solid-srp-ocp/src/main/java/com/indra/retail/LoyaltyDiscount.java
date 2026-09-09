package com.indra.retail;

import java.math.BigDecimal;

public class LoyaltyDiscount implements DiscountCalculator {

    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.85));
    }

}
