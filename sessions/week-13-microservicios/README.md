# Week 13 — Microservicios: Resiliencia Distribuida

**Fecha**: 2026-09-24 · **Paquete**: Micro + Cloud + DevOps

## Objetivo

Analizar y corregir problemas de resiliencia en una arquitectura de microservicios, implementando una estrategia de reintentos controlada e idempotencia para evitar peticiones HTTP duplicadas ante timeouts, fallos de comunicación y respuestas tardías de un determinado componente.

## Contexto técnico

**Brecha QC atacada**: Microservicios (86%) — la comunidad lo quiere fortalecer (23% encuesta).  
Los sistemas distribuidos introducen problemas que no suelen presentarse de la misma manera en aplicaciones monolíticas: latencia de red, fallos parciales, pérdida de comunicación y resultados inciertos.

En este escenario, los reintentos descontrolados pueden multiplicar las solicitudes entre servicios y provocar sobrecarga o pagos duplicados. La sesión aborda cómo identificar estos problemas y diseñar mecanismos de resiliencia que mantengan la consistencia de las operaciones.

## Agenda de la sesión

| Tiempo | Actividad |
|--------|-----------|
| 0–10' | Demo: arquitectura de microservicios, fallos distribuidos, timeouts, retries e idempotencia |
| 10–30' | Reto: resolver `The Retry Storm` (ver `enunciado.md`) |
| 30–35' | Validación: pruebas de aceptación y `mvn verify` en ambos servicios |
| 35–40' | Socialización de la solución y Cierre: week-13-solution|