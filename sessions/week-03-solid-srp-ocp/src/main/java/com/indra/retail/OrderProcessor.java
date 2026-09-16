package com.indra.retail;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final DiscountCalculator discountCalculator;
    private final OrderNotifier orderNotifier;

    public OrderProcessor(StockValidator stockValidator, DiscountCalculator discountCalculator,
                          OrderNotifier orderNotifier) {
        this.stockValidator = stockValidator;
        this.discountCalculator = discountCalculator;
        this.orderNotifier = orderNotifier;
    }

    public BigDecimal process(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        BigDecimal finalPrice = discountCalculator.apply(
                order.getPrice(), order.getDiscountType(), order.getCustomerMonths());

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}
