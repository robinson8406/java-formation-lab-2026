package com.indra.retail.orders.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "{validation.customerId.notBlank}")
        String customerId,
        @NotEmpty(message = "{validation.items.notEmpty}")
        @Size(min = 1, message = "{validation.items.size}")
        List<@NotNull(message = "{validation.item.notNull}") @Valid ItemRequest> items,
        @NotNull(message = "{validation.deliveryAddress.notNull}")
        @Size(min = 10, message = "{validation.deliveryAddress.size}")
        String deliveryAddress) {

    public record ItemRequest(
            @NotBlank(message = "{validation.item.sku.notBlank}") String sku,
            @Positive(message = "{validation.item.quantity.positive}") int quantity,
            @Positive(message = "{validation.item.unitPrice.positive}") double unitPrice) {
    }
}