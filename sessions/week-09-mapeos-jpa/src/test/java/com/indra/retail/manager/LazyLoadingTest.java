package com.indra.retail.manager;

import com.indra.retail.entities.Customer;
import com.indra.retail.entities.Order;
import com.indra.retail.entities.OrderItem;
import com.indra.retail.entities.Product;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LazyLoadingTest {

    @Autowired
    private EntityManagerRecordManager manager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long orderId;

    @BeforeEach
    void createOrderWithItems() {
        orderId = transactionTemplate.execute(status -> {
            Customer customer = manager.create(new Customer("Carla", UUID.randomUUID() + "@indra.com"));
            Product product = manager.create(new Product("Laptop", new BigDecimal("1500.00"), 4));
            Order order = new Order(customer);
            order.addItem(new OrderItem(product, 1, product.getPrice()));
            return manager.create(order).getId();
        });
    }

    @Test
    @DisplayName("Acceder a items fuera de la transacción lanza LazyInitializationException")
    void shouldThrowLazyInitializationExceptionOutsideTransaction() {
        // findById abre y cierra su transacción: la Order sale DETACHED
        Order order = manager.findById(Order.class, orderId).orElseThrow();

        assertThrows(LazyInitializationException.class, () -> order.getItems().size());
    }

    @Test
    @DisplayName("Acceder a items dentro de una transacción activa funciona")
    void shouldLoadItemsInsideTransaction() {
        int itemCount = transactionTemplate.execute(status -> {
            Order order = manager.findById(Order.class, orderId).orElseThrow();
            return order.getItems().size();
        });

        assertEquals(1, itemCount);
    }

    // ───────────────────────── Retos para participantes ─────────────────────────

    @Test
    @DisplayName("Customer de un Order no se carga hasta que se usa (LAZY)")
    void shouldLoadCustomerLazily() {

        Order detachedOrder = manager.findById(Order.class, orderId).orElseThrow();

        assertThrows(LazyInitializationException.class, () -> detachedOrder.getCustomer().getName());

        String customerName = transactionTemplate.execute(status -> {
            Order managedOrder = manager.findById(Order.class, orderId).orElseThrow();
            return managedOrder.getCustomer().getName();});

        assertEquals("Carla", customerName);
    }

    @Test
    @DisplayName("Quitar un ítem de la lista elimina la fila (orphanRemoval)")
    void shouldRemoveOrphanItem() {
        
        Long itemId = transactionTemplate.execute(status -> { Order order = manager.findById(Order.class, orderId).orElseThrow();
        OrderItem item = order.getItems().get(0);
        Long id = item.getId();
        order.getItems().remove(item);
        return id;});

        assertFalse(manager.findById(OrderItem.class, itemId).isPresent());
    }

    @Test
    @DisplayName("No se puede guardar un OrderItem sin Order")
    void shouldRejectOrphanOrderItem() {

        Product product = manager.create(new Product("Mouse", new BigDecimal("20.00"), 10));
        assertThrows(Exception.class, () -> transactionTemplate.execute(status -> {
        OrderItem item = new OrderItem(product, 1, product.getPrice());
        manager.create(item);
        return null;}));
    }

    @Test
    @DisplayName("Borrar un OrderItem no borra el Product")
    void shouldNotDeleteProductWhenDeletingItem() {

        Long[] ids = transactionTemplate.execute(status -> {

            Customer customer = manager.create(new Customer("Juan", UUID.randomUUID() + "@indra.com"));
            Product product = manager.create(new Product("Mouse", new BigDecimal("20.00"), 10));
            Order order = new Order(customer);
            OrderItem item = new OrderItem(product,1,product.getPrice());
            order.addItem(item);
            manager.create(order);

            return new Long[] {item.getId(),product.getId()};
        });

        transactionTemplate.execute(status -> {
            manager.delete(OrderItem.class, ids[0]);
            return null;
        });

        assertTrue(
                manager.findById(Product.class, ids[1]).isPresent()
        );
    }
}
