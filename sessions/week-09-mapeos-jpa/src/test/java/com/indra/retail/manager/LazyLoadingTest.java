package com.indra.retail.manager;

import com.indra.retail.entities.Address;
import com.indra.retail.entities.Customer;
import com.indra.retail.entities.Order;
import com.indra.retail.entities.OrderItem;
import com.indra.retail.entities.Product;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LazyLoadingTest {

    private final EntityManagerRecordManager manager;
    private final TransactionTemplate transactionTemplate;

    private Long orderId;

    @Autowired
    public LazyLoadingTest(EntityManagerRecordManager manager, TransactionTemplate transactionTemplate) {
        this.manager = manager;
        this.transactionTemplate = transactionTemplate;
    }

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

    @Test
    @DisplayName("Customer de un Order no se carga hasta que se usa (LAZY)")
    void shouldLoadCustomerLazily() {
        Order detachedOrder = manager.findById(Order.class, orderId).orElseThrow();

        assertThrows(LazyInitializationException.class, () -> detachedOrder.getCustomer().getName());

        String customerName = transactionTemplate.execute(status -> {
            Order managedOrder = manager.findById(Order.class, orderId).orElseThrow();
            return managedOrder.getCustomer().getName();
        });

        assertEquals("Carla", customerName);
    }

    @Test
    @DisplayName("Quitar un ítem de la lista elimina la fila (orphanRemoval)")
    void shouldRemoveOrphanItem() {
        Long itemId = transactionTemplate.execute(status -> {
            Order order = manager.findById(Order.class, orderId).orElseThrow();
            return order.getItems().get(0).getId();
        });

        transactionTemplate.executeWithoutResult(status -> {
            Order order = manager.findById(Order.class, orderId).orElseThrow();
            OrderItem itemToRemove = order.getItems().get(0);
            order.removeItem(itemToRemove);
            manager.update(order);
        });

        Optional<OrderItem> deletedItem = manager.findById(OrderItem.class, itemId);
        assertTrue(deletedItem.isEmpty());
    }

    @Test
    @DisplayName("No se puede guardar un OrderItem sin Order")
    void shouldRejectOrphanOrderItem() {
        Product product = transactionTemplate.execute(status ->
                manager.create(new Product("Mouse Gamer", new BigDecimal("45.00"), 10))
        );

        OrderItem orphanItem = new OrderItem(product, 2, product.getPrice());

        assertThrows(Exception.class, () ->
                transactionTemplate.executeWithoutResult(status -> manager.create(orphanItem))
        );
    }

    @Test
    @DisplayName("Borrar un OrderItem no borra el Product")
    void shouldNotDeleteProductWhenDeletingItem() {
        Long[] productAndItemIds = transactionTemplate.execute(status -> {
            Product product = manager.create(new Product("Teclado RGB", new BigDecimal("80.00"), 15));
            Customer customer = manager.create(new Customer("David", UUID.randomUUID() + "@indra.com"));
            Order order = new Order(customer);
            OrderItem item = new OrderItem(product, 1, product.getPrice());
            order.addItem(item);
            manager.create(order);
            return new Long[] {product.getId(), item.getId()};
        });

        assertNotNull(productAndItemIds);
        Long productId = productAndItemIds[0];
        Long itemId = productAndItemIds[1];

        boolean deleted = manager.delete(OrderItem.class, itemId);
        assertTrue(deleted);

        Optional<Product> productAfterItemDeletion = manager.findById(Product.class, productId);
        assertTrue(productAfterItemDeletion.isPresent());
        assertEquals("Teclado RGB", productAfterItemDeletion.get().getName());
    }

    @Test
    @DisplayName("Bonus: Bloqueo optimista con @Version en Product lanza excepcion ante conflicto")
    void shouldSupportOptimisticLockingOnProduct() {
        Product product = manager.create(new Product("Tablet", new BigDecimal("300.00"), 8));
        assertNotNull(product.getVersion());

        Product instance1 = manager.findById(Product.class, product.getId()).orElseThrow();
        Product instance2 = manager.findById(Product.class, product.getId()).orElseThrow();

        instance1.setStock(7);
        manager.update(instance1);

        instance2.setStock(6);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> manager.update(instance2));
    }

    @Test
    @DisplayName("Bonus: Customer persiste y recupera direccion embebida @Embeddable correctamente")
    void shouldPersistAndRetrieveCustomerWithEmbeddedAddress() {
        Address address = new Address("Avenida Siempreviva 742", "Springfield", "12345");
        Customer customer = new Customer("Homero", UUID.randomUUID() + "@indra.com", address);

        Customer saved = manager.create(customer);
        assertNotNull(saved.getId());

        Customer retrieved = manager.findById(Customer.class, saved.getId()).orElseThrow();
        assertNotNull(retrieved.getAddress());
        assertEquals("Avenida Siempreviva 742", retrieved.getAddress().getStreet());
        assertEquals("Springfield", retrieved.getAddress().getCity());
        assertEquals("12345", retrieved.getAddress().getZipCode());
    }
}
