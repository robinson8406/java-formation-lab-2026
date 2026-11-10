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


Para el uso de bloqueo optimista vs pesimista

Se sugiere usar el optimista cuando se pueda generar un error y sea factible controlar los errores de concurrencia 
como en el caso de reseva de stock ya que se es espera que el usuario que lo hace pueda esperar o hacer un reintento 
y este caso no es muy frecuente

aunque en al hacer un pago si ya escalaramos a un carrito de compras deberiamos pasar a bloqueo pesimista para evitar 
errores en el pago.

Por otro lado el bloqueo pesimista depende de la alta concurrencia que podramos tener y se espera en el sistema que 
varias transacciones accedan a incrementar o disminuir el stock con mucha frecuencia y los retrasos en esto tienen 
impactos en la operacion del negocio, o en extremo caso se deberá buscar una solucion diferente.