package com.indra.logistics.base.factory;

import org.springframework.stereotype.Component;

import com.indra.logistics.base.domain.PaymentMethod;
import com.indra.logistics.base.strategy.PaymentStrategy;

/** Factory */
@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    @Override
    public PaymentStrategy getStrategy(String methodCode) {
        PaymentMethod paymentMethod = PaymentMethod.fromMethodCode(methodCode);
        return switch (paymentMethod) {
            case CASH -> new com.indra.logistics.base.strategy.CashPayment();
            case VISA -> new com.indra.logistics.base.strategy.VisaPayment();
            case PAYPAL -> new com.indra.logistics.base.strategy.PaypalPayment();
            default -> throw new com.indra.logistics.base.UnknownPaymentMethodException(methodCode);
        };
    }
}
