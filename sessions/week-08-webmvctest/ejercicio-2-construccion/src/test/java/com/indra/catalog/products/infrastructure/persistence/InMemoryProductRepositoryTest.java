package com.indra.catalog.products.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.indra.catalog.products.domain.model.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InMemoryProductRepositoryTest {

    private final InMemoryProductRepository repository = new InMemoryProductRepository();

    private static Product laptop() {
        return new Product("PRD-0001", "Laptop", new BigDecimal("3500000.00"), 10);
    }

    @Test
    void nextId_generatesSequentialIdsWithContractFormat() {
        assertThat(repository.nextId()).isEqualTo("PRD-0001");
        assertThat(repository.nextId()).isEqualTo("PRD-0002");
    }

    @Test
    void save_thenFindById_returnsProduct() {
        repository.save(laptop());

        assertThat(repository.findById("PRD-0001")).contains(laptop());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        assertThat(repository.findById("PRD-9999")).isEmpty();
    }

    @Test
    void deleteById_returnsTrueOnlyWhenProductExisted() {
        repository.save(laptop());

        assertThat(repository.deleteById("PRD-0001")).isTrue();
        assertThat(repository.deleteById("PRD-0001")).isFalse();
        assertThat(repository.findById("PRD-0001")).isEmpty();
    }
}
