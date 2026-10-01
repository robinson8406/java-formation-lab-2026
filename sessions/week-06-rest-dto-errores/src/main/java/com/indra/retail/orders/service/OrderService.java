package com.indra.retail.orders.service;

import com.indra.retail.orders.model.CreateOrderRequest;
import com.indra.retail.orders.model.Order;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

     private final Map<String, CreateOrderRequest> createOrderRequests = new ConcurrentHashMap<>();

    public Order create(Order order) {
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

    public CreateOrderRequest create(CreateOrderRequest createOrderRequest) {
        createOrderRequests.put(createOrderRequest.customerId(), createOrderRequest);
        return createOrderRequest;
    }
}
