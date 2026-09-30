package com.indra.retail.orders.web.dto;

import com.indra.retail.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderResponse(
        String orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDate estimatedDelivery) {
}
