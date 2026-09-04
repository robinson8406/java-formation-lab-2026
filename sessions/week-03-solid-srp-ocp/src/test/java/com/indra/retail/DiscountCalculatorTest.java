package com.indra.retail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountCalculatorTest {

	@Test
	@DisplayName("Debe validar tipo Standar")
	void shouldCalculateDiscountStandar() {
		 DiscountStrategy disc =  new DiscountStrategyStandar();
		 Order order =  new Order("1", BigDecimal.valueOf(1000), disc, 10, "mail@indra.es");
		 
		 assertEquals(
				    0,
				    new BigDecimal("950")
				        .compareTo(disc.applyDiscount(order))
				);
		
	}
	
	@Test
	@DisplayName("Debe validar tipo Seasonal")
	void shouldCalculateDiscountSesonal() {
		 DiscountStrategy disc =  new DiscountStrategySeasonal();
		 Order order =  new Order("1", BigDecimal.valueOf(1000), disc, 10, "mail@indra.es");
		 
		 assertEquals(
				    0,
				    new BigDecimal("800")
				        .compareTo(disc.applyDiscount(order))
				);
		
	}
	
	@Test
	@DisplayName("Debe validar tipo default")
	void shouldCalculateDiscountDefault() {
		 DiscountStrategy disc =  new DiscountStrategyNoDiscount();
		 Order order =  new Order("1", BigDecimal.valueOf(1000), disc, 10, "mail@indra.es");
		 
		 assertEquals(BigDecimal.valueOf(1000), disc.applyDiscount(order));
		
	}
} 