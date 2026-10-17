package com.indra.retail.orders.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Ítem del pedido tal como llega del cliente; desacoplado del modelo de dominio. */
public record OrderItemRequest(
        @NotBlank(message = "{order.item.sku.required}")
        String sku,
        @Positive(message = "{order.item.quantity.positive}")
        int quantity,
        @Positive(message = "{order.item.unit-price.positive}")
        double unitPrice) {
}
