package com.indra.retail.orders.web.dto;

import com.indra.retail.orders.model.OrderItem;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "{order.customer-id.required}")
        @NotEmpty(message = "{order.customer-id.required}")
        String customerId,
        @NotNull(message = "{order.items.required}")
        @NotEmpty(message = "{order.items.required}")
        @Size(min = 1, message = "{order.items.min-size}")
        List<OrderItem> items,
        @NotNull(message = "{order.delivery-address.required}")
        @Size(min = 10, message = "{order.delivery-address.min-size}")
        String deliveryAddress) {
}