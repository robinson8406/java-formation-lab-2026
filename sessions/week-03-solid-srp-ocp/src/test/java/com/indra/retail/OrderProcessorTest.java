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

    private static final BigDecimal INITIAL_PRICE = new BigDecimal("1000");
    private static final int REQUESTED_QTY = 5;
    private static final String CUSTOMER_ID = "1";
    private static final String CUSTOMER_EMAIL = "test@mail.com";

    private Costumer recentCustomer;

    @BeforeEach
    void setUp() {
        orderProcessor = new OrderProcessor(new StockValidator(), new OrderNotifier());
        recentCustomer = new Costumer(CUSTOMER_ID, CUSTOMER_EMAIL, LocalDate.now());
    }


    @ParameterizedTest
    @CsvSource({
            "STANDARD,950",
            "SEASONAL,800"
    })
    @DisplayName("Verifica precios finales para distintos tipos de descuento")
    void processWithVariousDiscounts(DiscountType type, String expected) {
        Order order = createOrderWithType(type, INITIAL_PRICE);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(new BigDecimal(expected)));
    }

    @Test
    @DisplayName("Valida que exista suficiente stock para procesar el pedido")
    void processWithInsufficientStock() {
        Order order = createOrderWithType(DiscountType.STANDARD, INITIAL_PRICE);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderProcessor.process(order, 3));
        assertEquals("Stock insuficiente para el pedido 123", exception.getMessage());
    }

    @Test
    @DisplayName("Verifica descuento 15% loyalty para clientes con más de 1 año de antigüedad")
    void processWithLoyaltyDiscount() {
        Costumer loyalCustomer = new Costumer(CUSTOMER_ID, CUSTOMER_EMAIL, LocalDate.of(2022, 10, 1));
        Order order = new Order("123", INITIAL_PRICE, DiscountType.LOYALTY, REQUESTED_QTY, loyalCustomer);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(new BigDecimal("850")));
    }

    @Test
    @DisplayName("Verifica que no se aplique descuento loyalty para clientes con menos de 1 año de antigüedad")
    void processWithLoyaltyDiscountForNewCustomer() {
        Order order = new Order("123", INITIAL_PRICE, DiscountType.LOYALTY, REQUESTED_QTY, recentCustomer);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(INITIAL_PRICE));
    }

    @Test
    @DisplayName("Verifica que no se aplique descuento loyalty para clientes sin fecha de antigüedad")
    void processWithLoyaltyDiscountForCustomerWithoutJoinDate() {
        Costumer customerWithoutJoinDate = new Costumer(CUSTOMER_ID, CUSTOMER_EMAIL, null);
        Order order = new Order("123", INITIAL_PRICE, DiscountType.LOYALTY, REQUESTED_QTY, customerWithoutJoinDate);
        BigDecimal price = orderProcessor.process(order, 10);
        assertEquals(0, price.compareTo(INITIAL_PRICE));
    }


    private Order createOrderWithType(DiscountType type, BigDecimal price) {
        return new Order("123", price, type, REQUESTED_QTY, recentCustomer);
    }


}