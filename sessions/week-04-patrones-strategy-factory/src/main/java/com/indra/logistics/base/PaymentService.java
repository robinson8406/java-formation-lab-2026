package com.indra.logistics.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BASE: toda la lógica de comisión vive en un if-else que crece con cada método de pago nuevo.
 * Agregar un método de pago implica editar esta clase y arriesgar los demás casos.
 */
public class PaymentService {

    public PaymentResult process(PaymentRequest request) {
        BigDecimal amount = request.amount();
        String method = request.method();

        BigDecimal fee;
        String message;

        if ("VISA".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.035)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.";
        } else if ("PAYPAL".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.02)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con PayPal procesado, comisión de plataforma aplicada.";
        } else if ("CASH".equals(method)) {
            fee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            message = "Pago en efectivo registrado, sin comisión.";
        } else if ("BANK_TRANSFER".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.025)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago por transferencia bancaria registrado, comisión bancaria aplicada.";
        } else if ("MASTERCARD".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
        } else if ("DEBIT_CARD".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.04)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta de débito procesado, se aplica comisión bancaria.";
        }  else if ("AMEX".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.032)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta American Express procesado, se aplica comisión bancaria.";
        } else {
            throw new IllegalArgumentException("método de pago no soportado");
        }

        BigDecimal total = amount.add(fee);
        return new PaymentResult(method, amount, fee, total, message);
    }
}
