package com.indra.retail.domain.model;

import com.indra.retail.domain.discount.DiscountStrategy;

import java.math.BigDecimal;

public class Order {

    private final String id;
    private final BigDecimal price;
    private final DiscountStrategy discountStrategy;
    private final int requestedQuantity;
    private final Costumer customer;

    public Order(String id, BigDecimal price, DiscountStrategy discountStrategy, int requestedQuantity, Costumer customer) {
        this.id = id;
        this.price = price;
        this.discountStrategy = discountStrategy;
        this.requestedQuantity = requestedQuantity;
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public Costumer getCustomer() {
        return customer;
    }
}
