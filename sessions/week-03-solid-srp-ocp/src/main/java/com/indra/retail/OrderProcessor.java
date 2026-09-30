package com.indra.retail;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;
    private final DiscountStrategy.Factory discountStrategyFactory;

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier, DiscountStrategy.Factory discountStrategyFactory) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
        this.discountStrategyFactory = discountStrategyFactory;
    }

    public BigDecimal process(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        DiscountStrategy strategy = discountStrategyFactory.create(order.getDiscountType());
        BigDecimal finalPrice = strategy.apply(order.getPrice());

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}
