package com.indra.jpa.orders.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.items i
            JOIN FETCH i.product
            WHERE o.status = :status
            """)
	List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

	@Query(
			value = """
                    SELECT o.id
                    FROM Order o
                    WHERE o.status = :status
                    ORDER BY o.id
                    """,
			countQuery = """
                    SELECT COUNT(o)
                    FROM Order o
                    WHERE o.status = :status
                    """
	)
	Page<Long> findIdsByStatus(
			@Param("status") OrderStatus status,
			Pageable pageable
	);

	@Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.items i
            JOIN FETCH i.product
            WHERE o.id IN :ids
            """)
	List<Order> findByIdInWithItems(@Param("ids") Collection<Long> ids);

	List<Order> findByStatus(OrderStatus status);
}