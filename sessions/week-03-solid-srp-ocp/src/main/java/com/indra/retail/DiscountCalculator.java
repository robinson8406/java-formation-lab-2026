package com.indra.retail;
import java.math.BigDecimal;

public class DiscountCalculator {

    public BigDecimal calculateDiscount(
            BigDecimal price,
            DiscountType type) {

        return type.applyDiscount(price);
    }
}