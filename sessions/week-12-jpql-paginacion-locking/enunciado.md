# Enunciado — Week 12: JPQL + Paginación + Locking

## Qué viene dado en `week-12-start`

El start llega con:
- `ProductRepository` con una query JPQL funcional pero **sin paginación** — carga todos los registros.
- Entidad `Product` con `@Version private Long version` ya declarado.
- `InventoryService.reserveStock()` implementado pero **sin manejo de `OptimisticLockException`** — el test de concurrencia falla.
- `ProductService.increaseStock()` implementado con una consulta por ID que aún no aplica bloqueo pesimista.
- Tests para paginación, concurrencia optimista y concurrencia pesimista.

Tu trabajo es hacer pasar los tests con cambios mínimos y quirúrgicos.

---

## Contexto del reto

**Indra Retail** tiene dos bugs: el endpoint de búsqueda carga 50.000 productos en memoria y cuando dos usuarios compran el último ítem simultáneamente ambas compras se aprueban.

## Lo que debes implementar

**Tarea 1 — Agregar `Pageable` a la query existente**

La query del start es:
```java
@Query("SELECT p FROM Product p WHERE " +
       "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
List<Product> searchProducts(@Param("name") String name);
```

Cámbiala para retornar `Page<Product>` y recibir `Pageable`:
```java
Page<Product> searchProducts(@Param("name") String name, Pageable pageable);
```

El test en rojo verifica: 50 productos en BD, búsqueda con `PageRequest.of(0, 10)` → `content.size() == 10` y `totalElements == 50`.

**Transacción de solo lectura — `searchProducts()`**

La anotación es una indicación para el gestor de transacciones y el proveedor JPA, no una garantía universal de que se prohíban escrituras. Con la propagación predeterminada, si el método participa en una transacción existente, prevalecen las características de esa transacción.

**Timeout de consulta — `searchProducts()`**

La configuración actual establece un timeout JPA de 3000 ms y un timeout de sesión H2 de 3000 ms. Para configurar una consulta concreta, agrega al método del repositorio un hint en milisegundos:
Verifica mediante una prueba que el timeout esperado llegue a las sentencias JDBC de contenido y conteo. Una consulta que termina antes del límite no demuestra que otra más lenta sea cancelada.

**Tarea 2 — Manejar `OptimisticLockException` en `reserveStock()`**

`reserveStock()` ya descuenta el stock. Solo falta envolver la llamada a `save()` para traducir la excepción:
```java
try {
    productRepository.save(product);
} catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
    throw new ConcurrentModificationException("Stock modificado concurrentemente, reintenta");
}
```

El test en rojo simula 2 threads comprando el último ítem: verifica que exactamente 1 lanza `ConcurrentModificationException`.

**Tarea 3 — Bloqueo pesimista en `increaseStock()`**

El test inicia 3 transacciones concurrentes sobre el mismo producto. La primera que obtiene el bloqueo lo conserva hasta que las otras 2 agotan la espera y fallan; después confirma un único incremento. El bloqueo se libera al terminar la transacción, por commit o rollback.

## Restricciones técnicas (para todos)

- La paginación debe ocurrir en la query SQL — no filtrar en Java.
- Sin cambiar `FetchType.EAGER` como solución al test de concurrencia.
- **Criterio no funcional (performance)**: la query paginada no debe cargar más registros que `page.size`.

## Criterio de aceptación del PR

- [ ] Test de paginación en verde (`content.size == 10`, `totalElements == 50`)
- [ ] Test de `searchProducts()` verifica la transacción y la sesión de solo lectura.
- [ ] Test del timeout de `searchProducts()` comprueba el valor aplicado a contenido y conteo.
- [ ] Test de concurrencia en verde (1 de 2 threads lanza `ConcurrentModificationException`)
- [ ] Test `increaseStock()` usa bloqueo pesimista y el test de 3 transacciones confirma un único incremento.
- [ ] Sin `FetchType.EAGER` añadido
- [ ] `mvn verify` en verde

## Bonus (opcional)

- Agregar los filtros opcionales `minPrice` y `maxPrice` a la query paginada.
- Reemplazar la query JPQL por `Specification<Product>` con `JpaSpecificationExecutor`.
