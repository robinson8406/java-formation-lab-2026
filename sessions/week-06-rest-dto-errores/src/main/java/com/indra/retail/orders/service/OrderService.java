package com.indra.retail.orders.service;

import com.indra.retail.orders.dto.CreateOrderRequest;
import com.indra.retail.orders.dto.OrderResponse;
import com.indra.retail.orders.model.Order;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderResponse create(CreateOrderRequest request) {
        Order order = new Order(request.customerId(), request.items(), request.deliveryAddress());
        orders.put(order.getId(), order);
        return OrderResponse.from(order);
    }

    public Order create(Order order) {
        orders.put(order.getId(), order);
        return order;
    }

    public OrderResponse findById(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return OrderResponse.from(order);
    }

    public Order findOrderById(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }
}
