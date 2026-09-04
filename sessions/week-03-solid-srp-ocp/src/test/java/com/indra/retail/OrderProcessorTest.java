package com.indra.retail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OrderProcessorTest {

    private final OrderProcessor orderProcesor =  new OrderProcessor(new StockValidator(), new OrderNotifier());

    public @Test
    @DisplayName("Debe calcular el finalPrice para la orden tipo Seasonal")
    void shouldProcesOrder() {
    	Order order =  new Order("1", BigDecimal.valueOf(1000), new DiscountStrategySeasonal(), 10, "mail@indra.es");

    	BigDecimal value = orderProcesor.process(order, 50000);
    	assertEquals(value, BigDecimal.valueOf(800.0));
    }
}
