package com.indra.retail.orders.service;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderItem;
import com.indra.retail.orders.web.dto.CreateOrderRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public Order create(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(item.sku(), item.quantity(), item.unitPrice()))
                .toList();
        Order order = new Order(request.customerId(), items, request.deliveryAddress());
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
