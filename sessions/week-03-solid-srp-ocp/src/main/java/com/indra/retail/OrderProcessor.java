package com.indra.retail;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
    }

    public BigDecimal process(Order order, int availableStock) {
        validateStock(order, availableStock);
        var finalPrice = applyDiscount(order);

        orderNotifier.notifyCustomer(order.customerEmail(),
                "Tu pedido " + order.id() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }

    private void validateStock(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.requestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.id());
        }
    }

    private static BigDecimal applyDiscount(Order order) {
        if (null == order.discountType()) {
            return order.price();
        }

        DiscountCalculator discountCalculator = switch (order.discountType()) {
            case STANDARD -> new StandardDiscount();
            case SEASONAL -> new SeasonalDiscount();
            case LOYALTY -> new LoyaltyDiscount();
        };

        return discountCalculator.apply(order.price());
    }

}
