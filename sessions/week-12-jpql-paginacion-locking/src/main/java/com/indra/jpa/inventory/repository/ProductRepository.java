package com.indra.jpa.inventory.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.indra.jpa.inventory.model.Product;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ProductRepository extends JpaRepository<Product, Long>,
                                         JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p WHERE "
            + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    @QueryHints(@QueryHint(name = "jakarta.persistence.query.timeout", value = "5000"))
    Page<Product> searchProducts(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE "
        + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))"
        + "AND p.price BETWEEN :minPrice AND :maxPrice")
    @QueryHints(@QueryHint(name = "jakarta.persistence.query.timeout", value = "5000"))
    Page<Product> searchProductsMinMax(@Param("name") String name,
        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Specification<Product> searchById(@Param("id") Long id);

}