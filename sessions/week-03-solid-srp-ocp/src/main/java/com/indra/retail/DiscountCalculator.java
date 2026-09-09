package com.indra.retail;

import java.math.BigDecimal;

public class DiscountCalculator {
    public BigDecimal apply(BigDecimal price, DiscountType type) {
        BigDecimal finalPrice;
        if (type == DiscountType.STANDARD) {
            finalPrice = price.multiply(BigDecimal.valueOf(0.95));
        } else if (type == DiscountType.SEASONAL) {
            finalPrice = price.multiply(BigDecimal.valueOf(0.80));
        } else if (type == DiscountType.LOYALTY) {
            finalPrice = price.multiply(BigDecimal.valueOf(0.85));
        } else {
            finalPrice = price;
        }
        return finalPrice;
    }    
}
