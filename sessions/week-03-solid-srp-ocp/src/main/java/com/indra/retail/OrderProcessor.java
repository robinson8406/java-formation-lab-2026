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

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }

    private void validateStock(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }
    }

    private static BigDecimal applyDiscount(Order order) {
        if (null == order.getDiscountType()) {
            return order.getPrice();
        }

        DiscountCalculator discountCalculator = switch (order.getDiscountType()) {
            case STANDARD -> new StandardDiscount();
            case SEASONAL -> new SeasonalDiscount();
            case LOYALTY -> new LoyaltyDiscount();
        };

        return discountCalculator.apply(order.getPrice());
    }

}
