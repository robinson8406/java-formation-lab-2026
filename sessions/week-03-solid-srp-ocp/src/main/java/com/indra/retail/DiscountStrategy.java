package com.indra.retail;

import java.math.BigDecimal;

public interface DiscountStrategy {

    DiscountType supports();

    BigDecimal apply(BigDecimal price, int customerMonths);
}