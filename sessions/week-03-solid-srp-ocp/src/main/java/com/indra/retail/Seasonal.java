package com.indra.retail;

import java.math.BigDecimal;

public final class Seasonal implements DiscountCalculator {

    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.80));
    }

}
