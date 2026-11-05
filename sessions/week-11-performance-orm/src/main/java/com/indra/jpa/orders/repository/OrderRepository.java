package com.indra.jpa.orders.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.indra.jpa.orders.model.Order;
import com.indra.jpa.orders.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Versión con N+1: items y product se cargan LAZY al recorrer el resultado.
	// TODO (reto): agrega findByStatusWithItems con JOIN FETCH o @EntityGraph.
	// Útil como alternativa batch-fetch para consultas que necesitan cargar las relaciones LAZY.
	List<Order> findByStatus(OrderStatus status);

	@Query("""
			select distinct o
			from Order o
			left join fetch o.items i
			left join fetch i.product
			where o.status = :status
			""")
	List<Order> findByStatusWithItems(@Param("status") OrderStatus status);

	@EntityGraph(attributePaths = { "items", "items.product" })
	List<Order> findByStatusOrderByIdAsc(OrderStatus status);

	@Query("select o.id from Order o where o.status = :status")
	Page<Long> findIdsByStatus(@Param("status") OrderStatus status, Pageable pageable);

	@Query("""
			select distinct o
			from Order o
			left join fetch o.items i
			left join fetch i.product
			where o.id in :ids
			""")
	List<Order> findAllWithItemsByIdIn(@Param("ids") List<Long> ids);
}