package com.indra.logistics.base;

import java.math.BigDecimal;


import com.indra.logistics.base.factory.PaymentStrategyFactory;
import com.indra.logistics.base.strategy.PaymentStrategy;

/**
 * BASE: toda la lógica de comisión vive en un if-else que crece con cada método de pago nuevo.
 * Agregar un método de pago implica editar esta clase y arriesgar los demás casos.
 */
public class PaymentService {

    private final PaymentStrategyFactory factory;

    public PaymentService(PaymentStrategyFactory factory) {
        this.factory = factory;
    }

    public PaymentResult process(PaymentRequest request) {
        PaymentStrategy strategy = factory.getStrategy(request.method());
        BigDecimal amount = request.amount();
        BigDecimal fee = strategy.calculateFee(amount);
        BigDecimal total = amount.add(fee);
        return new PaymentResult(strategy.methodCode(), amount, fee, total, strategy.confirmationMessage(amount));
    }
}
