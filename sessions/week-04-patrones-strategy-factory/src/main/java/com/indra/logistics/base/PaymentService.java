package com.indra.logistics.base;

import java.math.BigDecimal;

import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;
import com.indra.logistics.base.strategy.PaymentStrategy;

/**
 * BASE: toda la lógica de comisión vive en un if-else que crece con cada método
 * de pago nuevo.
 * Agregar un método de pago implica editar esta clase y arriesgar los demás
 * casos.
 */
public class PaymentService {

    public PaymentResult process(PaymentRequest request) {
        BigDecimal amount = request.amount();
        String method = request.method();

        PaymentStrategyFactoryImpl factory = new PaymentStrategyFactoryImpl();
        PaymentStrategy strategy = factory.getStrategy(method);

        BigDecimal fee;
        String message;

        fee = strategy.calculateFee(amount);
        message = strategy.confirmationMessage();


        BigDecimal total = amount.add(fee);
        return new PaymentResult(method, amount, fee, total, message);
    }

}