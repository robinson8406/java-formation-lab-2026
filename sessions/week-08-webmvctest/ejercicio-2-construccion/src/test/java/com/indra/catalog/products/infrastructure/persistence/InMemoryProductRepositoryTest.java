package com.indra.catalog.products.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.indra.catalog.products.domain.model.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InMemoryProductRepositoryTest {

    private final InMemoryProductRepository repository = new InMemoryProductRepository();
    private final Product laptop = new Product("PRD-0001", "Laptop", BigDecimal.TEN, 1);

    @Test
    void savedProductCanBeFoundAndDeleted() {
        repository.save(laptop);

        assertThat(repository.findById("PRD-0001")).contains(laptop);
        assertThat(repository.existsById("PRD-0001")).isTrue();

        repository.deleteById("PRD-0001");

        assertThat(repository.findById("PRD-0001")).isEmpty();
    }

    @Test
    void idGeneratorProducesSequentialIds() {
        SequentialProductIdGenerator generator = new SequentialProductIdGenerator();

        assertThat(generator.nextId()).isEqualTo("PRD-0001");
        assertThat(generator.nextId()).isEqualTo("PRD-0002");
    }
}
