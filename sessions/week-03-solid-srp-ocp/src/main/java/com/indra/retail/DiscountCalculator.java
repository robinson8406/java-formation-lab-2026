package com.indra.retail;

import java.math.BigDecimal;

public sealed interface DiscountCalculator permits Standard, Seasonal, Loyalty {

    BigDecimal apply(BigDecimal price);

}
