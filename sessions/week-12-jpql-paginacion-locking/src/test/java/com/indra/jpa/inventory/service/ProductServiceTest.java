package com.indra.jpa.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.hibernate.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.repository.ProductRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Test de 2 threads concurrentes: solo 1 triunfa `reserveStock()` maneja `OptimisticLockException`")
    void concurrentReservationsAllowOnlyOneSuccess() throws Exception {
        Product product = new Product();
        product.setName("Concurrent reservation");
        product.setPrice(new BigDecimal("49.99"));
        product.setStock(1);
        product.setVersion(1L);
        Product savedProduct = productRepository.saveAndFlush(product);
        Long productId = savedProduct.getId();
        Long initialVersion = savedProduct.getVersion();
        CountDownLatch productsLoaded = new CountDownLatch(2);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        Callable<Boolean> reserve = () -> {
            try {
                transactionTemplate.executeWithoutResult(transaction -> {
                    productRepository.findById(productId).orElseThrow();
                    productsLoaded.countDown();
                    try {
                        if (!productsLoaded.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Both transactions must load the product");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Reservation interrupted", exception);
                    }
                    productService.reserveStock(productId, 1);
                });
                return true;
            } catch (ConcurrentModificationException exception) {
                return false;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> firstReservation = executor.submit(reserve);
            Future<Boolean> secondReservation = executor.submit(reserve);

            boolean firstSucceeded = firstReservation.get(20, TimeUnit.SECONDS);
            boolean secondSucceeded = secondReservation.get(20, TimeUnit.SECONDS);
            assertTrue(firstSucceeded ^ secondSucceeded);
            Product updatedProduct = productRepository.findById(productId).orElseThrow();
            assertEquals(0, updatedProduct.getStock());
            assertEquals(initialVersion + 1, updatedProduct.getVersion());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(20, TimeUnit.SECONDS);
            productRepository.deleteById(productId);
        }
    }

    @Test
    @DisplayName("Test Consulta de productos en transacción de solo lectura")
    void searchProductsRunsInReadOnlyTransaction() {
        Pageable pageable = PageRequest.of(0, 5);
        AtomicBoolean transactionActive = new AtomicBoolean();
        AtomicBoolean transactionReadOnly = new AtomicBoolean();
        AtomicBoolean sessionReadOnly = new AtomicBoolean();
        doAnswer(invocation -> {
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            transactionReadOnly.set(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            sessionReadOnly.set(entityManager.unwrap(Session.class).isDefaultReadOnly());
            return Page.empty(pageable);
        }).when(productRepository).searchProducts(any(), eq(pageable));

        productService.searchProducts("keyboard", null, null, pageable);

        verify(productRepository).searchProducts(any(), eq(pageable));
        assertAll(
                () -> assertTrue(transactionActive.get()),
                () -> assertTrue(transactionReadOnly.get()),
                () -> assertTrue(sessionReadOnly.get()));
    }

    @Test
    @DisplayName("Test de 3 threads concurrentes: solo 1 triunfa `increaseStock()` maneja `Pessimistic Lock`")
    void increaseStockLocksProductUntilTransactionCompletes() throws Exception {
        Product product = new Product();
        product.setName("Pessimistic lock test");
        product.setPrice(new BigDecimal("49.99"));
        product.setStock(10);
        Long productId = productRepository.saveAndFlush(product).getId();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch transactionsReady = new CountDownLatch(3);
        CountDownLatch startUpdates = new CountDownLatch(1);
        CountDownLatch firstUpdateFinished = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);
        AtomicInteger firstUpdate = new AtomicInteger(-1);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        List<Callable<Product>> operations = IntStream.range(0, 3).mapToObj(index -> (Callable<Product>) () ->
                transactionTemplate.execute(transaction -> {
                    transactionsReady.countDown();
                    try {
                        if (!startUpdates.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Updates were not started");
                        }
                        Product result = productService.increaseStock(productId, 1);
                        if (firstUpdate.compareAndSet(-1, index)) {
                            firstUpdateFinished.countDown();
                            if (!releaseFirstTransaction.await(20, TimeUnit.SECONDS)) {
                                throw new IllegalStateException("First transaction was not released");
                            }
                        }
                        return result;
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Test interrupted", exception);
                    }
                })).toList();
        try {
            List<Future<Product>> updates = operations.stream().map(executor::submit).toList();
            assertTrue(transactionsReady.await(5, TimeUnit.SECONDS));
            startUpdates.countDown();
            assertTrue(firstUpdateFinished.await(5, TimeUnit.SECONDS));
            int successfulIndex = firstUpdate.get();
            for (int index = 0; index < updates.size(); index++) {
                if (index != successfulIndex) {
                    Future<Product> failedUpdate = updates.get(index);
                    ExecutionException exception = assertThrows(ExecutionException.class,
                            () -> failedUpdate.get(10, TimeUnit.SECONDS));
                    assertInstanceOf(PessimisticLockingFailureException.class, exception.getCause());
                }
            }
            releaseFirstTransaction.countDown();

            Product successfulProduct = updates.get(successfulIndex).get(10, TimeUnit.SECONDS);
            assertEquals(productId, successfulProduct.getId());
            assertEquals(11, successfulProduct.getStock());
            assertEquals(11, productRepository.findById(productId).orElseThrow().getStock());
        } finally {
            startUpdates.countDown();
            releaseFirstTransaction.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            productRepository.deleteById(productId);
        }
    }



}