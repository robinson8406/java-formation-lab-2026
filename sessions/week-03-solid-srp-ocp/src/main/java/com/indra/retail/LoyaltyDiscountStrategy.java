package com.indra.retail;

import java.math.BigDecimal;

public class LoyaltyDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.LOYALTY;
    }

    @Override
    public BigDecimal apply(BigDecimal price, int customerMonths) {
        return customerMonths > 12
                ? price.multiply(BigDecimal.valueOf(0.85))
                : price;
    }
}
