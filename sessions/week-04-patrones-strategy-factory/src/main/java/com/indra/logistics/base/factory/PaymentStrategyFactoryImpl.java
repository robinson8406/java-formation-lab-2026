package com.indra.logistics.base.factory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.AmexPayment;
import com.indra.logistics.base.strategy.BankTransferPayment;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.DebitCardPayment;
import com.indra.logistics.base.strategy.MastercardPayment;
import com.indra.logistics.base.strategy.PayPalPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.VisaPayment;

@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactoryImpl() {
        this(List.of(
                new CashPayment(),
                new VisaPayment(),
                new PayPalPayment(),
                new BankTransferPayment(),
                new MastercardPayment(),
                new DebitCardPayment(),
                new AmexPayment()
        ));
    }

    @Autowired
    public PaymentStrategyFactoryImpl(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(PaymentStrategy::methodCode, Function.identity()));
    }

    @Override
    public PaymentStrategy getStrategy(String methodCode) {
        return Optional.ofNullable(strategies.get(methodCode))
                .orElseThrow(() -> new UnknownPaymentMethodException(methodCode));
    }
}
