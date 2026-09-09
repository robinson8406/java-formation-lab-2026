package com.indra.retail;

import java.math.BigDecimal;

public interface DiscountCalculator {

    BigDecimal apply(BigDecimal price);

}
