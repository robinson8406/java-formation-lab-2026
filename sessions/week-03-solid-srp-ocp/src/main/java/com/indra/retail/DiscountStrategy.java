package com.indra.retail;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal apply(BigDecimal price);
}
