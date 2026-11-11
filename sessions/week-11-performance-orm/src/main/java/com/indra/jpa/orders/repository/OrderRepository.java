package com.indra.jpa.orders.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.status = :status")
	List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

	// Paginación en dos pasos: 1) página de ids (LIMIT real en SQL)
	@Query(value = "SELECT o.id FROM Order o WHERE o.status = :status ORDER BY o.id",
			countQuery = "SELECT COUNT(o) FROM Order o WHERE o.status = :status")
	Page<Long> findIdsByStatus(@Param("status") OrderStatus status, Pageable pageable);

	// 2) carga con JOIN FETCH solo las órdenes de esa página
	@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.id IN :ids")
	List<Order> findByIdsWithItems(@Param("ids") List<Long> ids);

}