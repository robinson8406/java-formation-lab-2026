package com.indra.retail;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

public class LoyaltyDiscount implements Discounts{
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
