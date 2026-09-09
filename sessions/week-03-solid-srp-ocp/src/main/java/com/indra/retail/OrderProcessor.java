package com.indra.retail;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
    }

    public Money process(Order order, int availableStock) {
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

    private static Money applyDiscount(Order order) {
        var discountCalculator = order.discountType();

        if (null == discountCalculator) {
            return order.price();
        }

        return discountCalculator.apply(order.price());
    }

}
