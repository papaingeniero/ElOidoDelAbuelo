---
description: Proceso estandarizado para publicar una nueva versión estable (Release)
---



# Workflow: Publicar Nueva Versión

Sigue estos pasos estrictamente cuando el usuario solicite una "Release".

## 1. Safety Check (Semáforo)
1.  **Ejecuta**: `git status --porcelain`
2.  **Verifica**: Si hay salida (archivos modificados), STOP. Exige consolidar cambios pendientes.

## 2. Preparación y Documentación
1.  **Arqueología (Memoria Histórica)**: Identifica snapshots fallidos desde la última versión y documéntalos en la Bitácora.
2.  **Bitácora**: Edita `BITACORA.md` (append) con la crónica completa. Formato: `## 🚀 Phase X: Título | Fecha...`. Guarda esto en `bitacora_temp.md`.
3.  **Versionado**: Incrementa `versionName` en `app/build.gradle` y añade entrada a `CHANGELOG.md`.

## 3. Ejecución de Release (Git)
1.  **Stage**: `git add .`
2.  **Commit**: Subject: `tipo: Descripción breve vX.Y.Z`. Body: Copia literal de `bitacora_temp.md`.
3.  **Tag Enriquecido**: `git tag -a vX.Y.Z --cleanup=verbatim -m "Release vX.Y.Z" -m "$(cat bitacora_temp.md)" -m "$(cat CHANGELOG.md)"`
4.  **Limpieza y Push**: `rm bitacora_temp.md`, luego `git push origin vX.Y.Z` y `git push origin main`.

## 4. Cierre y Verificación de 8 Puntos
Reportar: Versión, Compilación, Ejecución, Bitácora, Changelog, Commit/Tag, Push, Git Status.

