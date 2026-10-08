package com.indra.retail;

import java.math.BigDecimal;

public class Order {

    private final String id;
    private final BigDecimal price;
    private final DiscountType discountType;
    private final int requestedQuantity;
    private final String customerEmail;

    public Order(String id, BigDecimal price, DiscountType discountType, int requestedQuantity, String customerEmail) {
        this.id = id;
        this.price = price;
        this.discountType = discountType;
        this.requestedQuantity = requestedQuantity;
        this.customerEmail = customerEmail;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}
