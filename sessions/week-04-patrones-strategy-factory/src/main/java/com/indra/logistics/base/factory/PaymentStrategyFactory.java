package com.indra.logistics.base.factory;

import com.indra.logistics.base.strategy.PaymentStrategy;

public interface PaymentStrategyFactory {

    PaymentStrategy getStrategy(String methodCode);
}
