package com.indra.retail.orders.web;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void create_validRequest_returns201() throws Exception {
        Order order = new Order("cust123", Collections.emptyList(), "Calle Falsa 123");
        when(orderService.create(any(Order.class))).thenReturn(order);

        String validJson = """
                {
                    "customerId": "cust123",
                    "items": [
                        { "sku": "ITEM1", "quantity": 1, "unitPrice": 10.0 }
                    ],
                    "deliveryAddress": "Calle Falsa 123"
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isCreated());
    }

    @Test
    void create_invalidRequest_returns400() throws Exception {
        String invalidJson = """
                {
                    "customerId": "",
                    "items": [],
                    "deliveryAddress": "Corta"
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
