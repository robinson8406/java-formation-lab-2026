package com.indra.retail;

import java.math.BigDecimal;

/**
 * Orquestador del procesamiento de pedidos en Indra Retail.
 * Responsabilidad única (SRP): coordinar el flujo de validación, cálculo y notificación,
 * delegando cada aspecto a su respectivo componente especializado.
 */
public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;
    private final DiscountCalculator discountCalculator;

    /**
     * Constructor principal con inyección de todas las dependencias requeridas.
     */
    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier, DiscountCalculator discountCalculator) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
        this.discountCalculator = discountCalculator;
    }

    /**
     * Constructor sobrecargado para retrocompatibilidad con clientes existentes.
     */
    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier) {
        this(stockValidator, orderNotifier, new DiscountCalculator());
    }

    public BigDecimal process(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        BigDecimal finalPrice = discountCalculator.apply(order.getPrice(), order.getDiscountType());

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}
