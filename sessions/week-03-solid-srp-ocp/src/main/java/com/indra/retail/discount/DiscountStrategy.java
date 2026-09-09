package com.indra.retail.discount;

import java.math.BigDecimal;

@FunctionalInterface
public interface DiscountStrategy {
    BigDecimal apply(BigDecimal price, int monthsAsCustomer);
}