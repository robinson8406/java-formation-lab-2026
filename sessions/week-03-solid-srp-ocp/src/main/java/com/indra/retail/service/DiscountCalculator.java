package com.indra.retail.service;

import com.indra.retail.domain.model.Order;
import java.math.BigDecimal;

public class DiscountCalculator {


    public BigDecimal apply(Order order) {
        return order.getDiscountStrategy().calculate(order);
    }

}
