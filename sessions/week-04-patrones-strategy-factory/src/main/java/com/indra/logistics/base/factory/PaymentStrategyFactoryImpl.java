package com.indra.logistics.base.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.AmexPayment;
import com.indra.logistics.base.strategy.BankTransferPayment;
import com.indra.logistics.base.strategy.CashPayment;
import com.indra.logistics.base.strategy.DebitCardPayment;
import com.indra.logistics.base.strategy.MasterCardPayment;
import com.indra.logistics.base.strategy.PayPalPayment;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.VisaPayment;

@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactoryImpl()
    {
       this(List.of(
               new CashPayment(),
               new VisaPayment(),
               new PayPalPayment(),
               new BankTransferPayment(),
               new MasterCardPayment(),
               new DebitCardPayment(),
               new AmexPayment()
       ));
    }

    @Autowired
    public PaymentStrategyFactoryImpl(List<PaymentStrategy> strategies)
    {
       this.strategies = strategies.stream()
               .collect(Collectors.toMap(
                       PaymentStrategy::methodCode,
                       Function.identity(),
                       (existing, replacement) -> existing
               ));
    }

    @Override
    public PaymentStrategy getStrategy(String methodCode)
    {
       PaymentStrategy strategy = strategies.get(methodCode);
       if (strategy == null) {
           throw new UnknownPaymentMethodException(methodCode);
       }
       return strategy;
    }
}
