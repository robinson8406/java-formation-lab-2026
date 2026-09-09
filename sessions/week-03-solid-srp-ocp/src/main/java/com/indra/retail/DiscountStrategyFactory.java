package com.indra.retail;

public class DiscountStrategyFactory {
    public DiscountStrategy getStrategy(DiscountType type) {
        return switch (type) {
            case STANDARD -> new StandardDiscountStrategy();
            case SEASONAL -> new SeasonalDiscountStrategy();
            case LOYALTY -> new LoyaltyDiscountStrategy();
        };
    }
}
