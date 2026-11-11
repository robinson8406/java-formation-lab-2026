#  Criterios de evaluación — Week 13
## Reto: The Retry Storm

### Pregunta clave del reto

**¿Cómo podemos garantizar que una operación de pago se procese una sola vez, aunque existan timeouts, reintentos y pérdida de comunicación entre servicios?**

### ¿Qué vamos a evaluar?

| Aspecto | ¿Qué esperamos? |
|---|---|
| [ ] Diagnóstico | Identificar por qué se están multiplicando las solicitudes y los pagos. |
| [ ] Resiliencia | Definir dónde deben ejecutarse los reintentos y cómo controlar los timeouts. |
| [ ] Idempotencia | Evitar que una misma operación genere pagos duplicados, conservando su `Idempotency-Key`. |
| [ ] Código y arquitectura | Implementar una solución limpia, desacoplada y fácil de mantener. |
| [ ] Pruebas | Conseguir que las pruebas existentes pasen sin modificarlas y demostrar que la solución funciona. |


### Tests
- [ ] `mvn verify` verde en ambos proyectos (`order-service` y `payment-service`)


### Reglas para aprobar el reto

- [ ] Las pruebas de aceptación deben quedar en verde.
- [ ] No se pueden modificar las pruebas entregadas.
- [ ] No se puede modificar el código de `provider-simulator`: es un proveedor externo.
- [ ] Las correcciones deben realizarse en `order-service` y `payment-service`.
- [ ] No basta con reducir los reintentos: hay que resolver la causa de los pagos duplicados.
- [ ] La misma operación de pago debe conservar su `Idempotency-Key` durante todos sus intentos.

### Escala de madurez

¿Qué se espera del desarrollador? 

| Junior | Semi-senior | Senior | Experto |
|--------|-------------|--------|---------|
| Encuentra el problema, realiza los ajustes necesarios y consigue que las pruebas pasen. | Identifica qué servicio debe controlar los reintentos y conserva correctamente la clave de idempotencia. | Resuelve la causa raíz, diseña una estrategia de resiliencia adecuada. | Analiza escenarios de concurrencia, fallos parciales y resultados inciertos. Propone mejoras de observabilidad y prevención de incidentes futuros. |

**Importante:** todos los niveles deben entregar una solución funcional. Lo que diferencia a cada nivel es la profundidad del análisis, la calidad de la solución y la capacidad de anticipar problemas de producción.

