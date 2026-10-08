package com.indra.catalog.products.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indra.catalog.products.domain.exception.ProductNotFoundException;
import com.indra.catalog.products.domain.model.Product;
import com.indra.catalog.products.domain.port.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unitario puro (sin Spring): verifica la asignación de id y el DIP sobre {@link ProductRepository}. */
class ProductServiceImplTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository);
    }

    @Test
    void create_asignaUnIdSecuencialYPersisteElProducto() {
        Product toRegister = Product.register("Laptop", new BigDecimal("3500000.00"), 10);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = service.create(toRegister);

        assertThat(created.id()).isEqualTo("PRD-0001");
        verify(repository).save(created);
    }

    @Test
    void findById_conProductoExistente_loDevuelve() {
        Product product = new Product("PRD-0001", "Laptop", BigDecimal.TEN, 5);
        when(repository.findById("PRD-0001")).thenReturn(Optional.of(product));

        assertThat(service.findById("PRD-0001")).isEqualTo(product);
    }

    @Test
    void findById_conProductoInexistente_lanzaProductNotFoundException() {
        when(repository.findById("PRD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void delete_conProductoInexistente_lanzaProductNotFoundException() {
        when(repository.existsById("PRD-9999")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
