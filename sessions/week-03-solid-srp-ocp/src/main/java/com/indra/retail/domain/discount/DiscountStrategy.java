package com.indra.retail.domain.discount;

import com.indra.retail.domain.model.Order;

import java.math.BigDecimal;

public sealed interface DiscountStrategy
            permits StandardDiscount,
            SeasonalDiscount,
            LoyaltyDiscount
{
    BigDecimal calculate(Order order);
}