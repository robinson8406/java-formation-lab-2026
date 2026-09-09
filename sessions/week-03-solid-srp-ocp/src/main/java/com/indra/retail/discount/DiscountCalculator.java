package com.indra.retail.discount;

import com.indra.retail.domain.DiscountType;

import java.math.BigDecimal;
import java.util.Map;

public class DiscountCalculator {

    private final Map<DiscountType, DiscountStrategy> strategies;

    public DiscountCalculator(Map<DiscountType, DiscountStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    public BigDecimal apply(BigDecimal price, DiscountType type) {
        return apply(price, type, 0);
    }

    public BigDecimal apply(BigDecimal price, DiscountType type, int monthsAsCustomer) {
        DiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de descuento no soportado: " + type);
        }
        return strategy.apply(price, monthsAsCustomer);
    }
}