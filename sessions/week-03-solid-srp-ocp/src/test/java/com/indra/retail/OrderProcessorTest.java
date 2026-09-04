package com.indra.retail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderProcessorTest {


    private OrderProcessor orderProcessor;
    private Order order;

    private static final BigDecimal INITIAL_PRICE = new BigDecimal("1000");
    private static final int REQUESTED_QTY = 5;

    @BeforeEach
    void setUp() {
        orderProcessor = new OrderProcessor(new StockValidator(), new OrderNotifier());
    }


    @ParameterizedTest
    @CsvSource({
            "STANDARD,950",
            "SEASONAL,800",
            "LOYALTY,1000"
    })
    @DisplayName("Verifica precios finales para distintos tipos de descuento")
    void processWithVariousDiscounts(DiscountType type, String expected) {
        order = new Order("123", INITIAL_PRICE, type, REQUESTED_QTY, "test@mail.com");
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(new BigDecimal(expected)));
    }

    @Test
    @DisplayName("Valida que exista suficiente stock para procesar el pedido")
    void processWithInsufficientStock() {
        order = new Order("123", INITIAL_PRICE, DiscountType.STANDARD, REQUESTED_QTY, "test@mail.com");
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderProcessor.process(order, 3));
        assertEquals("Stock insuficiente para el pedido 123", exception.getMessage());
    }




}