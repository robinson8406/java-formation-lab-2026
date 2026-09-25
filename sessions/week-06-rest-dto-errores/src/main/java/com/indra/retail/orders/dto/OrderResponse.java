package com.indra.retail.orders.dto;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderStatus;
import java.time.LocalDate;

public record OrderResponse(
    String orderId,
    OrderStatus status,
    double totalAmount,
    LocalDate estimatedDelivery
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getEstimatedDelivery()
        );
    }
}
