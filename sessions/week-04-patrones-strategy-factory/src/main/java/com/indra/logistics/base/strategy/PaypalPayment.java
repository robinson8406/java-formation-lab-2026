package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

import com.indra.logistics.base.util.PaymentMethod;
import org.springframework.stereotype.Component;

import static com.indra.logistics.base.util.MoneyCalculator.round;

@Component
public class PaypalPayment implements PaymentStrategy {

    private static final BigDecimal COMMISSION_RATE  = new BigDecimal("0.02");

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return round(amount.multiply(COMMISSION_RATE));
    }

    @Override
    public String confirmationMessage() {
        return "Pago con PayPal procesado, comisión de plataforma aplicada.";
    }
}
