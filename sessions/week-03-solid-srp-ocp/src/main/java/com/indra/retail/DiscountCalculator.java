package com.indra.retail;

import java.math.BigDecimal;

public class DiscountCalculator {

    private  FactoryDiscount factoryDiscount;

    public DiscountCalculator() {
        this.factoryDiscount = new FactoryDiscount();
    }

    public BigDecimal apply(Order order) {
        return factoryDiscount.create(order.getDiscountType()).calculate(order);
    }

}
