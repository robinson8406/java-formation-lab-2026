package com.indra.retail;

import java.math.BigDecimal;

public interface DiscountStrategy {

    DiscountType supportedType();

    BigDecimal apply(BigDecimal price, int customerMonths);
}
