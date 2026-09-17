package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

public interface PaymentStrategy {

    String methodCode();

    BigDecimal calculateFee(BigDecimal amount);

    String confirmationMessage();
}
