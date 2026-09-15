package com.indra.logistics.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.factory.PaymentStrategyFactoryImpl;
import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.util.PaymentUtils;

/**
 * BASE: toda la lógica de comisión vive en un if-else que crece con cada método
 * de pago nuevo.
 * Agregar un método de pago implica editar esta clase y arriesgar los demás
 * casos.
 */
public class PaymentService {

    public PaymentResult process(PaymentRequest request) {
        BigDecimal amount = request.amount();
        String method = request.method();

        PaymentStrategyFactoryImpl factory = new PaymentStrategyFactoryImpl();
        PaymentStrategy strategy = factory.getStrategy(method);

        BigDecimal fee;
        String message;

        fee = strategy.calculateFee(amount);
        message = strategy.confirmationMessage();

/* 
            
     if ("BANK_TRANSFER".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.025)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago por transferencia bancaria registrado, comisión bancaria aplicada.";
        } else if ("MASTERCARD".equals(method)) {
            // Si el monto es menor a 100, no se aplica comisión
            if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
                fee = BigDecimal.ZERO;
                message = "Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.";
            } else {
                BigDecimal tariff = PaymentUtils.creditCardTariff().add(BigDecimal.ZERO);
                fee = amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
                message = "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
            }
        } else if ("DEBIT_CARD".equals(method)) {
            fee = amount.multiply(BigDecimal.valueOf(0.04)).setScale(2, RoundingMode.HALF_UP);
            message = "Pago con tarjeta de débito procesado, se aplica comisión bancaria.";
        } else if ("AMEX".equals(method)) {
            // Si el monto es menor a 100, no se aplica comisión
            if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
                fee = BigDecimal.ZERO;
                message = "Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.";
            } else {
                BigDecimal tariff = PaymentUtils.creditCardTariff().add(BigDecimal.ZERO);
                fee = amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
                message = "Pago con tarjeta American Express procesado, se aplica comisión bancaria.";
            }
        } else {
            throw new UnknownPaymentMethodException(method);
        }
*/
        BigDecimal total = amount.add(fee);
        return new PaymentResult(method, amount, fee, total, message);
    }

}