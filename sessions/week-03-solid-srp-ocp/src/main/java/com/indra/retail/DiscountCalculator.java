package com.indra.retail;

import java.math.BigDecimal;

public class DiscountCalculator {

    public BigDecimal apply(BigDecimal price, DiscountType type) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }

        return switch (type) {
            case STANDARD -> price.multiply(BigDecimal.valueOf(0.95));
            case SEASONAL -> price.multiply(BigDecimal.valueOf(0.80));
            case LOYALTY -> price.multiply(BigDecimal.valueOf(0.85));
        };
    }
}
