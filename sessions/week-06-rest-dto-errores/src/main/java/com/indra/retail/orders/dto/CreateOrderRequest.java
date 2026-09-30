package com.indra.retail.orders.dto;

import com.indra.retail.orders.model.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "{order.customer.notblank}")
    String customerId,

    @NotEmpty(message = "{order.items.notempty}")
    List<OrderItem> items,

    @NotBlank(message = "{order.address.notblank}")
    @Size(min = 10, message = "{order.address.size}")
    String deliveryAddress
) {}
