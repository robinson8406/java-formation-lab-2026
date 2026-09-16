package com.indra.logistics.base;

import java.math.BigDecimal;

import com.indra.logistics.base.factory.PaymentStrategyFactory;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.util.PaymentMethod;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {


    private final PaymentStrategyFactory strategyFactory;

    public PaymentService(PaymentStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public PaymentResult process(PaymentRequest request) {
        BigDecimal amount = request.amount();
        String method = request.method();
        PaymentStrategy strategy = strategyFactory.getStrategy(PaymentMethod.get(method));
        BigDecimal fee = strategy.calculateFee(amount);
        String message = strategy.confirmationMessage(amount);
        BigDecimal total = amount.add(fee);
        return new PaymentResult(method, amount, fee, total, message);
    }

}
