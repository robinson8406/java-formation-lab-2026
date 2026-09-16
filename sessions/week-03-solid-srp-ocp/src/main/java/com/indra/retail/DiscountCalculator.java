package com.indra.retail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DiscountCalculator {

    private final List<DiscountStrategy> strategies;

    public DiscountCalculator(List<DiscountStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public BigDecimal apply(BigDecimal price, DiscountType type) {
        return apply(price, type, 0);
    }

    public BigDecimal apply(BigDecimal price, DiscountType type, int customerMonths) {
        DiscountStrategy strategy = strategies.stream()
                .filter(candidate -> candidate.supportedType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe estrategia para " + type));

        return strategy.apply(Objects.requireNonNull(price, "price no puede ser null"), customerMonths);
    }
}
