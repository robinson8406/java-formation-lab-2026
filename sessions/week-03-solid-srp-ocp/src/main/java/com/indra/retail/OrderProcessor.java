package com.indra.retail;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;
    private final DiscountCalculator discountCalculator;

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
        this.discountCalculator = new DiscountCalculator();
    }

    public BigDecimal process(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        // Punto de partida del reto: descuento mezclado aquí, viola SRP y OCP.
        BigDecimal finalPrice = discountCalculator.apply(order);

        orderNotifier.notifyCustomer(order.getCustomer().getEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}
