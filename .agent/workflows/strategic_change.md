---
description: Protocolo para realizar cambios en la "Inteligencia del Agente" (Workflows, Reglas)
---



# Workflow: Cambio Estratégico (Meta-Ingeniería)

Usa este workflow cuando modifiques archivos en `.agent/` o cambies la forma de trabajar.

## 1. Validación de Impacto
1.  **Reflexiona**: ¿Por qué cambio esto?
2.  **Versionado**: Incrementa `vX.Y.Z-dev.N+1` en `app/build.gradle`.

## 2. Ejecución y Documentación
1.  Modifica los archivos en `.agent/`.
2.  **BITACORA.md**: Añade entrada `### [Meta-Ingeniería] Título | Fecha...`.
3.  **CHANGELOG.md**: Añade el cambio bajo `### Engineering & Process`.

## 3. El Commit Pedagógico
* **Subject**: `meta: <Descripción corta>`
* **Body**: Explica el PROBLEMA del proceso anterior y la SOLUCIÓN estratégica.

## 4. Cierre
* `git add .`
* `git commit -m "meta: ..." -m "..."`
* `git push origin main`