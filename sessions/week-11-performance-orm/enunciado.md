# Enunciado — Week 11: Performance ORM (N+1)

## Contexto del reto

**Indra Retail** tiene un endpoint `GET /api/orders` que lista las órdenes con sus ítems y el nombre del producto. Con 100 órdenes en la base de datos, el endpoint ejecuta **401 queries SQL** (1 para las órdenes + 100 para los ítems + 300 para los productos). El SLA exige respuesta en <200ms; actualmente tarda ~3 segundos.

## Lo que debes implementar

1. Activa el log de SQL en `application-dev.properties`:
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   logging.level.org.hibernate.stat=DEBUG
   spring.jpa.properties.hibernate.generate_statistics=true
   ```
2. Ejecuta el test de carga provisto en `week-11-start` y cuenta las queries (están logueadas).
3. Corrige el N+1 en `OrderRepository` usando **JOIN FETCH** en la query JPQL:
   ```java
   @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.status = :status")
   List<Order> findByStatusWithItems(@Param("status") OrderStatus status);
   ```
4. Verifica que el número de queries baja a 1.
5. Escribe un test que afirme que cargar 50 órdenes genera **máximo 3 queries** (usando `StatisticsService` de Hibernate o `@Sql` con dataset fijo).

## Restricciones técnicas (para todos)

- No cambiar `FetchType.EAGER` como solución — está prohibido.
- La solución debe funcionar para listados paginados también.
- **Criterio no funcional (performance)**: el test debe demostrar con un assertion el número de queries, no solo "funciona".

## Criterio de aceptación del PR

- [ ] Conteo de queries antes del fix documentado en el PR (comment o commit message)
- [ ] `JOIN FETCH` implementado en `OrderRepository`
- [ ] Conteo de queries después: ≤ 3 para 50 órdenes
- [ ] Test con assertion del número de queries
- [ ] Sin `FetchType.EAGER` añadido
- [ ] `mvn verify` en verde

## Bonus (opcional)

- Implementar la misma optimización con `@EntityGraph` como alternativa al `JOIN FETCH`.
Se realiza con `@EntityGraph(attributePaths = {"items", "items.product"})` en cambio del query y da 1 querie para traer la lista sin paginación igual que con `JOIN FETCH`
- Configurar `hibernate.default_batch_fetch_size=25` y comparar el impacto.
El numero de queries no varió pero se redujo el tiempo de construcción del build de 22seg a 17seg