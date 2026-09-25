package com.indra.retail.orders.dto;

import com.indra.retail.orders.model.OrderItem;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "{order.customerId.notNull}")
    @NotEmpty(message = "{order.customerId.notEmpty}")
    String customerId,

    @NotNull(message = "{order.items.notNull}")
    @NotEmpty(message = "{order.items.notEmpty}")
    List<OrderItem> items,

    @NotNull(message = "{order.deliveryAddress.notNull}")
    @Size(min = 10, message = "{order.deliveryAddress.size}")
    String deliveryAddress
) {
}
