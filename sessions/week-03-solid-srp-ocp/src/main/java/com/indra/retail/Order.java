package com.indra.retail;

import java.math.BigDecimal;

public record Order(
        String id,
        BigDecimal price,
        DiscountType discountType,
        int requestedQuantity,
        String customerEmail
) {}
