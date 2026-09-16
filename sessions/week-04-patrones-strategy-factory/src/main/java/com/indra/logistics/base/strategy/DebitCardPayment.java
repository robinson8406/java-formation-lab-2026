package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

import com.indra.logistics.base.util.PaymentMethod;
import org.springframework.stereotype.Component;

import static com.indra.logistics.base.util.MoneyCalculator.round;

@Component
public class DebitCardPayment implements PaymentStrategy {

    private static final BigDecimal COMMISSION_RATE  = new BigDecimal("0.04");

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.DEBIT_CARD;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return round(amount.multiply(COMMISSION_RATE));
    }

    @Override
    public String confirmationMessage() {
        return "Pago con tarjeta de débito procesado, se aplica comisión bancaria.";
    }
}
