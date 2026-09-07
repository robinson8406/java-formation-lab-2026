package com.indra.retail.domain.discount;

import com.indra.retail.domain.model.Order;

import java.math.BigDecimal;

public final class SeasonalDiscount implements DiscountStrategy {
    @Override
    public BigDecimal calculate(Order order) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.80));
    }
}
