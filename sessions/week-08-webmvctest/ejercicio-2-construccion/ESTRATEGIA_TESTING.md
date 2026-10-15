# Estrategia de testing — Catálogo de productos

> Completa esta plantilla. Es el entregable que diferencia el nivel **Experto**: no basta con que los
> tests pasen, hay que justificar **qué se prueba dónde y por qué**.

## 1. Pirámide propuesta

| Nivel | Herramienta | Qué valida | Qué NO valida | Nº de tests | Tiempo aprox. |
|-------|-------------|------------|---------------|-------------|---------------|
| Unitario (dominio) | JUnit 5 + AssertJ | Invariantes de `Product` (nombre, precio > 0, stock ≥ 0, límites 120/121) | HTTP, seguridad, persistencia | 12 | ~0,06 s |
| Unitario (aplicación) | JUnit 5 + Mockito (`@Mock`) | Orquestación: generación de id, 404 al buscar/borrar, no persistir inválidos | Serialización JSON, reglas de seguridad | 6 (+4 repositorio, +2 mapper) | ~0,8 s (init de Mockito) |
| Slice web | `@WebMvcTest` + `MockMvc` + `@Import(SecurityConfig)` | Traducción HTTP: status, `Content-Type`, `Location`, JSON, validación 400, mapeo de excepciones, 401/403 con MIS reglas | Lógica de negocio real (servicio mockeado), cableado de beans | 12 | ~2,7 s aislado / ~0,6 s en suite |
| Integración | `@SpringBootTest` + `@AutoConfigureMockMvc` | Que las capas reales quedan cableadas y los usuarios reales autentican | Casos de borde (ya cubiertos abajo) | 1 | ~2,1 s |

## 2. Matriz de decisión

| Regla / comportamiento | Dominio | Aplicación | Slice web | Integración | Justificación |
|------------------------|:-------:|:----------:|:---------:|:-----------:|---------------|
| El precio debe ser > 0 | ✅ | | ✅ (400) | | Es invariante del dominio; el slice solo verifica la traducción a 400 de Bean Validation |
| El nombre vacío devuelve 400 | ✅ | | ✅ | | "Devuelve 400" es un contrato HTTP → slice. El dominio protege igual si llega por otro canal |
| Producto inexistente devuelve 404 | | ✅ (lanza excepción) | ✅ (404) | ✅ | Aplicación decide "no existe"; web decide que eso es 404 |
| Se genera un id al crear | | ✅ | | ✅ | Responsabilidad del caso de uso + formato en test del repositorio |
| `Location` apunta al recurso creado | | | ✅ | | Puramente HTTP |
| `DELETE` exige rol `ADMIN` | | | ✅ (403/401) | | Regla de la `SecurityFilterChain`; requiere importarla en el slice |
| Las capas quedan cableadas | | | | ✅ | Único lugar donde no hay mocks |

## 3. Decisiones y trade-offs

- **`@MockBean` vs `@Mock`**: el controller recibe el servicio por inyección de Spring, así que el doble debe ser un bean del contexto (`@MockBean`). El servicio es Java plano: `@Mock` + `@InjectMocks` sin contexto es órdenes de magnitud más rápido.
- **Seguridad en el slice**: `@WebMvcTest` no escanea clases `@Configuration`; sin `@Import(SecurityConfig.class)` aplica la seguridad por defecto de Boot. Experimento: al quitar el `@Import` fallan 11/12 tests (GET → 401, POST/DELETE → 403 por CSRF). Se usa `@WithMockUser` para probar autorización y `httpBasic(...)` para probar autenticación real.
- **Tiempo del pipeline si cada test de controller usa `@SpringBootTest`** (medido en local, con `-XX:TieredStopAtLevel=1`):

  | Escenario | `@SpringBootTest` | `@WebMvcTest` |
  |-----------|-------------------|---------------|
  | `ProductControllerTest` (12 tests, aislado, 3 ejecuciones) | 3,17 / 2,98 / 3,01 s | 2,86 / 2,89 / 2,82 s |
  | 3 clases de controller que difieren en un `@MockBean` (3 contextos) | 4,59 / 4,26 s | 4,04 / 4,21 s |

  **Conclusión honesta:** en este módulo el slice es solo ~5–10 % más rápido, porque la aplicación completa casi no
  tiene beans adicionales (sin base de datos, JPA, Flyway ni clientes externos). La diferencia crece con el tamaño del
  contexto: cada `@SpringBootTest` levanta *todo* (datasource, migraciones, integraciones), mientras que el slice
  sigue cargando solo la capa MVC. Además, con `@SpringBootTest` el `@MockBean` se aplica sobre el contexto completo,
  así que cada combinación distinta de mocks multiplica el número de contextos pesados que hay que levantar. Esa es la
  causa típica de los "4 minutos" del pipeline del equipo.

  Más allá del tiempo, el slice aporta **aislamiento**: un test de controller no falla por un problema de
  configuración de base de datos o de otra capa, y obliga a que el controller dependa de abstracciones mockeables.
- **Caché de contexto**: todos los slice tests comparten anotaciones/perfil `test`; se evita `@DirtiesContext`. Lo invalidan distintos `@MockBean`, perfiles o propiedades.
- **Riesgos no cubiertos**: contrato con consumidores (→ contract tests con Spring Cloud Contract/Pact), carga y concurrencia del repositorio (→ Gatling/JMeter), formato del body en 401/403 (lo genera Security, no el `ApiExceptionHandler`), persistencia real (llegará con JPA → `@DataJpaTest`).

## 4. Mediciones

| Comando | Tests | Tiempo reportado por Surefire |
|---------|-------|-------------------------------|
| `mvn -pl ejercicio-2-construccion test -Dtest='ProductControllerTest*'` | 12 | 2.603 s |
| `mvn -pl ejercicio-2-construccion verify` | 37 |  6.078 s|
