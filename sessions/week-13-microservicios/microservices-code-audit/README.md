# The Retry Storm — Instructor Solution

A three-service Java 21 / Spring Boot lab about retry amplification, ambiguous timeouts, idempotency, and observability. This is a multi-module Maven project, not a monolith.

## Architecture

- **order-service** (`8081`) — accepts an order, persists it, and makes exactly one HTTP call to Payment Service. It does not retry that boundary.
- **payment-service** (`8082`) — owns the provider integration, retries transient connection/read failures up to three total attempts, and persists the resulting payment behind an application output port.
- **provider-simulator** (`8083`) — commits an idempotent charge before deliberately delaying the HTTP response. It counts HTTP requests separately from unique charges.

Payment Service owns retries because it owns the provider-specific failure classification, timeout, and idempotency behavior. The same stable idempotency key is propagated from Order through Payment to the provider. Reusing a key with a different business payload is rejected with HTTP 409.

## Requirements
- JDK 21
- Maven 3.9+

## Build
From the project root: `mvn clean verify`

## Run locally
Open three terminals from the project root and run in this order:

```bash
mvn -pl provider-simulator spring-boot:run
```
```bash
mvn -pl payment-service spring-boot:run
```
```bash
mvn -pl order-service spring-boot:run
```

The simulator defaults to a 500 ms response delay. Payment Service has a 100 ms read timeout and retries three total attempts. Order Service waits 600 ms for Payment Service's final response, so it does not add an outer retry layer.

## Reproduce the timeout / idempotency scenario
Reset provider metrics:

```bash
curl -X DELETE http://localhost:8083/provider/metrics
```

Create an order:

```bash
curl -i -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: retry-storm-demo-001' \
  -d '{"orderId":"ORD-1001","amount":250000.00,"currency":"COP"}'
```

With the default delay, the request normally ends with HTTP 503 from Payment Service. The provider may nevertheless have committed the charge before the response timed out. After the retries complete, inspect:

```bash
curl http://localhost:8083/provider/metrics
curl http://localhost:8083/provider/charges
```

Expected for one isolated operation: `totalRequests: 3` and `uniqueCharges: 1`. The three HTTP requests represent the initial call plus two retries; the single charge demonstrates provider idempotency. Reset metrics between runs.

To demonstrate a successful payment, start the simulator with no delay:

```bash
mvn -pl provider-simulator spring-boot:run -Dspring-boot.run.arguments=--provider.simulation.response-delay-ms=0
```
Then submit an order with a new `orderId`.

## Correlation and logs
Each service accepts `X-Correlation-Id`, generates one when absent, places it in Log4j2 ThreadContext, and returns it in the response header. The ID is propagated on outbound HTTP calls. The servlet filter clears ThreadContext in `finally` so pooled request threads cannot leak context. Retry event logs are emitted by Resilience4j in Payment Service; `REQUEST_SENT` / `COMMUNICATION_FAILURE` logs are emitted at the HTTP adapter boundary. Retry events describe policy decisions; provider metrics count actual received HTTP requests.

## Design decisions
- Application use case depends on application output ports, not Spring MVC DTOs or HTTP adapters.
- JDBC and transaction handling live in `JdbcPaymentRepository`; remote I/O is not performed inside a database transaction.
- Request DTO validation lives at the web boundary.
- Retry ownership is singular for this exercise: Payment Service retries Provider; Order Service does not retry Payment.
- Idempotency keys are unique in the database and simulator. Payload mismatch is a conflict, not a replay.
- URLs and timeout values are externalized in each service's `application.yml`.

## Important limitation
The provider simulator's in-memory idempotency store is suitable only for this lab. A production payment integration needs durable idempotency at the actual provider, reconciliation for unknown outcomes, bounded exponential backoff with jitter, circuit-breaker and bulkhead policies sized to capacity, authentication, metrics/tracing export, and an operational recovery process. The sample demonstrates correlation propagation, not full distributed tracing.
