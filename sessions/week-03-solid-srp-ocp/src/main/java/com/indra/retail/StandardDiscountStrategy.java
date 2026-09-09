package com.indra.retail;

import java.math.BigDecimal;

public class StandardDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.95));
    }
    
}
