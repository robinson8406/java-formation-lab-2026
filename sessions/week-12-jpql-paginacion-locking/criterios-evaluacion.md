# Criterios de evaluación — Week 12

## Checklist del reviewer

### Paginación
- [ ] Query JPQL con `Pageable` en `ProductRepository`
- [ ] Filtros opcionales funcionan (null → sin filtro)
- [ ] Test verifica `totalElements` y `content.size() <= pageSize`
- [ ] Sin carga de todos los registros en memoria

### Transacción de solo lectura
- [ ] `ProductService.searchProducts()` Un test verifica una transacción activa de solo lectura y la sesión Hibernate de solo lectura.

### Timeout de consulta
- [ ] Se identifica el timeout general configurado en milisegundos y aplica una solucion de time out para productRepository `searchProducts()`
- [ ] Se distingue el timeout de consulta del tiempo de 5 segundos.

### Locking optimista
- [ ] `@Version` en entidad `Product`
- [ ] `reserveStock()` maneja `OptimisticLockException`
- [ ] Test de 2 threads concurrentes: solo 1 triunfa

### Locking pesimista
- [ ] Aplicar logica Locking pesimista a `increaseStock()`
- [ ] Test de 3 transacciones concurrentes: una confirma el incremento y las otras 2 fallan al agotar la espera del bloqueo

### Calidad
- [ ] `PESSIMISTIC_WRITE` se limita al caso de reposición; `reserveStock()` mantiene locking optimista
- [ ] `mvn verify` en verde

## Escala de madurez

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Paginación funcional, @Version presente | Test de concurrencia básico | Specification, @QueryHints | Propone estrategia de locking para el dominio completo (cuándo optimista vs pesimista) |
