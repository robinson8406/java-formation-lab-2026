package com.indra.jpa.orders.repository;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.status = :status")
	List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

	@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.status = :status")
	List<Order> findByStatus(@Param("status") OrderStatus status);

	// Pagina los pedidos primero; el servicio carga sus asociaciones para esos IDs.
	@Query("SELECT o FROM Order o WHERE o.status = :status")
	Page<Order> findPageByStatus(@Param("status") OrderStatus status, Pageable pageable);

	@Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id IN :orderIds")
	List<Order> findByIdInWithItemsAndProducts(@Param("orderIds") List<Long> orderIds);
}