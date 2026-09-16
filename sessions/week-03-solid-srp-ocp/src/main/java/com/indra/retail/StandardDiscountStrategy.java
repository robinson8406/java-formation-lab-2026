package com.indra.retail;

import java.math.BigDecimal;

public class StandardDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.STANDARD;
    }

    @Override
    public BigDecimal apply(BigDecimal price, int customerMonths) {
        return price.multiply(BigDecimal.valueOf(0.95));
    }
}
