package com.indra.retail;

import java.math.BigDecimal;

public class DiscountStrategySeasonal implements DiscountStrategy {

	@Override
	public BigDecimal applyDiscount(Order order) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.80));
	}

}
