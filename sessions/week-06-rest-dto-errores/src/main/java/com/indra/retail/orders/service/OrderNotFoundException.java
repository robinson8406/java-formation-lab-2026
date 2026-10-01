package com.indra.retail.orders.service;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super(orderId);
    }
}
