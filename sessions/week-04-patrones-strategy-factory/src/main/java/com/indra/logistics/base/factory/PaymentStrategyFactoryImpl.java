package com.indra.logistics.base.factory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.AmexPayment;
import com.indra.logistics.base.strategy.BankTransferPayment;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.DebitCardPayment;
import com.indra.logistics.base.strategy.MastercardPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.PaypalPayment;
import com.indra.logistics.base.strategy.VisaPayment;

/**
 * Factory con soporte para auto-descubrimiento de estrategias registradas como @Component.
 * Cumple con OCP: agregar una nueva estrategia no requiere modificar esta clase ni PaymentService.
 */
@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    @Autowired
    public PaymentStrategyFactoryImpl(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::methodCode,
                        strategy -> strategy,
                        (existing, replacing) -> existing
                ));
    }

    public PaymentStrategyFactoryImpl() {
        this(List.of(
                new CashPayment(),
                new VisaPayment(),
                new MastercardPayment(),
                new AmexPayment(),
                new PaypalPayment(),
                new BankTransferPayment(),
                new DebitCardPayment()
        ));
    }

    @Override
    public PaymentStrategy getStrategy(String methodCode) {
        if (methodCode == null) {
            throw new UnknownPaymentMethodException("null");
        }
        PaymentStrategy strategy = strategies.get(methodCode);
        if (strategy == null) {
            throw new UnknownPaymentMethodException(methodCode);
        }
        return strategy;
    }
}
