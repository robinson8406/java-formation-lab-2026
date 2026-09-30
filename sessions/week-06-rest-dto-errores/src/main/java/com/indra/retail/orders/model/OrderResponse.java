package com.indra.retail.orders.model;

import java.time.LocalDate;

public record OrderResponse(String orderId, int status, double totalAmount, LocalDate estimatedDelivery) {

}
