package com.indra.retail;

import java.math.BigDecimal;

public class DiscountCalculator {

    private  FactoryDiscount factoryDiscount;

    public DiscountCalculator() {
        this.factoryDiscount = new FactoryDiscount();
    }

    public BigDecimal apply(BigDecimal price, DiscountType type) {
        return factoryDiscount.create(type).calculate(price);
    }

}
