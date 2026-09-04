package com.indra.retail;

import java.math.BigDecimal;

public interface DiscountStrategy{

	default BigDecimal applyDiscount(Order order) {
		return order.getPrice();
	}
}