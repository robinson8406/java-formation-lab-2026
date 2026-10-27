package com.indra.retail.manager;

import com.indra.retail.entities.Address;
import com.indra.retail.entities.Customer;
import com.indra.retail.entities.Order;
import com.indra.retail.entities.OrderItem;
import com.indra.retail.entities.OrderStatus;
import com.indra.retail.entities.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CRUD básico (crear, consultar, actualizar, eliminar) con EntityManager.
 * Cada test usa datos únicos, así que no dependen del orden de ejecución.
 */
@SpringBootTest
class EntityManagerRecordManagerTest {

    @Autowired
    private EntityManagerRecordManager manager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@indracompany.com";
    }

    @Test
    @DisplayName("Crea un Customer y lo consulta por id")
    void shouldCreateAndFindCustomer() {
        Customer saved = manager.create(new Customer("Ana", uniqueEmail()));

        assertNotNull(saved.getId());
        Customer found = manager.findById(Customer.class, saved.getId()).orElseThrow();
        assertEquals("Ana", found.getName());
    }

    @Test
    @DisplayName("Persiste Address como parte del Customer")
    void shouldPersistEmbeddedAddress() {
        Customer saved = manager.create(new Customer(
                "Sofia",
                uniqueEmail(),
                new Address("Calle 10 #20-30", "Bogota", "110111", "Colombia")
        ));

        Customer found = manager.findById(Customer.class, saved.getId()).orElseThrow();
        assertEquals("Bogota", found.getAddress().getCity());
    }

    @Test
    @DisplayName("Actualiza un Product")
    void shouldUpdateProduct() {
        Product saved = manager.create(new Product("Teclado", new BigDecimal("50.00"), 10));
        Long initialVersion = saved.getVersion();

        saved.setStock(7);
        manager.update(saved);

        Product found = manager.findById(Product.class, saved.getId()).orElseThrow();
        assertEquals(7, found.getStock());
        assertTrue(found.getVersion() > initialVersion);
    }

    @Test
    @DisplayName("Elimina un Customer")
    void shouldDeleteCustomer() {
        Customer saved = manager.create(new Customer("Luis", uniqueEmail()));

        assertTrue(manager.delete(Customer.class, saved.getId()));

        assertFalse(manager.findById(Customer.class, saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Devuelve false al eliminar un registro inexistente")
    void shouldReturnFalseWhenDeletingMissingRecord() {
        assertFalse(manager.delete(Customer.class, Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Lista los Product creados")
    void shouldListProducts() {
        Product saved = manager.create(new Product("Mouse", new BigDecimal("20.00"), 5));

        assertTrue(manager.findAll(Product.class).stream()
                .anyMatch(p -> p.getId().equals(saved.getId())));
    }

    @Test
    @DisplayName("Persiste un Order con sus ítems en cascada")
    void shouldPersistOrderWithItems() {
        // Todo en una transacción: las entidades quedan managed y el test no
        // depende de qué cascade tenga el mapeo.
        Long[] ids = transactionTemplate.execute(status -> {
            Customer customer = manager.create(new Customer("Marta", uniqueEmail()));
            Product product = manager.create(new Product("Monitor", new BigDecimal("300.00"), 3));
            Order order = new Order(customer);
            order.addItem(new OrderItem(product, 2, product.getPrice()));
            manager.create(order);
            return new Long[] {order.getId(), customer.getId()};
        });

        Order found = manager.findById(Order.class, ids[0]).orElseThrow();
        assertEquals(OrderStatus.PENDING, found.getStatus());
        assertEquals(ids[1], found.getCustomer().getId());
    }
}
