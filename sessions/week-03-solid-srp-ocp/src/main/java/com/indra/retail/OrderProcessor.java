package com.indra.retail;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;
    private final DiscountCalculator discountCalculator;

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier,
            DiscountCalculator discountCalculator) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
        this.discountCalculator = discountCalculator;
    }

    public BigDecimal process(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        // Uso del DiscountCalculator para calcular el precio final.
        BigDecimal finalPrice = discountCalculator.calculateDiscount(order);

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}
