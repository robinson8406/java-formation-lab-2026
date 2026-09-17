package com.indra.logistics.base;

import java.math.BigDecimal;

import com.indra.logistics.base.factory.PaymentStrategyFactory;
import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;
import com.indra.logistics.base.strategy.PaymentStrategy;

public class PaymentService {

    private final PaymentStrategyFactory paymentStrategyFactory;

    public PaymentService()
    {
        this(new PaymentStrategyFactoryImpl());
    }

    public PaymentService(PaymentStrategyFactory paymentStrategyFactory)
    {
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    public PaymentResult process(PaymentRequest request)
    {
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(request.method());

        BigDecimal amount = request.amount();
        BigDecimal fee = strategy.calculateFee(amount);
        BigDecimal total = amount.add(fee);

        return new PaymentResult(
               request.method(),
               amount,
               fee,
               total,
               strategy.confirmationMessage()
        );
    }
}
