package com.indra.retail.domain.discount;

import com.indra.retail.domain.model.Order;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

public final class LoyaltyDiscount implements DiscountStrategy {

    @Override
    public BigDecimal calculate(Order order) {
       
        LocalDate joinDate = order.getCustomer().getCustomerJoinDate();
        if (joinDate == null) {
            return order.getPrice();
        }
        long months = ChronoUnit.MONTHS.between(joinDate, LocalDate.now());
        if (months > 12) {
            return order.getPrice().multiply(BigDecimal.valueOf(0.85)); // 15% off
        }
        return order.getPrice();
    }
}
