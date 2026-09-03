package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import java.math.BigDecimal;

/**
 * Estrategia de descuento estándar (5% de descuento).
 */
public class StandardDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal MULTIPLIER = BigDecimal.valueOf(0.95);

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.STANDARD;
    }

    @Override
    public BigDecimal apply(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("El precio no puede ser nulo");
        }
        return price.multiply(MULTIPLIER);
    }
}
