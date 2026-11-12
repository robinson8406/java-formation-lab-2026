package com.indra.jpa.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.indra.jpa.inventory.model.Product;
import com.indra.jpa.inventory.repository.ProductRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
class ProductServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceTest.class);

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
        LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Inicio: se espera una reserva exitosa y un conflicto optimista");
        Product product = new Product();
        product.setName("Concurrent reservation");
        product.setPrice(new BigDecimal("49.99"));
        product.setStock(1);
        product.setVersion(1L);
        Product savedProduct = productRepository.saveAndFlush(product);
        Long productId = savedProduct.getId();
        Long initialVersion = savedProduct.getVersion();
        LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Fixture: producto={}, stock inicial={}, versión inicial={}, reservas=2 de 1 unidad", productId, savedProduct.getStock(), initialVersion);
        CountDownLatch productsLoaded = new CountDownLatch(2);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        Callable<Boolean> reserve = () -> {
            LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Hilo {} inicia transacción para producto {}", Thread.currentThread().getName(), productId);
            try {
                transactionTemplate.executeWithoutResult(transaction -> {
                    productRepository.findById(productId).orElseThrow();
                    productsLoaded.countDown();
                    LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Hilo {} cargó producto; espera a que ambas transacciones estén listas", Thread.currentThread().getName());
                    try {
                        if (!productsLoaded.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Both transactions must load the product");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Reservation interrupted", exception);
                    }
                    LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Hilo {} continúa; intenta reservar 1 unidad", Thread.currentThread().getName());
                    productService.reserveStock(productId, 1);
                });
                LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Hilo {} confirma la reserva", Thread.currentThread().getName());
                return true;
            } catch (ConcurrentModificationException exception) {
                LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Hilo {} revierte por conflicto optimista esperado", Thread.currentThread().getName());
                return false;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Coordina dos hilos para cargar la misma versión {}", initialVersion);
            Future<Boolean> firstReservation = executor.submit(reserve);
            Future<Boolean> secondReservation = executor.submit(reserve);

            boolean firstSucceeded = firstReservation.get(20, TimeUnit.SECONDS);
            boolean secondSucceeded = secondReservation.get(20, TimeUnit.SECONDS);
            LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Resultado: se esperaba exactamente un commit; obtenidos hilo1={}, hilo2={}", firstSucceeded, secondSucceeded);
            assertTrue(firstSucceeded ^ secondSucceeded);
            Product updatedProduct = productRepository.findById(productId).orElseThrow();
            LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Stock esperado=0, obtenido={}; versión esperada={}, obtenida={}", 0, initialVersion + 1, updatedProduct.getVersion());
            assertEquals(0, updatedProduct.getStock());
            assertEquals(initialVersion + 1, updatedProduct.getVersion());
        } finally {
            LOGGER.info("[concurrentReservationsAllowOnlyOneSuccess] Limpieza: detener hilos y eliminar producto {}", productId);
            executor.shutdownNow();
            executor.awaitTermination(20, TimeUnit.SECONDS);
            productRepository.deleteById(productId);
        }
    }

    @Test
    @DisplayName("El incremento retiene el bloqueo mientras la reserva obsoleta intenta guardar; solo el incremento confirma")
    void staleReservationFailsAfterCommittedIncrease() throws Exception {
        LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Inicio: incremento confirma y reserva obsoleta debe revertirse");
        Long productId = 1L;
        Product initialProduct = productRepository.findById(productId).orElseThrow();
        int initialStock = initialProduct.getStock();
        Long initialVersion = initialProduct.getVersion();
        int reservationQuantity = 2;
        int incrementQuantity = 5;
        LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Fixture: producto={}, stock inicial={}, versión inicial={}, reserva={}, incremento={}", productId, initialStock, initialVersion, reservationQuantity, incrementQuantity);
        LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Stock requerido para reserva: esperado al menos {}, obtenido={}", reservationQuantity, initialStock);
        assertTrue(initialStock >= reservationQuantity);

        CountDownLatch incrementLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseIncrement = new CountDownLatch(1);
        CountDownLatch staleEntityLoaded = new CountDownLatch(1);
        CountDownLatch reservationSaveEntered = new CountDownLatch(1);
        Long[] reservationVersionAtSave = new Long[1];
        AtomicInteger successfulCommits = new AtomicInteger();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Product> committedIncrease = null;
        Future<Boolean> staleReservation = null;

        try {
            doAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                if (productId.equals(product.getId())) {
                    reservationVersionAtSave[0] = product.getVersion();
                    reservationSaveEntered.countDown();
                }
                Product managedProduct = entityManager.merge(product);
                entityManager.flush();
                return managedProduct;
            }).when(productRepository).saveAndFlush(any(Product.class));

            committedIncrease = executor.submit(() -> {
                LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} inicia transacción de incremento", Thread.currentThread().getName());
                Product updatedProduct = transactionTemplate.execute(transaction -> {
                    Product increasedProduct = productService.increaseStock(productId, incrementQuantity);
                    incrementLockAcquired.countDown();
                    LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} mantiene la transacción de incremento abierta; espera liberación", Thread.currentThread().getName());
                    try {
                        if (!releaseIncrement.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Increment transaction was not released");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Increment transaction interrupted", exception);
                    }
                    return increasedProduct;
                });
                successfulCommits.incrementAndGet();
                LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} confirma incremento; stock devuelto={}, versión={}", Thread.currentThread().getName(), updatedProduct.getStock(), updatedProduct.getVersion());
                return updatedProduct;
            });

            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Espera a que el incremento adquiera el bloqueo");
            assertTrue(incrementLockAcquired.await(10, TimeUnit.SECONDS));
            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Incremento mantiene el bloqueo; inicia reserva con entidad obsoleta");
            staleReservation = executor.submit(() -> {
                LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} inicia transacción de reserva", Thread.currentThread().getName());
                try {
                    transactionTemplate.executeWithoutResult(transaction -> {
                        Product staleProduct = productRepository.findById(productId).orElseThrow();
                        LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Versión obsoleta esperada={}, obtenida={}", initialVersion, staleProduct.getVersion());
                        assertEquals(initialVersion, staleProduct.getVersion());
                        staleEntityLoaded.countDown();
                        LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} intenta reservar {}; puede bloquearse al guardar mientras el incremento conserva el bloqueo", Thread.currentThread().getName(), reservationQuantity);
                        productService.reserveStock(productId, reservationQuantity);
                    });
                    successfulCommits.incrementAndGet();
                    LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} confirma reserva inesperadamente", Thread.currentThread().getName());
                    return true;
                } catch (ConcurrentModificationException exception) {
                    LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Hilo {} revierte por conflicto optimista esperado", Thread.currentThread().getName());
                    return false;
                }
            });

            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Espera carga de entidad obsoleta y entrada al guardado");
            assertTrue(staleEntityLoaded.await(10, TimeUnit.SECONDS));
            assertTrue(reservationSaveEntered.await(10, TimeUnit.SECONDS));
            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Versión en guardado esperada={}, obtenida={}", initialVersion, reservationVersionAtSave[0]);
            assertEquals(initialVersion, reservationVersionAtSave[0]);

            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Libera la transacción de incremento para permitir el desenlace del guardado obsoleto");
            releaseIncrement.countDown();
            Product committedProduct = committedIncrease.get(10, TimeUnit.SECONDS);
            boolean staleReservationSucceeded = staleReservation.get(10, TimeUnit.SECONDS);
            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Resultado esperado: incremento confirmado y reserva rechazada; incremento stock={}, reserva confirmó={}", committedProduct.getStock(), staleReservationSucceeded);
            assertFalse(staleReservationSucceeded);
            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Commits esperados=1, obtenidos={}", successfulCommits.get());
            assertEquals(1, successfulCommits.get());

            Product finalProduct = productRepository.findById(productId).orElseThrow();
            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Estado final esperado: stock={}, versión={}; obtenido: stock={}, versión={}", initialStock + incrementQuantity, initialVersion + 1, finalProduct.getStock(), finalProduct.getVersion());
            assertEquals(initialStock + incrementQuantity, finalProduct.getStock());
            assertEquals(initialVersion + 1, finalProduct.getVersion());
        } finally {
            LOGGER.info("[staleReservationFailsAfterCommittedIncrease] Limpieza: libera bloqueo, termina hilos y restaura stock={}, versión={}", initialStock, initialVersion);
            releaseIncrement.countDown();
            executor.shutdown();
            boolean workersTerminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            if (!workersTerminated) {
                executor.shutdownNow();
                workersTerminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            assertTrue(workersTerminated);
            reset(productRepository);
            transactionTemplate.executeWithoutResult(transaction -> {
                entityManager.createNativeQuery(
                        "UPDATE products SET stock = :stock, version = :version WHERE id = :id")
                        .setParameter("stock", initialStock)
                        .setParameter("version", initialVersion)
                        .setParameter("id", productId)
                        .executeUpdate();
            });
        }
    }

    @Test
    @DisplayName("Reserva e incremento confirmados en transacciones separadas conservan ambos cambios")
    void reserveThenIncreaseStockInSeparateTransactions() {
        LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Inicio: reserva e incremento deben confirmar en transacciones separadas");
        Long productId = 1L;
        Product initialProduct = productRepository.findById(productId).orElseThrow();
        int initialStock = initialProduct.getStock();
        Long initialVersion = initialProduct.getVersion();
        int reservationQuantity = 3;
        int incrementQuantity = 8;
        LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Fixture: producto={}, stock inicial={}, versión inicial={}, reserva={}, incremento={}", productId, initialStock, initialVersion, reservationQuantity, incrementQuantity);
        LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Stock requerido para reserva: esperado al menos {}, obtenido={}", reservationQuantity, initialStock);
        assertTrue(initialStock >= reservationQuantity);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Ejecuta transacción de reserva");
            transactionTemplate.executeWithoutResult(
                    transaction -> productService.reserveStock(productId, reservationQuantity));
            LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Reserva confirmada; ejecuta transacción de incremento");
            transactionTemplate.executeWithoutResult(
                    transaction -> productService.increaseStock(productId, incrementQuantity));
            LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Incremento confirmado");

            Product finalProduct = productRepository.findById(productId).orElseThrow();
            int expectedStock = initialStock - reservationQuantity + incrementQuantity;
            LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Stock esperado={}, obtenido={}", expectedStock, finalProduct.getStock());
            assertEquals(expectedStock, finalProduct.getStock());
        } finally {
            LOGGER.info("[reserveThenIncreaseStockInSeparateTransactions] Limpieza: restaura stock={}, versión={}", initialStock, initialVersion);
            transactionTemplate.executeWithoutResult(transaction -> {
                entityManager.createNativeQuery(
                        "UPDATE products SET stock = :stock, version = :version WHERE id = :id")
                        .setParameter("stock", initialStock)
                        .setParameter("version", initialVersion)
                        .setParameter("id", productId)
                        .executeUpdate();
            });
        }
    }

    
    @Test
    @DisplayName("Test Consulta de productos en transacción de solo lectura")
    void searchProductsRunsInReadOnlyTransaction() {
        LOGGER.info("[searchProductsRunsInReadOnlyTransaction] Inicio: verificar consulta en transacción de solo lectura");
        Pageable pageable = PageRequest.of(0, 5);
        AtomicBoolean transactionActive = new AtomicBoolean();
        AtomicBoolean transactionReadOnly = new AtomicBoolean();
        AtomicBoolean sessionReadOnly = new AtomicBoolean();
        doAnswer(invocation -> {
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            transactionReadOnly.set(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            sessionReadOnly.set(entityManager.unwrap(Session.class).isDefaultReadOnly());
            LOGGER.info("[searchProductsRunsInReadOnlyTransaction] Repositorio invocado: transacción activa={}, solo lectura={}, sesión solo lectura={}", transactionActive.get(), transactionReadOnly.get(), sessionReadOnly.get());
            return Page.empty(pageable);
        }).when(productRepository).searchProducts("keyboard", pageable);

        LOGGER.info("[searchProductsRunsInReadOnlyTransaction] Ejecuta búsqueda 'keyboard' con página={}, tamaño={}", pageable.getPageNumber(), pageable.getPageSize());
        productService.searchProducts("keyboard", pageable);

        LOGGER.info("[searchProductsRunsInReadOnlyTransaction] Verifica que el repositorio recibió la búsqueda y la página solicitadas");
        verify(productRepository).searchProducts("keyboard", pageable);
        LOGGER.info("[searchProductsRunsInReadOnlyTransaction] Flags esperados=true; obtenidos transacción activa={}, transacción solo lectura={}, sesión solo lectura={}", transactionActive.get(), transactionReadOnly.get(), sessionReadOnly.get());
        assertAll(
                () -> assertTrue(transactionActive.get()),
                () -> assertTrue(transactionReadOnly.get()),
                () -> assertTrue(sessionReadOnly.get()));
    }

    @Test
    @DisplayName("Test de 3 threads concurrentes: solo 1 triunfa `increaseStock()` maneja `Pessimistic Lock`")
    void increaseStockLocksProductUntilTransactionCompletes() throws Exception {
        LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Inicio: tres transacciones compiten por bloqueo pesimista; solo la primera confirma");
        Product product = new Product();
        product.setName("Pessimistic lock test");
        product.setPrice(new BigDecimal("49.99"));
        product.setStock(10);
        Product savedProduct = productRepository.saveAndFlush(product);
        Long productId = savedProduct.getId();
        LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Fixture: producto={}, stock inicial={}, versión inicial={}, hilos=3, incremento por hilo=1", productId, savedProduct.getStock(), savedProduct.getVersion());
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch transactionsReady = new CountDownLatch(3);
        CountDownLatch startUpdates = new CountDownLatch(1);
        CountDownLatch firstUpdateFinished = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);
        AtomicInteger firstUpdate = new AtomicInteger(-1);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        List<Callable<Product>> operations = IntStream.range(0, 3).mapToObj(index -> (Callable<Product>) () ->
                transactionTemplate.execute(transaction -> {
                    LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo {} (índice {}) inicia transacción y espera señal de salida", Thread.currentThread().getName(), index);
                    transactionsReady.countDown();
                    try {
                        if (!startUpdates.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Updates were not started");
                        }
                        LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo {} solicita incremento y bloqueo de producto {}", Thread.currentThread().getName(), productId);
                        Product result = productService.increaseStock(productId, 1);
                        if (firstUpdate.compareAndSet(-1, index)) {
                            firstUpdateFinished.countDown();
                            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo {} conserva la transacción abierta tras el incremento; espera liberación", Thread.currentThread().getName());
                            if (!releaseFirstTransaction.await(20, TimeUnit.SECONDS)) {
                                throw new IllegalStateException("First transaction was not released");
                            }
                        }
                        LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo {} completa incremento dentro de su transacción", Thread.currentThread().getName());
                        return result;
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Test interrupted", exception);
                    }
                })).toList();
        try {
            List<Future<Product>> updates = operations.stream().map(executor::submit).toList();
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Espera que los tres hilos estén listos");
            assertTrue(transactionsReady.await(5, TimeUnit.SECONDS));
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Libera los tres hilos para competir por el bloqueo");
            startUpdates.countDown();
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Espera que el primer incremento termine y mantenga abierta su transacción");
            assertTrue(firstUpdateFinished.await(5, TimeUnit.SECONDS));
            int successfulIndex = firstUpdate.get();
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo ganador esperado con índice válido; índice obtenido={}", successfulIndex);
            for (int index = 0; index < updates.size(); index++) {
                if (index != successfulIndex) {
                    Future<Product> failedUpdate = updates.get(index);
                    LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo índice {} debe fallar por bloqueo; espera su resultado", index);
                    ExecutionException exception = assertThrows(ExecutionException.class,
                            () -> failedUpdate.get(10, TimeUnit.SECONDS));
                    LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Hilo índice {}: excepción esperada={}, obtenida={}", index, PessimisticLockingFailureException.class.getSimpleName(), exception.getCause().getClass().getSimpleName());
                    assertInstanceOf(PessimisticLockingFailureException.class, exception.getCause());
                }
            }
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Libera transacción ganadora para confirmar");
            releaseFirstTransaction.countDown();

            Product successfulProduct = updates.get(successfulIndex).get(10, TimeUnit.SECONDS);
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Incremento confirmado: id esperado={}, obtenido={}; stock esperado=11, obtenido={}", productId, successfulProduct.getId(), successfulProduct.getStock());
            assertEquals(productId, successfulProduct.getId());
            assertEquals(11, successfulProduct.getStock());
            int finalStock = productRepository.findById(productId).orElseThrow().getStock();
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Stock persistido esperado=11, obtenido={}", finalStock);
            assertEquals(11, finalStock);
        } finally {
            LOGGER.info("[increaseStockLocksProductUntilTransactionCompletes] Limpieza: libera hilos, detiene executor y elimina producto {}", productId);
            startUpdates.countDown();
            releaseFirstTransaction.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            productRepository.deleteById(productId);
        }
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
    @DisplayName("increaseStock rechaza cantidades inválidas sin consultar el repositorio")
    void increaseStockRejectsInvalidQuantitiesWithoutRepositoryLookup(Integer quantity) {
        clearInvocations(productRepository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.increaseStock(1L, quantity));

        assertEquals("La cantidad debe ser positiva", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
    @DisplayName("reserveStock rechaza cantidades inválidas sin consultar el repositorio")
    void reserveStockRejectsInvalidQuantitiesWithoutRepositoryLookup(Integer quantity) {
        clearInvocations(productRepository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.reserveStock(1L, quantity));

        assertEquals("La cantidad debe ser positiva", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("reserveStock rechaza una cantidad superior al stock sin modificar el producto")
    void reserveStockRejectsQuantityAboveAvailableStockWithoutModification() {
        Long productId = 1L;
        Product existingProduct = productRepository.findById(productId).orElseThrow();
        int initialStock = existingProduct.getStock();

        if (initialStock == Integer.MAX_VALUE) {
            Product fixtureProduct = new Product();
            fixtureProduct.setStock(0);
            ProductRepository fixtureRepository = mock(ProductRepository.class);
            when(fixtureRepository.findById(productId)).thenReturn(Optional.of(fixtureProduct));
            ProductService fixtureService = new ProductService(fixtureRepository);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> fixtureService.reserveStock(productId, 1));

            assertEquals("Stock insuficiente", exception.getMessage());
            assertEquals(0, fixtureProduct.getStock());
            verify(fixtureRepository).findById(productId);
            verify(fixtureRepository, never()).saveAndFlush(any(Product.class));
            verifyNoMoreInteractions(fixtureRepository);
            return;
        }

        assertTrue(initialStock >= 0);
        int quantity = initialStock + 1;
        clearInvocations(productRepository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productService.reserveStock(productId, quantity));

        assertEquals("Stock insuficiente", exception.getMessage());
        verify(productRepository, never()).saveAndFlush(any(Product.class));
        Product unchangedProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, unchangedProduct.getStock());
        assertEquals(existingProduct.getVersion(), unchangedProduct.getVersion());
    }

    

}