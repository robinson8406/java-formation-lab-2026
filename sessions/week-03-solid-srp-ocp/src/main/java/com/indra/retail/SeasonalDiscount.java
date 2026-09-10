package com.indra.retail;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SeasonalDiscount implements DiscountType {

	private static final BigDecimal RATE = new BigDecimal("0.10");

	@Override
	public BigDecimal applyDiscount(BigDecimal price) {
		BigDecimal discount = price.multiply(RATE).setScale(0, RoundingMode.HALF_UP);
		return price.subtract(discount);
	}
}
