package com.indra.retail;

import java.math.BigDecimal;

public class StandardDiscount implements Discounts{
    @Override
    public BigDecimal calculate(BigDecimal price) {
        return  price.multiply(BigDecimal.valueOf(0.95));
    }
}
