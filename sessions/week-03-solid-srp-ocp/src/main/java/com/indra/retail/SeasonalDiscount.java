package com.indra.retail;

import java.math.BigDecimal;

public class SeasonalDiscount implements DiscountCalculator {

    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.80));
    }

}
