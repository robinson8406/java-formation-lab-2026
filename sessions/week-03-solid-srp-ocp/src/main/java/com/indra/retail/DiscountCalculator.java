package com.indra.retail;

import java.math.BigDecimal;

public class DiscountCalculator {
    public BigDecimal calculateDiscount(Order order) {
        BigDecimal finalPrice = switch (order.getDiscountType()) {
            case STANDARD -> order.getPrice().multiply(BigDecimal.valueOf(0.95));
            case SEASONAL -> order.getPrice().multiply(BigDecimal.valueOf(0.80));
            case LOYALTY -> order.getPrice().multiply(BigDecimal.valueOf(0.85));
            default -> order.getPrice();
        };

        return finalPrice;
    }
}
