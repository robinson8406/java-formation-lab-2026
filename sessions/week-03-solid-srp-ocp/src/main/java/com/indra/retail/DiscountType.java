package com.indra.retail;

import java.math.BigDecimal;

public sealed interface DiscountType
    permits StandardDiscount, SeasonalDiscount, LoyaltyDiscount {

    DiscountType STANDARD = new StandardDiscount();
    DiscountType SEASONAL = new SeasonalDiscount();
    DiscountType LOYALTY = new LoyaltyDiscount();

    BigDecimal applyDiscount(BigDecimal price);
}
