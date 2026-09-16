package com.indra.logistics.base.factory;

import com.indra.logistics.base.util.PaymentMethod;
import com.indra.logistics.base.strategy.PaymentStrategy;

public interface PaymentStrategyFactory {

    PaymentStrategy getStrategy(PaymentMethod methodCode);


}
