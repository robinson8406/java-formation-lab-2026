package com.indra.retail;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DiscountCalculator {

    private final Map<DiscountType, DiscountStrategy> strategies;

    public DiscountCalculator(List<DiscountStrategy> strategies) {
        this.strategies = new EnumMap<>(DiscountType.class);
        for (DiscountStrategy strategy : strategies) {
            DiscountStrategy previous = this.strategies.put(strategy.supports(), strategy);
            if (previous != null) {
                throw new IllegalArgumentException("Estrategia duplicada: " + strategy.supports());
            }
        }
    }

    public BigDecimal apply(BigDecimal price, DiscountType type) {
        return apply(price, type, 0);
    }

    public BigDecimal apply(BigDecimal price, DiscountType type, int customerMonths) {
        if (price == null || type == null) {
            throw new IllegalArgumentException("price y type son obligatorios");
        }
        DiscountStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No existe estrategia para: " + type);
        }
        return strategy.apply(price, customerMonths);
    }
}