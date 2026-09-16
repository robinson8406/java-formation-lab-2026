package com.indra.logistics.base.strategy.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.strategy.PaymentStrategy;

public class ThresholdFeeWaiverDecorator implements PaymentStrategy {

    private final PaymentStrategy delegate;
    private final BigDecimal threshold;
    private final String waivedMessage;
    private boolean feeWaived;

    public ThresholdFeeWaiverDecorator(PaymentStrategy delegate, BigDecimal threshold, String waivedMessage) {
        this.delegate = delegate;
        this.threshold = threshold;
        this.waivedMessage = waivedMessage;
    }

    @Override
    public String methodCode() {
        return delegate.methodCode();
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        feeWaived = amount.compareTo(threshold) < 0;
        return feeWaived
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : delegate.calculateFee(amount);
    }

    @Override
    public String confirmationMessage() {
        return feeWaived ? waivedMessage : delegate.confirmationMessage();
    }
}