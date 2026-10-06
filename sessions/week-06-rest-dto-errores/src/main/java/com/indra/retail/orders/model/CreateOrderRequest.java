    package com.indra.retail.orders.model;

    import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

    public record CreateOrderRequest(
        @NotNull String customerId,
        @NotEmpty List<OrderItem> items,
        @NotNull String deliveryAddress) {
    }
