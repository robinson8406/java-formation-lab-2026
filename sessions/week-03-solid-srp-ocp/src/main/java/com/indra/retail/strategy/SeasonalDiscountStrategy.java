package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import java.math.BigDecimal;

/**
 * Estrategia de descuento de temporada (20% de descuento).
 */
public class SeasonalDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal MULTIPLIER = BigDecimal.valueOf(0.80);

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.SEASONAL;
    }

    @Override
    public BigDecimal apply(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("El precio no puede ser nulo");
        }
        return price.multiply(MULTIPLIER);
    }
}
