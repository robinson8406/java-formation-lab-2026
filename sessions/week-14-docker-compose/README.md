# Week 14 — Podman + Compose

**Fecha**: 2026-10-01 · **Paquete**: Micro + Cloud + DevOps

## Objetivo

Contenerizar la aplicación Spring Boot y orquestar el stack completo (app + base de datos) con Podman Compose para garantizar un entorno reproducible entre dev y CI.

> **¿Por qué Podman?** Por política de licencias se usa Podman en lugar de Docker Desktop. La sintaxis del `Dockerfile` y del archivo `compose.yml` es idéntica — el conocimiento aplica en ambos entornos.

## Contexto técnico

**Brecha QC atacada**: Cloud/Docker (76%), CI/CD (15% encuesta)  
"En mi máquina funciona" es el síntoma de falta de contenedores. Con Podman Compose el entorno de desarrollo es idéntico al de CI y staging.

## Agenda de la sesión

| Tiempo | Actividad |
|--------|-----------|
| 0–20' | Revisión `week-13-solution`: cuándo NO microservicios |
| 20–40' | Contexto + demo: `podman run` vs `podman compose up`, layers en el Dockerfile — Dockerfile multi-stage + compose (ver `enunciado.md`) |
| 40–60' | Q&A (se resuelve entre semana, PR antes del jueves siguiente) |
