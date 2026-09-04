package com.indra.retail;

import java.math.BigDecimal;

public class LoyaltyDiscount implements Discounts{
    @Override
    public BigDecimal calculate(BigDecimal price) {
        return price;
    }
}
