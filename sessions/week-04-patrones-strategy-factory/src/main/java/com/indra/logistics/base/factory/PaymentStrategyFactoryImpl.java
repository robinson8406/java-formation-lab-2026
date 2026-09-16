package com.indra.logistics.base.factory;


import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.ThresholdWaivable;
import com.indra.logistics.base.strategy.decorator.ThresholdFeeWaiverDecorator;
import org.springframework.stereotype.Component;

import com.indra.logistics.base.strategy.PaymentStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Factory */
@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategiesByCode;

    public PaymentStrategyFactoryImpl(List<PaymentStrategy> strategies) {
        this.strategiesByCode = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(PaymentStrategy::methodCode, Function.identity()));
    }

    @Override
    public PaymentStrategy getStrategy(String methodCode) {
        PaymentStrategy strategy = strategiesByCode.get(methodCode);
        if (strategy == null) {
            throw new UnknownPaymentMethodException(methodCode);
        }
        if (strategy instanceof ThresholdWaivable waivable) {
            return new ThresholdFeeWaiverDecorator(strategy, waivable.threshold(), waivable.waivedMessage());
        }
        return strategy;
    }
}
