package com.indra.retail.orders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrderReturnsCreatedWithPublicResponse() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "customerId": "customer-1",
                            "items": [{"sku": "SKU-1", "quantity": 2, "unitPrice": 10.5}],
                            "deliveryAddress": "Calle 10 # 20-30"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(21.0))
                .andExpect(jsonPath("$.estimatedDelivery").isNotEmpty())
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andExpect(jsonPath("$.internalWarehouseCode").doesNotExist());
    }

    @Test
    void invalidOrderReturnsConsistentValidationError() throws Exception {
        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "",
                                    "items": [],
                                    "deliveryAddress": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    @Test
    void missingOrderReturnsNotFoundWithoutStackTrace() throws Exception {
        mockMvc.perform(get("/api/orders/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0]").value("Pedido no encontrado: missing"))
                .andExpect(jsonPath("$.path").value("/api/orders/missing"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void malformedRequestReturnsStandardInternalError() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed-json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value("Error interno del servidor"))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
