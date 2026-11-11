package com.indra.jpa.orders.repository;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.status = :status")
	List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

	@EntityGraph(attributePaths = { "items", "items.product" })
	List<Order> findWithGraphByStatus(OrderStatus status);

	@Query(value = "SELECT o.id FROM Order o WHERE o.status = :status ORDER BY o.id",
			countQuery = "SELECT COUNT(o) FROM Order o WHERE o.status = :status")
	Page<Long> findIdsByStatus(@Param("status") OrderStatus status, Pageable pageable);

	@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.id IN :ids")
	List<Order> findByIdsWithItems(@Param("ids") List<Long> ids);

}