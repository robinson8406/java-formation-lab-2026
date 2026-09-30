package com.indra.retail.orders.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "{order.customerId.notBlank}")
        String customerId,
        @NotEmpty(message = "{order.items.notEmpty}")
        @Size(min = 1, message = "{order.items.size}")
        List<@NotNull(message = "{order.item.notNull}") @Valid CreateOrderItemRequest> items,
        @NotNull(message = "{order.deliveryAddress.notNull}")
        @NotBlank(message = "{order.deliveryAddress.notBlank}")
        @Size(min = 10, message = "{order.deliveryAddress.size}")
        String deliveryAddress) {
}
