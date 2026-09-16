package com.indra.logistics.base;

import java.math.BigDecimal;

import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;
import com.indra.logistics.base.strategy.PaymentStrategy;

public class PaymentService {

    public PaymentResult process(PaymentRequest request) {
        BigDecimal amount = request.amount();
        String method = request.method();

        PaymentStrategyFactoryImpl factory = new PaymentStrategyFactoryImpl();
        PaymentStrategy strategy = factory.getStrategy(method);

        BigDecimal fee=strategy.calculateFee(amount);           
        String message = strategy.confirmationMessage();


        BigDecimal total = amount.add(fee);
        return new PaymentResult(method, amount, fee, total, message);
    }

}