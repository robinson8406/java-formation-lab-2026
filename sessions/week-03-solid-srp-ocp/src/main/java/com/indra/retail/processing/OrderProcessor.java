package com.indra.retail.processing;

import com.indra.retail.discount.DiscountCalculator;
import com.indra.retail.domain.Order;
import com.indra.retail.notification.OrderNotifier;
import com.indra.retail.stock.StockValidator;

import java.math.BigDecimal;

public class OrderProcessor {

    private final StockValidator stockValidator;
    private final OrderNotifier orderNotifier;
    private final DiscountCalculator discountCalculator;

    public OrderProcessor(StockValidator stockValidator, OrderNotifier orderNotifier, DiscountCalculator discountCalculator) {
        this.stockValidator = stockValidator;
        this.orderNotifier = orderNotifier;
        this.discountCalculator = discountCalculator;
    }

    public BigDecimal process(Order order, int availableStock) {
        if (!stockValidator.hasEnoughStock(availableStock, order.getRequestedQuantity())) {
            throw new IllegalStateException("Stock insuficiente para el pedido " + order.getId());
        }

        BigDecimal finalPrice = discountCalculator.apply(
                order.getPrice(), order.getDiscountType(), order.getMonthsAsCustomer());

        orderNotifier.notifyCustomer(order.getCustomerEmail(),
                "Tu pedido " + order.getId() + " fue procesado. Total: " + finalPrice);

        return finalPrice;
    }
}