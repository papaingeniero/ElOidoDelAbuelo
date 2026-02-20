---
description: Protocolo para desplegar una versión de prueba (Snapshot) en el Xiaomi Redmi 9C
---



Este workflow describe los pasos críticos para desplegar una Snapshot de desarrollo por ADB WiFi.

# 1. Alineación del Agente
- [ ] Leer y comprender `legacy_dev_rules.md`.

# 2. Identidad (Versionado)
- [ ] Incrementar `versionName` en `app/build.gradle`. (Formato: `vX.Y.Z-dev.N`).
- [ ] Verificar que `git status` está limpio.

# 3. Compilación y Despliegue
- [ ] Ejecutar `./gradlew assembleDebug`.
- [ ] Instalar APK: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Esperar a que el sistema asimile el cambio: `sleep 2`.
- [ ] Lanzar app: `adb shell am start -n com.david.eloidodelabuelo/.MainActivity`.

# 4. Documentación (Bitácora)
- [ ] Actualizar `BITACORA.md` (APPEND). Incluir "Lección del Día".
- [ ] Actualizar `CHANGELOG.md` (PREPEND).

# 5. Git Snapshot
- [ ] Generar archivo temporal `commit_msg.txt` con el contenido de la Bitácora.
- [ ] `git add .`
- [ ] `git reset commit_msg.txt`
- [ ] `git commit -F commit_msg.txt`
- [ ] `rm commit_msg.txt`
- [ ] `git push origin main`.

# 6. Verificación de 7 Puntos (Reporte Final)
Generar reporte estructurado copiando **ESTA PLANTILLA LITERAlMENTE** y mostrarlo siempre al final en el chat de la conversación:

## 🚀 Reporte de Despliegue vX.Y.Z-dev.N (Título Breve)
| FASE | ESTADO | NOTAS |
| :--- | :---: | :--- |
| 🏷️ **Identidad** | ✅ | Versionado aumentado en `build.gradle` |
| 🧹 **Purity** | ✅ | Git Status comprobado como limpio |
| 🏗️ **Build** | ✅ | `assembleDebug` completado sin errores |
| 📱 **Install** | ✅ | APK instalado via ADB y app ejecutada |
| 📜 **Bitácora** | ✅ | Crónica técnica redactada |
| 📝 **Changelog** | ✅ | Novedades registradas (`PREPEND`) |
| 📦 **Cápsula** | ✅ | Commit y Push hacia `origin main` |

### 🔍 Resumen de Cambios
- (Punto 1)
- (Punto 2)

### ⚠️ Reporte de Incidentes (OBLIGATORIO)
- **Problema descubierto**: ...
- **Solución aplicada**: ...