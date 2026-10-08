# Estrategia de testing — Catálogo de productos

## 1. Pirámide propuesta

| Nivel | Herramienta | Qué valida | Qué NO valida | Nº de tests | Tiempo aprox. |
|-------|-------------|------------|---------------|-------------|---------------|
| Unitario (dominio) | JUnit 5 + AssertJ | Invariantes de `Product` (nombre, precio > 0, stock ≥ 0) y el ensamblado vía `register`/`withId` | Persistencia, HTTP, seguridad | 5 (`ProductTest`) | ~0.02 s |
| Unitario (aplicación) | JUnit 5 + Mockito | Que `ProductServiceImpl` asigna el id correcto y traduce "no existe" a `ProductNotFoundException`, con el repositorio mockeado (DIP) | Formato HTTP, serialización JSON, seguridad | 4 (`ProductServiceImplTest`) | ~1.7 s (arranque de JVM/Mockito, sin contexto Spring) |
| Slice web | `@WebMvcTest` + `MockMvc` + `@MockBean` | Traducción HTTP↔dominio: status codes, `jsonPath` del body, header `Location`, `Content-Type` y las reglas de autorización (`401`/`403`) importando `SecurityConfig` | Persistencia real, lógica de negocio del servicio (está mockeado) | 7 (`ProductControllerTest`) | ~2 s (medido con Surefire, ver sección 4) |
| Integración | `@SpringBootTest` + `MockMvc` | Que las 4 capas quedan cableadas correctamente con el contenedor de Spring Security real | Casos de borde de validación o reglas de negocio (ya cubiertos abajo) | 2 (`ProductCatalogApplicationSmokeTest`: `contextLoads` + 1 flujo feliz de creación) | ~5 s |

## 2. Matriz de decisión

| Regla / comportamiento | Dominio | Aplicación | Slice web | Integración | Justificación |
|------------------------|:-------:|:----------:|:---------:|:-----------:|---------------|
| El precio debe ser > 0 | ✔ | | | | Es una invariante del propio `Product`; no depende de Spring ni de HTTP, así que el test más barato y específico es el unitario de dominio. |
| El nombre vacío devuelve 400 | | | ✔ | | Es una traducción HTTP (Bean Validation → `MethodArgumentNotValidException` → 400), responsabilidad exclusiva del slice web. |
| Producto inexistente devuelve 404 | | ✔ (lanza la excepción) | ✔ (la traduce a 404) | | El servicio decide *cuándo* lanzar `ProductNotFoundException` (unitario, con repo mockeado); el controller decide *cómo* se ve eso en HTTP (slice). Cada capa prueba su propia responsabilidad. |
| Se genera un id al crear | | ✔ | | | Es lógica de aplicación (`AtomicLong` + formato); un unitario con repositorio mockeado es más rápido y preciso que probarlo a través de MockMvc. |
| `Location` apunta al recurso creado | | | ✔ | | Depende de `ServletUriComponentsBuilder`, que solo existe en el contexto de una petición HTTP real (o simulada). Solo el slice web puede verificarlo. |
| `DELETE` exige rol `ADMIN` | | | ✔ | | La autorización por rol es configuración de Spring Security (`SecurityFilterChain`); se verifica importando `SecurityConfig` en el slice, sin necesidad de levantar toda la aplicación. |
| Las capas quedan cableadas | | | | ✔ | Ningún nivel más bajo detecta un error de `@Autowired`/`@Bean` faltante entre capas reales; para eso existe el único `@SpringBootTest` (smoke test). |

## 3. Decisiones y trade-offs

- **`@MockBean` en el slice vs. `@Mock` en el test del servicio**: `@MockBean` reemplaza un bean dentro del
  `ApplicationContext` de Spring (necesario en `@WebMvcTest`, porque el controller se resuelve vía
  inyección de dependencias real). `@Mock`/`Mockito.mock(...)` puro crea el doble sin levantar ningún
  contenedor, que es todo lo que necesita `ProductServiceImplTest` porque instancia `ProductServiceImpl`
  directamente con `new ProductServiceImpl(repository)`. Usar `@MockBean` ahí sería pagar el costo de
  arrancar Spring sin ningún beneficio.
- **Costo de `@SpringBootTest` por cada test de controller**: el único `@SpringBootTest` de este ejercicio
  (smoke test) tarda solo él ~5 s en levantar el contexto (ver sección 4), más que los 7 tests de
  `@WebMvcTest` juntos (~2 s). Si cada uno de los 7 casos del controller usara `@SpringBootTest`, el costo
  se multiplicaría porque Spring no reutiliza contexto entre configuraciones distintas. Con ~200
  participantes ejecutando esto cada semana en el pipeline, la diferencia entre segundos y minutos decide
  si el equipo corre los tests en cada commit o los evita.
- **Caché de contexto de Spring entre clases de test**: Spring Test cachea el `ApplicationContext` usando
  como clave la combinación exacta de configuración (`@WebMvcTest(X.class)`, `@MockBean`s, `@Import`,
  propiedades, perfiles). Mientras `ProductControllerTest` sea la única clase con esa combinación no hay
  nada que reutilizar entre clases, pero si se agregaran más slices sobre el mismo controller con la misma
  configuración compartirían el mismo contexto cacheado. Lo que invalida el caché: cambiar los `@MockBean`
  declarados, el conjunto de `@Import`, los `@TestPropertySource` o el perfil activo.
- **Riesgos no cubiertos**: (1) no hay *contract tests* entre `product-catalog` y un eventual consumidor
  del API (p. ej. con Pact), por lo que un cambio de forma en `ProductResponse` solo se detectaría
  manualmente; (2) no hay tests de carga/concurrencia sobre `InMemoryProductRepository` ni sobre el
  `AtomicLong` de `ProductServiceImpl` bajo alta concurrencia real; (3) no se prueba el comportamiento
  cuando `EDITOR_PASSWORD`/`ADMIN_PASSWORD` faltan en el entorno — hoy caen en los valores por defecto del
  `application.properties`, aceptable en dev pero que debería alertarse en un chequeo de arranque antes de
  producción.

## 4. Mediciones

| Comando | Tests | Tiempo reportado por Surefire |
|---------|-------|-------------------------------|
| `mvn -pl ejercicio-2-construccion test -Dtest='ProductControllerTest*'` | 7 | **2.698 s** (medición limpia con configuración explícita del slice; el contexto se limita a `ProductController` y `ApiExceptionHandler`) |
| `mvn -pl ejercicio-2-construccion verify` | 18 (`ProductServiceImplTest` 4 + `ProductTest` 5 + `ProductCatalogApplicationSmokeTest` 2 + `ProductControllerTest` 7) | **~16 s** (incluye arrancar 2 contextos Spring distintos: el smoke test y el slice) |

