package com.indra.retail.config;

import com.indra.retail.discount.*;
import com.indra.retail.domain.DiscountType;

import java.util.EnumMap;
import java.util.Map;

public final class DiscountCalculatorFactory {

    private DiscountCalculatorFactory() {
    }

    public static DiscountCalculator createDefault() {
        Map<DiscountType, DiscountStrategy> strategies = new EnumMap<>(DiscountType.class);
        strategies.put(DiscountType.STANDARD, new StandardDiscountStrategy());
        strategies.put(DiscountType.SEASONAL, new SeasonalDiscountStrategy());
        strategies.put(DiscountType.LOYALTY, new LoyaltyDiscountStrategy());
        return new DiscountCalculator(strategies);
    }
}