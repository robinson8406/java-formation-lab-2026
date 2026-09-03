package com.indra.retail.strategy;

import com.indra.retail.DiscountType;
import java.math.BigDecimal;

/**
 * Estrategia de descuento por fidelidad (15% de descuento para clientes > 12 meses).
 */
public class LoyaltyDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal MULTIPLIER = BigDecimal.valueOf(0.85);

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.LOYALTY;
    }

    @Override
    public BigDecimal apply(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("El precio no puede ser nulo");
        }
        return price.multiply(MULTIPLIER);
    }
}
