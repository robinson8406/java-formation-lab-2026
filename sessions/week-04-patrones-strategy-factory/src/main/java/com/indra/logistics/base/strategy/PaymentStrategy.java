package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

/** Contrato Strategy: cada método de pago sabe calcular su propia comisión y mensaje. */
public interface PaymentStrategy {

    String methodCode();

    BigDecimal calculateFee(BigDecimal amount);

    String confirmationMessage();
}
