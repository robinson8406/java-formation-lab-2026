package com.indra.retail.orders.web;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderItem;
import com.indra.retail.orders.web.dto.CreateOrderRequest;
import com.indra.retail.orders.web.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toOrder(CreateOrderRequest request) {
        var items = request.items().stream()
                .map(item -> new OrderItem(item.sku(), item.quantity(), item.unitPrice()))
                .toList();

        return new Order(request.customerId(), items, request.deliveryAddress());
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getEstimatedDelivery());
    }
}
