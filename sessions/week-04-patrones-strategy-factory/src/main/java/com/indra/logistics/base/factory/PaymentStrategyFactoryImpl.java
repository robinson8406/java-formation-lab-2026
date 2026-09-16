package com.indra.logistics.base.factory;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.indra.logistics.base.util.PaymentMethod;
import org.springframework.stereotype.Component;
import com.indra.logistics.base.strategy.PaymentStrategy;

@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactoryImpl(List<PaymentStrategy> strategies) {
            this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::methodCode,
                        Function.identity()
                ));
    }


    public PaymentStrategy getStrategy(PaymentMethod methodCode) {
        return strategies.get(methodCode);
    }


}
