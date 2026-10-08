package com.indra.retail.orders.model;

public record OrderResponse(String orderId, String status, double totalAmount, String estimatedDelivery) {

}
