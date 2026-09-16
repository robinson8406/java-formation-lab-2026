package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

import com.indra.logistics.base.util.PaymentMethod;
import org.springframework.stereotype.Component;

import static com.indra.logistics.base.util.MoneyCalculator.round;

@Component
public class MastercardPayment implements PaymentStrategy {

    private static final BigDecimal COMMISSION_RATE  = new BigDecimal("0.03");
    private static final BigDecimal MINIMUM_COMMISSION_AMOUNT = new BigDecimal("100");

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.MASTERCARD;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(MINIMUM_COMMISSION_AMOUNT) < 0) {
            return BigDecimal.ZERO;
        }
        return round(amount.multiply(COMMISSION_RATE));
    }

    @Override
    public String confirmationMessage() {
        return "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
    }

    @Override
    public String confirmationMessage(BigDecimal amount) {
        if (amount != null && amount.compareTo(MINIMUM_COMMISSION_AMOUNT) < 0) {
            return "Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.";
        }
        return confirmationMessage();
    }
}
