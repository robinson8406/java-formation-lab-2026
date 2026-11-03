package com.indra.jpa.orders.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
	
    @EntityGraph(attributePaths = {"items", "items.product"})
	Page<Order> findByStatus(OrderStatus status, Pageable pageable);

	@Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.status = :status")
	List<Order> findByStatusWithItems(OrderStatus status);
	
	@EntityGraph(attributePaths = {"items", "items.product"})
	List<Order> findOrdersByStatus(OrderStatus status);
}