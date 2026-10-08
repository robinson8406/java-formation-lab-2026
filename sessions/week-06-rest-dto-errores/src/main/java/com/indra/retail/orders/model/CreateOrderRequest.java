package com.indra.retail.orders.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;

public record CreateOrderRequest(@NotEmpty
        String customerId, @NotEmpty
        List<OrderItem> items, @NotEmpty
        @Min(10)
        String deliveryAddress) {

}
