package com.indra.retail;

import java.math.BigDecimal;

public sealed interface DiscountStrategy
        permits DiscountStrategy.StandardDiscount,
                DiscountStrategy.SeasonalDiscount,
                DiscountStrategy.LoyaltyDiscount {

    BigDecimal apply(BigDecimal price);

    interface Factory {
        DiscountStrategy create(DiscountType type);

        static Factory instance() {
            return new DiscountStrategyFactory();
        }
    }

    final class StandardDiscount implements DiscountStrategy {
        @Override
        public BigDecimal apply(BigDecimal price) {
            return price.multiply(BigDecimal.valueOf(0.95));
        }
    }

    final class SeasonalDiscount implements DiscountStrategy {
        @Override
        public BigDecimal apply(BigDecimal price) {
            return price.multiply(BigDecimal.valueOf(0.80));
        }
    }

    final class LoyaltyDiscount implements DiscountStrategy {
        @Override
        public BigDecimal apply(BigDecimal price) {
            return price.multiply(BigDecimal.valueOf(0.85));
        }
    }

    static class DiscountStrategyFactory implements Factory {
        @Override
        public DiscountStrategy create(DiscountType type) {
            return switch (type) {
                case STANDARD -> new StandardDiscount();
                case SEASONAL -> new SeasonalDiscount();
                case LOYALTY -> new LoyaltyDiscount();
            };
        }
    }
}
