package com.indra.retail.orders.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.retail.orders.dto.CreateOrderRequest;
import com.indra.retail.orders.dto.OrderResponse;
import com.indra.retail.orders.model.OrderItem;
import com.indra.retail.orders.model.OrderStatus;
import com.indra.retail.orders.service.OrderNotFoundException;
import com.indra.retail.orders.service.OrderService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/orders debe retornar 201 Created y OrderResponse cuando la solicitud es válida")
    void createOrder_validRequest_returns201Created() throws Exception {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 2, 50.0));
        CreateOrderRequest request = new CreateOrderRequest("CUST-001", items, "Calle 100 # 15-20, Bogota");
        OrderResponse expectedResponse = new OrderResponse(
            "order-123",
            OrderStatus.CREATED,
            100.0,
            LocalDate.now().plusDays(5)
        );

        when(orderService.create(any(CreateOrderRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId", is("order-123")))
            .andExpect(jsonPath("$.status", is("CREATED")))
            .andExpect(jsonPath("$.totalAmount", is(100.0)))
            .andExpect(jsonPath("$.estimatedDelivery", is(expectedResponse.estimatedDelivery().toString())));

        verify(orderService).create(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("POST /api/orders debe retornar 400 Bad Request cuando deliveryAddress tiene menos de 10 caracteres")
    void createOrder_shortDeliveryAddress_returns400BadRequest() throws Exception {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 1, 20.0));
        CreateOrderRequest request = new CreateOrderRequest("CUST-001", items, "Corta");

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.errors", hasItem(containsString("deliveryAddress"))));
    }

    @Test
    @DisplayName("POST /api/orders debe retornar 400 Bad Request cuando customerId es vacío")
    void createOrder_emptyCustomerId_returns400BadRequest() throws Exception {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 1, 20.0));
        CreateOrderRequest request = new CreateOrderRequest("", items, "Calle 100 # 15-20, Bogota");

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.errors", hasItem(containsString("customerId"))));
    }

    @Test
    @DisplayName("POST /api/orders debe retornar 400 Bad Request cuando items está vacío")
    void createOrder_emptyItems_returns400BadRequest() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("CUST-001", Collections.emptyList(), "Calle 100 # 15-20, Bogota");

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.errors", hasItem(containsString("items"))));
    }

    @Test
    @DisplayName("GET /api/orders/{orderId} debe retornar 200 OK con OrderResponse cuando el pedido existe")
    void getById_existingOrder_returns200Ok() throws Exception {
        OrderResponse expectedResponse = new OrderResponse(
            "order-456",
            OrderStatus.CONFIRMED,
            250.0,
            LocalDate.now().plusDays(3)
        );

        when(orderService.findById("order-456")).thenReturn(expectedResponse);

        mockMvc.perform(get("/api/orders/order-456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId", is("order-456")))
            .andExpect(jsonPath("$.status", is("CONFIRMED")))
            .andExpect(jsonPath("$.totalAmount", is(250.0)));

        verify(orderService).findById("order-456");
    }

    @Test
    @DisplayName("GET /api/orders/{orderId} debe retornar 404 Not Found cuando el pedido no existe")
    void getById_nonExistingOrder_returns404NotFound() throws Exception {
        when(orderService.findById("unknown-id")).thenThrow(new OrderNotFoundException("unknown-id"));

        mockMvc.perform(get("/api/orders/unknown-id"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.errors", hasItem("Pedido no encontrado: unknown-id")));

        verify(orderService).findById("unknown-id");
    }

    @Test
    @DisplayName("POST /api/orders debe retornar 500 Internal Server Error sin stack trace ante excepción no controlada")
    void createOrder_unexpectedException_returns500WithoutStackTrace() throws Exception {
        List<OrderItem> items = List.of(new OrderItem("SKU-100", 1, 20.0));
        CreateOrderRequest request = new CreateOrderRequest("CUST-001", items, "Calle 100 # 15-20, Bogota");

        when(orderService.create(any(CreateOrderRequest.class))).thenThrow(new RuntimeException("Database failure"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status", is(500)))
            .andExpect(jsonPath("$.errors", hasItem("Error interno del servidor")))
            .andExpect(jsonPath("$.stackTrace").doesNotExist())
            .andExpect(jsonPath("$.trace").doesNotExist());

        verify(orderService).create(any(CreateOrderRequest.class));
    }
}
