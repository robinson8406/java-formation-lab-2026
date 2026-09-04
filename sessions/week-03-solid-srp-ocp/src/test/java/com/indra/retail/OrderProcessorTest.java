package com.indra.retail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

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
            "SEASONAL,800"
    })
    @DisplayName("Verifica precios finales para distintos tipos de descuento")
    void processWithVariousDiscounts(DiscountType type, String expected) {
        Costumer costumer =new Costumer("1","test@mail.com",LocalDate.of(2025, 10, 1));
        order = new Order("123", INITIAL_PRICE, type, REQUESTED_QTY, costumer);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(new BigDecimal(expected)));
    }

    @Test
    @DisplayName("Valida que exista suficiente stock para procesar el pedido")
    void processWithInsufficientStock() {
        Costumer costumer =new Costumer("1","test@mail.com",LocalDate.of(2025, 10, 1));
        order = new Order("123", INITIAL_PRICE, DiscountType.STANDARD, REQUESTED_QTY, costumer);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderProcessor.process(order, 3));
        assertEquals("Stock insuficiente para el pedido 123", exception.getMessage());
    }

    @Test
    @DisplayName("Verifica descuento 15% loyalty para clientes con más de 1 año de antigüedad")
    void processWithLoyaltyDiscount() {
        Costumer costumer = new Costumer("1", "test@mail.com", LocalDate.of(2022, 10, 1));
        order = new Order("123", INITIAL_PRICE, DiscountType.LOYALTY, REQUESTED_QTY, costumer);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(new BigDecimal("975")));
    }

    @Test
    @DisplayName("Verifica que no se aplique descuento loyalty para clientes con menos de 1 año de antigüedad")
    void processWithLoyaltyDiscountForNewCustomer() {
        Costumer costumer = new Costumer("1", "test@mail.com", LocalDate.now());
        order = new Order("123", INITIAL_PRICE, DiscountType.LOYALTY, REQUESTED_QTY, costumer);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(new BigDecimal("1000")));
    }


}