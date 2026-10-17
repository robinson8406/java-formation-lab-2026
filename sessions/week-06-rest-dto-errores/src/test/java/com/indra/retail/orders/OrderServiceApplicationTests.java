package com.indra.retail.orders;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

    private static final String VALID_ORDER = """
            {
                "customerId": "customer-1",
                "items": [{"sku": "SKU-1", "quantity": 2, "unitPrice": 10.5}],
                "deliveryAddress": "Calle 10 # 20-30"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST válido → 201 con OrderResponse, header Location y sin campos internos")
    void createOrderReturnsCreatedWithPublicResponse() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/api/orders/")))
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(21.0))
                .andExpect(jsonPath("$.estimatedDelivery").isNotEmpty())
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andExpect(jsonPath("$.items").doesNotExist())
                .andExpect(jsonPath("$.deliveryAddress").doesNotExist())
                .andExpect(jsonPath("$.internalWarehouseCode").doesNotExist())
                .andExpect(jsonPath("$.createdByEmployeeId").doesNotExist());
    }

    @Test
    @DisplayName("GET de un pedido creado → 200 con el mismo OrderResponse")
    void createdOrderCanBeRetrievedById() throws Exception {
        String body = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(body, "$.orderId");

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.totalAmount").value(21.0));
    }

    @Test
    @DisplayName("POST inválido → 400 con un error 'campo: mensaje' por cada campo inválido")
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
                .andExpect(jsonPath("$.errors", containsInAnyOrder(
                        "customerId: es obligatorio y no puede estar vacio",
                        "items: debe contener al menos un elemento",
                        "deliveryAddress: debe tener al menos 10 caracteres")))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @DisplayName("POST sin campos → 400 con un único mensaje por campo obligatorio")
    void missingFieldsReturnOneErrorPerField() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(3)))
                .andExpect(jsonPath("$.errors", containsInAnyOrder(
                        "customerId: es obligatorio y no puede estar vacio",
                        "items: debe contener al menos un elemento",
                        "deliveryAddress: es obligatorio")));
    }

    @Test
    @DisplayName("POST con ítem inválido → 400 indicando la posición y el campo del ítem")
    void invalidItemReturnsFieldErrorWithIndex() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "customer-1",
                                    "items": [{"sku": "", "quantity": 0, "unitPrice": 10.5}],
                                    "deliveryAddress": "Calle 10 # 20-30"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", containsInAnyOrder(
                        "items[0].quantity: debe ser mayor a 0",
                        "items[0].sku: es obligatorio")));
    }

    @Test
    @DisplayName("GET de pedido inexistente → 404 con mensaje descriptivo y sin stack trace")
    void missingOrderReturnsNotFoundWithoutStackTrace() throws Exception {
        mockMvc.perform(get("/api/orders/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0]").value("Pedido no encontrado: missing"))
                .andExpect(jsonPath("$.path").value("/api/orders/missing"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @DisplayName("GET con id demasiado largo → 400 por @Validated en el path")
    void tooLongOrderIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/{orderId}", "x".repeat(40)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value("orderId: no puede superar 36 caracteres"));
    }

    @Test
    @DisplayName("JSON malformado → 400 (error del cliente), no 500")
    void malformedRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.errors[0]").value("El cuerpo de la peticion no es un JSON valido"))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @DisplayName("Método no soportado → 405 con el formato estándar, no 500")
    void unsupportedMethodKeepsItsHttpStatus() throws Exception {
        mockMvc.perform(delete("/api/orders"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }
}
