package com.indra.retail.orders.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Contrato de entrada para crear un pedido. Cada campo tiene una sola regla por
 * caso de error, para que el cliente reciba un único mensaje por problema.
 */
public record CreateOrderRequest(
        @NotBlank(message = "{order.customer-id.required}")
        String customerId,

        @NotEmpty(message = "{order.items.required}")
        List<@Valid OrderItemRequest> items,

        @NotNull(message = "{order.delivery-address.required}")
        @Size(min = 10, message = "{order.delivery-address.min-size}")
        String deliveryAddress) {
}
