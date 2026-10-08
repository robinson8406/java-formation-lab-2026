package com.indra.logistics.base.factory;

import org.springframework.stereotype.Component;

import com.indra.logistics.base.UnknownPaymentMethodException;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.strategy.impl.CashPaymentStrategy;
import com.indra.logistics.base.strategy.impl.PaypalPaymentStrategy;
import com.indra.logistics.base.strategy.impl.VisaPaymentStrategy;
import com.indra.logistics.base.strategy.impl.AmexPaymentStrategy;
import com.indra.logistics.base.strategy.impl.BankTransferPaymentStrategy;
import com.indra.logistics.base.strategy.impl.MasterCardPaymentStrategy;
import com.indra.logistics.base.strategy.impl.DebitCardPaymentStrategy;

/**
 * Factory
 */
@Component
public class PaymentStrategyFactoryImpl implements PaymentStrategyFactory {

    @Override
    public PaymentStrategy getStrategy(String methodCode) {
        return switch (methodCode) {
            case "CASH" ->
                new CashPaymentStrategy();
            case "VISA" ->
                new VisaPaymentStrategy();
            case "PAYPAL" ->
                new PaypalPaymentStrategy();
            case "AMEX" ->
                new AmexPaymentStrategy();
            case "BANK_TRANSFER" ->
                new BankTransferPaymentStrategy();
            case "MASTER_CARD" ->
                new MasterCardPaymentStrategy();
            case "DEBIT_CARD" ->
                new DebitCardPaymentStrategy();
            default ->
                throw new UnknownPaymentMethodException(methodCode);
        };

    }
}
