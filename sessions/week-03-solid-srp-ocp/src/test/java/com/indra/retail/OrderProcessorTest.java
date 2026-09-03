package com.indra.retail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OrderProcessor — Tests de coordinación del flujo de pedidos (SRP)")
class OrderProcessorTest {

    private StockValidator stockValidator;
    private OrderNotifier orderNotifier;
    private DiscountCalculator discountCalculator;
    private OrderProcessor orderProcessor;

    @BeforeEach
    void setUp() {
        stockValidator = new StockValidator();
        orderNotifier = new OrderNotifier();
        discountCalculator = new DiscountCalculator();
        orderProcessor = new OrderProcessor(stockValidator, orderNotifier, discountCalculator);
    }

    @Nested
    @DisplayName("Flujo exitoso de procesamiento de pedido")
    class SuccessfulOrderProcessingTests {

        @Test
        @DisplayName("Debe procesar pedido con descuento LOYALTY (15%) cuando hay stock suficiente")
        void shouldProcessOrderWithLoyaltyDiscountWhenStockIsSufficient() {
            Order order = new Order("ORD-001", new BigDecimal("100.00"), DiscountType.LOYALTY, 2, "cliente@indra.com");

            BigDecimal finalPrice = orderProcessor.process(order, 10);

            assertEquals(0, new BigDecimal("85.00").compareTo(finalPrice));
        }

        @Test
        @DisplayName("Debe procesar pedido con descuento STANDARD (5%) cuando hay stock suficiente")
        void shouldProcessOrderWithStandardDiscountWhenStockIsSufficient() {
            Order order = new Order("ORD-002", new BigDecimal("100.00"), DiscountType.STANDARD, 3, "cliente@indra.com");

            BigDecimal finalPrice = orderProcessor.process(order, 5);

            assertEquals(0, new BigDecimal("95.00").compareTo(finalPrice));
        }

        @Test
        @DisplayName("Debe procesar pedido con descuento SEASONAL (20%) cuando hay stock suficiente")
        void shouldProcessOrderWithSeasonalDiscountWhenStockIsSufficient() {
            Order order = new Order("ORD-003", new BigDecimal("100.00"), DiscountType.SEASONAL, 1, "cliente@indra.com");

            BigDecimal finalPrice = orderProcessor.process(order, 1);

            assertEquals(0, new BigDecimal("80.00").compareTo(finalPrice));
        }

        @Test
        @DisplayName("Debe procesar pedido sin descuento cuando discountType es nulo")
        void shouldProcessOrderWithoutDiscountWhenTypeIsNull() {
            Order order = new Order("ORD-004", new BigDecimal("100.00"), null, 1, "cliente@indra.com");

            BigDecimal finalPrice = orderProcessor.process(order, 2);

            assertEquals(0, new BigDecimal("100.00").compareTo(finalPrice));
        }
    }

    @Nested
    @DisplayName("Manejo de stock insuficiente")
    class InsufficientStockTests {

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando el stock disponible es menor al solicitado")
        void shouldThrowIllegalStateExceptionWhenStockIsInsufficient() {
            Order order = new Order("ORD-999", new BigDecimal("100.00"), DiscountType.STANDARD, 10, "cliente@indra.com");

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> orderProcessor.process(order, 5)
            );

            assertTrue(exception.getMessage().contains("Stock insuficiente para el pedido ORD-999"));
        }
    }

    @Nested
    @DisplayName("Compatibilidad y diseño de dependencias")
    class DependencyAndConstructorTests {

        @Test
        @DisplayName("Debe soportar constructor con 2 parámetros para retrocompatibilidad")
        void shouldSupportTwoArgConstructor() {
            OrderProcessor legacyProcessor = new OrderProcessor(stockValidator, orderNotifier);
            Order order = new Order("ORD-005", new BigDecimal("100.00"), DiscountType.STANDARD, 1, "cliente@indra.com");

            BigDecimal finalPrice = legacyProcessor.process(order, 5);

            assertNotNull(finalPrice);
            assertEquals(0, new BigDecimal("95.00").compareTo(finalPrice));
        }
    }
}
