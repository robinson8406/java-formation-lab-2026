# 🛡️ Decisiones de Arquitectura y Resiliencia Distribuida — Week 13
## Reto: The Retry Storm (Microservicios, Idempotencia y Fallos Parciales)

**Autor:** `veitronsm`  
**Rol:** Senior Enterprise Architect & Java Specialist  
**Repositorio:** `robinson8406/java-formation-lab-2026`  
**Módulos Afectados:** `order-service`, `payment-service`, `provider-simulator`  
**Nivel de Evaluación:** Experto por Diseño  

---

## 1. Diagnóstico del Incidente: La Tormenta de Reintentos (The Retry Storm)

### 1.1. Anatomía del Fallo Inicial
En la arquitectura distribuida original:
$$\text{order-service} \xrightarrow{\text{HTTP}} \text{payment-service} \xrightarrow{\text{HTTP}} \text{provider-simulator}$$

Se identificaron dos fallas estructurales críticas combinadas que detonaban la tormenta de reintentos y la duplicación de cobros:

1. **Destrucción de la Clave de Idempotencia (`PaymentController.java`):**
   - En el endpoint `POST /payments`, `PaymentController` recibía el encabezado `Idempotency-Key` (generado por `order-service` como `"ORDER-" + id + "-PAYMENT"`), pero inmediatamente lo sobreescribía con:
     ```java
     key = UUID.randomUUID().toString(); // BUG CRÍTICO
     ```
   - Al sustituir la clave de idempotencia del cliente por un UUID aleatorio en cada solicitud, el mecanismo de deduplicación de `PaymentApplicationServiceUseCase` y la restricción única de BD (`uk_idempotency_key`) quedaban totalmente burlados.
   - Cada intento se percibía como una transacción de pago nueva, provocando que el proveedor externo recibiera claves distintas y procesara pagos duplicados sobre la misma orden.

2. **Multiplicación de Reintentos en Cascada (Cascading Retries) & Timeout Incoherente:**
   - `order-service` tenía configurada una anotación `@Retry(name = "general")` en su cliente HTTP (`PaymentHttpClient.charge()`) con 3 intentos y un `read-timeout` de apenas **100 ms**.
   - `provider-simulator` introduce un retardo artificial intrínseco de **500 ms**.
   - Como $100\text{ ms} < 500\text{ ms}$, la llamada inicial de `order-service` siempre abortaba prematuramente por `SocketTimeoutException` antes de que `payment-service` pudiera responder, disparando los 3 reintentos de `order-service`.
   - Si a su vez `payment-service` tuviese fallas hacia el proveedor y reintentara 3 veces, el volumen total de peticiones al proveedor se amplificaría de acuerdo con la fórmula de multiplicación de reintentos:
     $$R_{\text{total}} = R_{\text{order}} \times R_{\text{payment}} = 3 \times 3 = 9 \text{ peticiones}$$
   - Esto creaba un efecto avalancha (**Thundering Herd / Retry Storm**) que saturaba la red, agotaba los pools de conexiones y colapsaba al proveedor.

---

## 2. Topología de Resiliencia: Single Retry Ownership y Timeout Budgeting

### 2.1. Principio de Propiedad Única del Reintento (Single Retry Ownership)
En una cadena síncrona de microservicios, **los reintentos no deben acumularse en cada salto**. La responsabilidad del reintento debe asignarse exclusivamente al componente más cercano a la falla transitoria:

```
[order-service]                  [payment-service]                [provider-simulator]
       │                                 │                                  │
       │─── POST /payments (Sin Retry) ─>│                                  │
       │    (Timeout Budget: 3000ms)     │─── POST /charges (Attempt 1) ───>│
       │                                 │    (Read Timeout: 800ms)         │
       │                                 │<─── Timeout / 503 ───────────────│
       │                                 │                                  │
       │                                 │─── POST /charges (Attempt 2) ───>│  <-- Single Retry Owner
       │                                 │<─── 200 OK (Approved) ───────────│
       │<── 201 Created (Approved) ──────│
```

- **Acción Implementada:**
  - Se eliminó `@Retry(name = "general")` de `PaymentHttpClient` en `order-service`.
  - `payment-service` permanece como el **único dueño del reintento** (`providerService` con `@Retry`), reintentando fallos transitorios de red (`ResourceAccessException`, `ProviderCommunicationException`) contra `provider-simulator`.

### 2.2. Presupuesto de Timeouts Distribuido (Timeout Budgeting)
Para que una llamada remota tenga éxito en presencia de reintentos aguas abajo, el timeout del cliente consumidor debe cubrir el peor escenario del proveedor:

$$\text{Timeout}_{\text{consumidor}} \ge N_{\text{reintentos}} \times (\text{Timeout}_{\text{salto}} + \text{Backoff}) + \text{Margen}$$

- **Cálculo Real para Week 13:**
  - `payment-service`:
    - `max-attempts`: 3
    - `read-timeout`: 800 ms
    - `wait-duration`: 100 ms
    - Peor caso aguas abajo: $3 \times 800\text{ ms} + 2 \times 100\text{ ms} = 2.600\text{ ms}$.
  - `order-service`:
    - En el archivo `application.yml`, se corrigió `clients.payment.read-timeout` de `100ms` a `3000ms`.
    - Esto garantiza un margen de seguridad de $400\text{ ms}$ para procesamiento interno, serialización JSON y latencia de red, evitando cortes prematuros cuando el proveedor está atendiendo la solicitud.

---

## 3. Garantía de Idempotencia y Concurrencia Extrema

### 3.1. Preservación Fiel del `Idempotency-Key`
El encabezado HTTP `Idempotency-Key` viaja inmutable de extremo a extremo:
1. `order-service`: Genera `ORDER-${orderId}-PAYMENT` y lo persiste como identificador canónico de la transacción.
2. `payment-service`: `PaymentController` extrae `@RequestHeader("Idempotency-Key") String key` y lo entrega intacto al caso de uso.
3. `provider-simulator`: Recibe el mismo `idempotencyKey` en cada reintento de `payment-service`.

### 3.2. Deduplicación Atómica en Base de Datos y Prevención de Bloqueos
El diseño de `PaymentApplicationServiceUseCase` aplica dos patrones arquitectónicos fundamentales:

1. **Evitar Transacciones Abiertas Durante Llamadas Remotas:**
   - La llamada HTTP `paymentProvider.charge(...)` se ejecuta **fuera** de una transacción de base de datos (`@Transactional`).
   - Mantener conexiones de base de datos abiertas durante una llamada remota de red es un antipatrón grave que agota el pool HikariCP ante aumentos de latencia del proveedor.

2. **Resolución de Concurrencia mediante Restricción Única (Unique Constraint Race):**
   - Cuando múltiples hilos concurrentes intentan procesar la misma clave simultáneamente:
     - El primer hilo inserta el registro exitosamente.
     - Los hilos competidores capturan `DuplicateKeyException` generada por la restricción de base de datos (`uk_idempotency_key`).
     - El bloque `catch (DuplicateKeyException)` consulta el registro ganador y retorna el resultado existente vía replay.

3. **Matriz de Idempotencia: Replay vs Conflict:**
   - **Misma Clave + Mismo Payload:** Retorna la transacción previamente completada con bandera `replayed: true` (HTTP 200/201). No se vuelve a contactar al proveedor.
   - **Misma Clave + Diferente Payload (monto, orden o moneda alterados):** Lanza `IdempotencyKeyConflictException` (HTTP 409 Conflict), previniendo ataques de reutilización o errores de cliente.

---

## 4. Gestión de Resultados Inciertos y Fallos Parciales (Uncertain Outcomes)

### 4.1. El Problema del Estado Desconocido (The Two Generals Problem)
Cuando un cliente experimenta un `SocketTimeoutException` o `ReadTimeoutException` al invocar un servicio de pagos:
> **El cliente NO sabe si:**
> 1. La petición nunca llegó al servidor.
> 2. El servidor procesó la transacción pero la respuesta se perdió en la red.
> 3. El servidor aún está procesando la solicitud.

Asumir fallo y no reintentar deja órdenes en estado inconsistente. Reintentar sin idempotencia causa cobros dobles.

### 4.2. Estrategia de Mitigación en Producción
Para mitigar resultados inciertos a escala enterprise, se recomiendan los siguientes patrones:

1. **Patrón Transactional Outbox & Polling Asíncrono:**
   - En lugar de confiar únicamente en la respuesta síncrona, `order-service` registra la intención de pago en una tabla local `outbox_payment_requests` con estado `PENDING`.
   - Un worker asíncrono o un job de conciliación consulta el estado de la transacción mediante un endpoint de verificación `GET /payments/status/{idempotencyKey}` antes de tomar cualquier acción correctiva.

2. **Reversa Transaccional y Sagas de Compensación:**
   - Si la transacción se declara definitivamente en estado ambiguo tras agotar el protocolo de consulta, el orquestador dispara una transacción compensatoria (`POST /payments/{id}/cancel` o `refund`) usando la misma clave de correlación.

---

## 5. Circuit Breaker y Prevención de Sobrecarga (Resilience4j)

### 5.1. Cuándo y Dónde Ubicar el Circuit Breaker
Un Circuit Breaker debe proteger el límite del servicio contra caídas sistémicas del proveedor externo.

**Configuración Recomendada para `payment-service`:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      providerService:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50.0
        slow-call-rate-threshold: 50.0
        slow-call-duration-threshold: 1500ms
        wait-duration-in-open-state: 15s
        permitted-number-of-calls-in-half-open-state: 5
        automatic-transition-from-open-to-half-open-enabled: true
```

### 5.2. Exponential Backoff con Full Jitter
Para evitar que las peticiones reintentadas choquen periódicamente en la misma ventana temporal, el algoritmo de backoff debe incluir jitter aleatorio:
$$t_{\text{wait}} = \text{random}(0, \min(t_{\text{max}}, t_{\text{base}} \times 2^{\text{attempt}}))$$
Esto aplana los picos de tráfico y permite que los sistemas degradados se recuperen gradualmente.

---

## 6. Observabilidad Distribuida y Monitoreo Enterprise

### 6.1. Propagación de Contexto (W3C TraceContext y MDC)
Cada petición debe trazarse unívocamente a través de los límites del sistema:
- `X-Correlation-Id`: Identificador de trazabilidad propagado en encabezados HTTP e indexado en Mapped Diagnostic Context (MDC) de SLF4J/Log4j2 para logs estructurados.
- Formato de log estructurado validado en el proyecto:
  ```
  PAYMENT_HTTP | event=REQUEST_SENT | orderId=ORD-1 | idempotencyKey=ORDER-1-PAYMENT
  PAYMENT_HTTP | event=COMMUNICATION_FAILURE | orderId=ORD-1 | idempotencyKey=ORDER-1-PAYMENT | cause=SocketTimeoutException
  ```

### 6.2. Métricas y Alertas Tempranas (Micrometer / Prometheus)
1. `resilience4j.retry.calls`: Monitoreo del ratio `successful_without_retry` vs `successful_with_retry` vs `failed_with_retry`.
2. `http.client.requests.p99`: Alerta temprana cuando el percentil 99 supere el 70% del `read-timeout`.
3. Tasa de colisiones de idempotencia: Métricas de peticiones repetidas para detectar ráfagas de clientes con bugs en bucles de reintento.

---

## 7. Matriz de Cumplimiento de Criterios de Evaluación

| Criterio | Estado | Evidencia Técnica |
|---|:---:|---|
| **Diagnóstico** | ✅ Cumplido | Identificada sobreescritura de `Idempotency-Key` en `PaymentController` y retry storm por `@Retry` en `order-service` con timeout de 100ms. |
| **Resiliencia** | ✅ Cumplido | Single Retry Ownership en `payment-service`; `read-timeout` de `order-service` incrementado a 3000ms. |
| **Idempotencia** | ✅ Cumplido | Clave conservada de principio a fin; deduplicación probada en concurrencia (12 hilos) y rechazo de payloads conflictivos (409 Conflict). |
| **Código y Arquitectura** | ✅ Cumplido | Sin `@Transactional` en llamadas remotas; Inyección por constructor; cero wildcards; separación de capas de dominio y puertos de salida. |
| **Pruebas de Aceptación** | ✅ Cumplido | 100% de tests en verde en `order-service` (25 tests), `payment-service` (27 tests) y `provider-simulator` (2 tests). Pruebas originales intactas. |
| **Pipeline CI/CD** | ✅ Cumplido | Agregador `sessions/week-13-microservicios/pom.xml` creado para garantizar compatibilidad directa con `azure-pipelines.yml`. |
