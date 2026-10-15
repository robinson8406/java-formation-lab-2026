# Estrategia de testing — Catálogo de productos

> Cada regla se prueba en el nivel **más bajo** que la puede comprobar. Los niveles altos solo prueban lo que
> los bajos no pueden ver: el contrato HTTP (slice) y el cableado de las capas (integración).

## 1. Pirámide propuesta

| Nivel | Herramienta | Qué valida | Qué NO valida | Nº de tests | Tiempo aprox. |
|-------|-------------|------------|---------------|-------------|---------------|
| Unitario (dominio) | JUnit 5 | Invariantes de `Product`: nombre obligatorio, precio > 0, stock ≥ 0, nombre recortado | HTTP, Spring, persistencia | 5 | < 0,1 s |
| Unitario (aplicación) | JUnit 5 + Mockito | Casos de uso: id generado al crear, 404 lógico si no existe, no borra lo que no existe | Validación de entrada HTTP, serialización JSON | 4 | ≈ 1,4 s (arranque de Mockito) |
| Unitario (infraestructura) | JUnit 5 | Adaptador en memoria y generador secuencial de ids | Concurrencia bajo carga | 2 | < 0,1 s |
| Slice web | `@WebMvcTest` + `MockMvc` + `@Import(SecurityConfig.class)` | Rutas, códigos HTTP, JSON (`jsonPath`), `Content-Type`, `Location`, validación (`400`), mapeo de errores (`404`), seguridad (`401`/`403`) | Reglas de negocio (el servicio es un `@MockBean`) | 8 | ≈ 1,0 s en `verify`; ≈ 4,4 s aislado en frío |
| Integración | `@SpringBootTest` + `@AutoConfigureMockMvc` | Que web → aplicación → dominio → infraestructura quedan cableados con la seguridad real | Casos de borde (ya cubiertos abajo) | 1 | ≈ 3,3 s |

## 2. Matriz de decisión

| Regla / comportamiento | Dominio | Aplicación | Slice web | Integración | Justificación |
|------------------------|:-------:|:----------:|:---------:|:-----------:|---------------|
| El precio debe ser > 0 | ✅ | | ✅ (400) | | La invariante vive en `Product` y se prueba ahí. El slice solo comprueba que un request inválido se traduce a `400` antes de llegar al servicio. |
| El nombre vacío devuelve 400 | | | ✅ | | Es contrato HTTP: Bean Validation + `ApiExceptionHandler`. Solo el slice ve el `400` y el formato del error. |
| Producto inexistente devuelve 404 | | ✅ | ✅ | | El servicio decide que no existe (lanza `ProductNotFoundException`); el slice comprueba que esa excepción se traduce a `404 PRODUCT_NOT_FOUND`. |
| Se genera un id al crear | | ✅ | | | Es una regla del caso de uso; se prueba con un `ProductIdGenerator` mockeado, sin Spring. |
| `Location` apunta al recurso creado | | | ✅ | | Depende de la URL de la petición: solo existe en la capa web. |
| `DELETE` exige rol `ADMIN` | | | ✅ | | La regla está en `SecurityConfig`. El slice la prueba importando esa configuración (si no, aplica la seguridad por defecto). |
| Las capas quedan cableadas | | | | ✅ | Ningún nivel inferior arranca todos los beans reales juntos. Basta un recorrido feliz crear → leer → borrar → 404. |

## 3. Decisiones y trade-offs

- **`@MockBean` vs `@Mock`.** En el slice, el controller lo crea Spring, así que el doble del servicio tiene que
  estar **dentro del contexto** para inyectarse: eso es `@MockBean`. En el test del servicio no hay Spring; se
  construye el objeto a mano y basta un mock de Mockito puro (`@Mock`), que no arranca nada y es instantáneo.
- **Por qué `@Import(SecurityConfig.class)`.** Con `spring-boot-starter-security` en el classpath, `@WebMvcTest`
  activa Spring Security, pero `SecurityConfig` es una `@Configuration` normal y el slice no la escanea. Sin el
  `@Import` el test usa la configuración por defecto (todo autenticado, CSRF activo): el `GET` público da `401`
  y el `POST` da `403`. Importarla hace que el test pruebe **nuestras** reglas.
- **Coste de `@SpringBootTest` por cada test de controller.** El smoke test tarda ≈ 3,3 s solo en arrancar el
  contexto completo. Si cada clase de test de controller lo usara con una configuración distinta, cada una pagaría
  ese arranque; con decenas de controllers eso explica los 4 minutos del pipeline actual.
- **Caché de contexto.** Spring reutiliza el contexto entre clases de test con la **misma** configuración. Lo
  invalidan: un conjunto distinto de `@MockBean`, `@Import`, propiedades o perfiles distintos, y `@DirtiesContext`.
  Por eso todos los casos de `ProductController` viven en una sola clase (con `@Nested`): un arranque, 8 tests.
- **Medición del criterio < 3 s.** Dentro de `mvn verify` la clase tarda ≈ 1,0 s. Aislada y en frío tarda ≈ 4,4 s
  en el portátil del formador (Windows): casi todo es el arranque de la JVM y del contexto del slice. El criterio se
  evalúa en el agente del pipeline (`ubuntu-latest`) o dentro de `mvn verify`.
- **Riesgos sin cubrir.** Contrato con consumidores (cubrir con contract tests, p. ej. Spring Cloud Contract o Pact);
  concurrencia del repositorio en memoria y del generador de ids bajo carga (tests de carga); persistencia real
  (cuando exista base de datos, `@DataJpaTest` o Testcontainers).

## 4. Mediciones

Portátil del formador, Windows, JDK 21.

| Comando | Tests | Tiempo reportado por Surefire |
|---------|-------|-------------------------------|
| `mvn -pl ejercicio-2-construccion test -Dtest='ProductControllerTest*'` | 8 | ≈ 4,4 s (aislado, arranque en frío) |
| `mvn -pl ejercicio-2-construccion verify` (dentro de `mvn verify` del agregador) | 20 | `ProductControllerTest` ≈ 1,0 s · smoke ≈ 3,3 s · módulo ≈ 10,5 s |
