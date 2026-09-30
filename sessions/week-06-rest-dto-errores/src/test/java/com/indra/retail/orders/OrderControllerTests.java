package com.indra.retail.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.service.OrderNotFoundException;
import com.indra.retail.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void createReturnsCreatedResponseWithoutInternalFields() throws Exception {
        when(orderService.create(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/orders")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "customer-1",
                                  "items": [{"sku": "SKU-1", "quantity": 2, "unitPrice": 19.99}],
                                  "deliveryAddress": "Calle 123, Bogotá"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", startsWith("/api/orders/")))
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(39.98))
                .andExpect(jsonPath("$.estimatedDelivery").isNotEmpty())
                .andExpect(jsonPath("$.internalWarehouseCode").doesNotExist())
                .andExpect(jsonPath("$.createdByEmployeeId").doesNotExist());
    }

    @Test
    void invalidRequestReturnsBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": " ",
                                  "items": [],
                                  "deliveryAddress": "Calle"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").isNotEmpty())
                .andExpect(content().string(not(containsString("trace"))));
    }

    @Test
    void missingOrderReturnsNotFoundWithConsistentErrorShape() throws Exception {
        when(orderService.findById("missing"))
                .thenThrow(new OrderNotFoundException("missing"));

        mockMvc.perform(get("/api/orders/missing")
                        .header("Accept-Language", "es"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value("Order not found: missing"));
    }

    @Test
    void getOrderReturnsOnlyPublicResponseFields() throws Exception {
        when(orderService.findById("order-1"))
                .thenReturn(new Order("customer-1", java.util.List.of(), "Calle 123, Bogotá"));

        mockMvc.perform(get("/api/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.internalWarehouseCode").doesNotExist())
                .andExpect(jsonPath("$.createdByEmployeeId").doesNotExist());
    }

    @Test
    void unexpectedExceptionReturnsGenericMessageWithoutInternalDetails() throws Exception {
        when(orderService.create(any(Order.class)))
                .thenThrow(new IllegalStateException("database password leaked"));

        mockMvc.perform(post("/api/orders")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "customer-1",
                                  "items": [{"sku": "SKU-1", "quantity": 1, "unitPrice": 5.0}],
                                  "deliveryAddress": "Calle 123, Bogotá"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.errors[0]").value(
                        "An internal error occurred. Please try again later."))
                .andExpect(content().string(not(containsString("database password leaked"))));
    }

    @Test
    void blankOrderIdIsRejectedByBonusPathValidation() throws Exception {
        mockMvc.perform(get("/api/orders/{orderId}", " ")
                        .header("Accept-Language", "es"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").value(
                        containsString("orderId")));
    }

    @Test
    void validationMessagesRemainEnglishRegardlessOfRequestedLanguage() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "",
                                  "items": [],
                                  "deliveryAddress": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors",
                        hasItem(containsString("must not be blank"))));
    }
}
