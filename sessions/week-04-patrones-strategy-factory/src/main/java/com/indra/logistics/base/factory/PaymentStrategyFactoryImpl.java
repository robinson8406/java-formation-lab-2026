package com.indra.logistics.base.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.PaymentStrategy;

/** Factory con auto-descubrimiento: Spring inyecta todos los @Component PaymentStrategy sin listarlos a mano. */
@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategiesByCode;

    public PaymentStrategyFactoryImpl(List<PaymentStrategy> strategies) {
        this.strategiesByCode = strategies.stream()
                .collect(java.util.stream.Collectors.toMap(PaymentStrategy::methodCode, Function.identity()));
    }

    public List<String> registeredMethodCodes() {
        return strategiesByCode.keySet().stream().sorted().toList();
    }

    @Override
    public PaymentStrategy getStrategy(String methodCode) {
        PaymentStrategy strategy = strategiesByCode.get(methodCode);
        if (strategy == null) {
            throw new UnknownPaymentMethodException(methodCode);
        }
        return strategy;
    }
}
