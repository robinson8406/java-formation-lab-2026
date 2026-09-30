package com.indra.retail.orders.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotBlank(message = "{order.item.sku.notBlank}")
        String sku,
        @Positive(message = "{order.item.quantity.positive}")
        int quantity,
        @NotNull(message = "{order.item.unitPrice.notNull}")
        @PositiveOrZero(message = "{order.item.unitPrice.positiveOrZero}")
        BigDecimal unitPrice) {
}
