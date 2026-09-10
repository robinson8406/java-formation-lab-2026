package com.indra.retail;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;
    private final DiscountCalculator discountCalculator;


    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier) {
        this(stockValidator, orderNotifier, new DiscountCalculator());
    }

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier, DiscountCalculator discountCalculator) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
        this.discountCalculator = discountCalculator;
    }

    public BigDecimal process(Order order, int availableStock) {

        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        BigDecimal subtotal = order.getPrice().multiply(BigDecimal.valueOf(order.getRequestedQuantity()));
        BigDecimal finalPrice = discountCalculator.calculateDiscount(subtotal, order.getDiscountType());

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}
