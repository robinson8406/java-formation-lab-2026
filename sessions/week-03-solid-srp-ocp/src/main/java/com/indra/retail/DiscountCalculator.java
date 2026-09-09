package com.indra.retail;

public sealed interface DiscountCalculator permits Standard, Seasonal, Loyalty {

    Money apply(Money price);

}
