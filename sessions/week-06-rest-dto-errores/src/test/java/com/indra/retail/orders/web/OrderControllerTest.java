package com.indra.retail.orders.web;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderItem;
import com.indra.retail.orders.service.OrderNotFoundException;
import com.indra.retail.orders.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(ApiExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderController orderController;

    @MockBean
    private OrderService orderService;

    @Test
    void createReturns201AndOnlyPublicFields() throws Exception {
        Order order = new Order("customer-1", List.of(new OrderItem("SKU-1", 2, 12.5)), "Calle principal 123");
        order.setInternalWarehouseCode("internal-value");
        given(orderService.create(eq("customer-1"), anyList(), eq("Calle principal 123"))).willReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"customer-1","items":[{"sku":"SKU-1","quantity":2,"unitPrice":12.5}],"deliveryAddress":"Calle principal 123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.totalAmount").value(25.0))
                .andExpect(jsonPath("$.internalWarehouseCode").doesNotExist());
    }

    @Test
    void invalidRequestReturnsLocalized400ErrorContract() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"\",\"items\":[],\"deliveryAddress\":\"Corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.hasItem("customerId: no debe estar vacío")));
    }

    @Test
    void missingOrderReturns404WithStandardContract() throws Exception {
        given(orderService.findById("missing")).willThrow(new OrderNotFoundException("missing"));

        mockMvc.perform(get("/api/orders/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("missing")));
    }

    @Test
    void unexpectedFailureReturnsGeneric500WithoutStackTrace() throws Exception {
        given(orderService.findById("broken")).willThrow(new IllegalStateException("secret details"));

        mockMvc.perform(get("/api/orders/broken"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.not("secret details")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret details"))));
    }

    @Test
    void blankOrderIdIsRejectedByBonusParameterValidation() throws Exception {
        assertThrows(jakarta.validation.ConstraintViolationException.class,
            () -> orderController.getById(" "));
    }
}