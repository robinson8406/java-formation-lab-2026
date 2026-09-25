package com.indra.retail.orders.service;

import com.indra.retail.orders.dto.CreateOrderRequest;
import com.indra.retail.orders.dto.OrderResponse;
import com.indra.retail.orders.model.OrderItem;
import com.indra.retail.orders.model.OrderStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }

    @Test
    @DisplayName("Debe crear un pedido a partir del DTO CreateOrderRequest y retornar OrderResponse")
    void create_validRequest_returnsOrderResponse() {
        List<OrderItem> items = List.of(
            new OrderItem("SKU-1", 2, 25.0),
            new OrderItem("SKU-2", 1, 50.0)
        );
        CreateOrderRequest request = new CreateOrderRequest("CUST-001", items, "Carrera 7 # 72-01, Bogota");

        OrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertNotNull(response.orderId());
        assertEquals(OrderStatus.CREATED, response.status());
        assertEquals(100.0, response.totalAmount());
        assertNotNull(response.estimatedDelivery());
    }

    @Test
    @DisplayName("Debe consultar un pedido existente por ID y retornar OrderResponse")
    void findById_existingOrder_returnsOrderResponse() {
        List<OrderItem> items = List.of(new OrderItem("SKU-1", 1, 30.0));
        CreateOrderRequest request = new CreateOrderRequest("CUST-002", items, "Calle 100 # 15-20, Bogota");
        OrderResponse created = orderService.create(request);

        OrderResponse found = orderService.findById(created.orderId());

        assertNotNull(found);
        assertEquals(created.orderId(), found.orderId());
        assertEquals(OrderStatus.CREATED, found.status());
        assertEquals(30.0, found.totalAmount());
    }

    @Test
    @DisplayName("Debe lanzar OrderNotFoundException cuando el ID no existe")
    void findById_nonExistingId_throwsOrderNotFoundException() {
        OrderNotFoundException exception = assertThrows(
            OrderNotFoundException.class,
            () -> orderService.findById("non-existent-id")
        );

        assertTrue(exception.getMessage().contains("non-existent-id"));
    }
}
