package com.indra.retail;

public record Order(
        String id,
        Money price,
        DiscountCalculator discountType,
        int requestedQuantity,
        String customerEmail
) {}
