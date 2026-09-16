package com.indra.logistics.base;

import com.indra.logistics.base.factory.PaymentStrategyFactory;
import com.indra.logistics.base.strategy.PaymentStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BASE: toda la lógica de comisión vive en un if-else que crece con cada método de pago nuevo.
 * Agregar un método de pago implica editar esta clase y arriesgar los demás casos.
 */
@Service
public class PaymentService {

    private final PaymentStrategyFactory paymentStrategyFactory;

    public PaymentService(PaymentStrategyFactory paymentStrategyFactory) {
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    public PaymentResult process(PaymentRequest request) {
        BigDecimal amount = request.amount();
        String method = request.method();

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);
        BigDecimal fee = strategy.calculateFee(amount);
        BigDecimal total = amount.add(fee);

        return new PaymentResult(method, amount, fee, total, strategy.confirmationMessage());
    }
}
