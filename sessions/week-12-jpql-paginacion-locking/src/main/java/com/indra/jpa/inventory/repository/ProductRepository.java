package com.indra.jpa.inventory.repository;

import com.indra.jpa.inventory.model.Product;
import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    default Page<Product> searchProducts(Specification<Product> spec, Pageable pageable) {
        return findAll(spec, pageable);
    }

    @Override
    @QueryHints(value = @QueryHint(name = "jakarta.persistence.query.timeout", value = "5000"))
    Page<Product> findAll(Specification<Product> spec, @Nonnull Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

}
