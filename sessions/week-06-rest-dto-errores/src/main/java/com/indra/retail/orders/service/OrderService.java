package com.indra.retail.orders.service;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderItem;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public Order create(String customerId, List<OrderItem> items, String deliveryAddress) {
        Order order = new Order(customerId, items, deliveryAddress);
        orders.put(order.getId(), order);
        return order;
    }

    public Order findById(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }
}
