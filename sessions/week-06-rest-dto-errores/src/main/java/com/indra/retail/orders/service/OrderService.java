package com.indra.retail.orders.service;

import com.indra.retail.orders.model.CreateOrderRequest;
import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderResponse create(CreateOrderRequest createOrderRequest) {
        Order order = new Order(createOrderRequest.customerId(), createOrderRequest.items(),
                createOrderRequest.deliveryAddress());
        orders.put(order.getId(), order);
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getEstimatedDelivery());
    }

    public OrderResponse findById(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getEstimatedDelivery());
    }
}
