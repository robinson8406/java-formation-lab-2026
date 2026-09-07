package com.indra.retail;

import com.indra.retail.domain.discount.DiscountStrategy;
import com.indra.retail.domain.discount.LoyaltyDiscount;
import com.indra.retail.domain.discount.SeasonalDiscount;
import com.indra.retail.domain.discount.StandardDiscount;
import com.indra.retail.domain.model.Costumer;
import com.indra.retail.domain.model.Order;
import com.indra.retail.notification.OrderNotifier;
import com.indra.retail.service.OrderProcessor;
import com.indra.retail.validation.StockValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {


    private OrderProcessor orderProcessor;

    private static final BigDecimal INITIAL_PRICE = new BigDecimal("1000");
    private static final int REQUESTED_QTY = 5;
    private static final int AVAILABLE_STOCK = 10;
    private static final int INSUFFICIENT_STOCK = 3;


    private static final String ORDER_ID = "123";
    private static final String CUSTOMER_ID = "1";
    private static final String CUSTOMER_EMAIL = "test@mail.com";

    private static final BigDecimal STANDARD_PRICE = new BigDecimal("950");
    private static final BigDecimal SEASONAL_PRICE = new BigDecimal("800");
    private static final BigDecimal LOYALTY_PRICE = new BigDecimal("850");


    private Costumer recentCustomer;

    @BeforeEach
    void setUp() {
        orderProcessor = new OrderProcessor(new StockValidator(), new OrderNotifier());
        recentCustomer = new Costumer(CUSTOMER_ID, CUSTOMER_EMAIL, LocalDate.of(2026, 9, 7));
    }


    @ParameterizedTest
    @MethodSource("discountStrategies")
    @DisplayName("Verifica precios finales para distintas estrategias de descuento")
    void processWithVariousDiscounts(DiscountStrategy strategy,BigDecimal expectedPrice) {
        Order order = createOrder(strategy);
        BigDecimal price =orderProcessor.process(order,AVAILABLE_STOCK);
        assertEquals(0,price.compareTo(expectedPrice));
    }


    @Test
    @DisplayName("Valida que exista suficiente stock para procesar el pedido")
    void processWithInsufficientStock() {
        Order order = createOrder(new StandardDiscount());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderProcessor.process(order, INSUFFICIENT_STOCK));
        assertEquals("Stock insuficiente para el pedido 123", exception.getMessage());
    }


    @Test
    @DisplayName("Verifica descuento 15% loyalty para clientes con más de 1 año de antigüedad")
    void processWithLoyaltyDiscount() {
        Costumer loyalCustomer = new Costumer(CUSTOMER_ID, CUSTOMER_EMAIL, LocalDate.of(2022, 10, 1));
        Order order = new Order(ORDER_ID, INITIAL_PRICE, new LoyaltyDiscount(), REQUESTED_QTY, loyalCustomer);
        BigDecimal price = orderProcessor.process(order, AVAILABLE_STOCK);
        assertEquals(0, price.compareTo(LOYALTY_PRICE));
    }


    @Test
    @DisplayName("Verifica que no se aplique descuento loyalty para clientes con menos de 1 año de antigüedad")
    void processWithLoyaltyDiscountForNewCustomer() {
        Order order = new Order(ORDER_ID, INITIAL_PRICE, new LoyaltyDiscount(), REQUESTED_QTY, recentCustomer);
        BigDecimal price = orderProcessor.process(order, AVAILABLE_STOCK);
        assertEquals(0, price.compareTo(INITIAL_PRICE));
    }


    @Test
    @DisplayName("Verifica que no se aplique descuento loyalty para clientes sin fecha de antigüedad")
    void processWithLoyaltyDiscountForCustomerWithoutJoinDate() {
        Costumer customerWithoutJoinDate = new Costumer(CUSTOMER_ID, CUSTOMER_EMAIL, null);
        Order order = new Order(ORDER_ID, INITIAL_PRICE, new LoyaltyDiscount(), REQUESTED_QTY, customerWithoutJoinDate);
        BigDecimal price = orderProcessor.process(order, AVAILABLE_STOCK);
        assertEquals(0, price.compareTo(INITIAL_PRICE));
    }


    private Order createOrder(DiscountStrategy discountStrategy) {
        return new Order(
                ORDER_ID,
                INITIAL_PRICE,
                discountStrategy,
                REQUESTED_QTY,
                recentCustomer);
    }


    private static Stream<Arguments> discountStrategies() {
        return Stream.of(
            Arguments.of(new StandardDiscount(),STANDARD_PRICE),
            Arguments.of(new SeasonalDiscount(),SEASONAL_PRICE)
        );
    }


}