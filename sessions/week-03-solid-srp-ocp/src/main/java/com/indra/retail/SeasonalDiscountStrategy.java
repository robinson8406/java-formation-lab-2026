package com.indra.retail;

import java.math.BigDecimal;

public class SeasonalDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.SEASONAL;
    }

    @Override
    public BigDecimal apply(BigDecimal price, int customerMonths) {
        return price.multiply(BigDecimal.valueOf(0.80));
    }
}
