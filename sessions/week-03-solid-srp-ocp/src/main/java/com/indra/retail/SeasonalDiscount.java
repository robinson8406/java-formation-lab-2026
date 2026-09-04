package com.indra.retail;

import java.math.BigDecimal;

public class SeasonalDiscount implements Discounts{
    @Override
    public BigDecimal calculate(Order order) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.80));
    }
}
