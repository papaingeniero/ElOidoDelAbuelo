# Bitácora de Desarrollo: El Oído del Abuelo

## 🚀 Inicio del Proyecto | 19-Feb-2026
### 📜 El Problema
Necesitamos establecer una base sólida para el proyecto 'El Oído del Abuelo', asegurando compatibilidad estricta con Android 10 (API 29) y un entorno limpio.

### 🛠️ La Solución
Se ha inicializado el proyecto con la siguiente estructura:
- **Gradle**: Configuración optimizada para API 29.
- **Manifest**: Permisos de Audio/Storage/PhoneState y `legacyExternalStorage` activado.
- **MainActivity**: Implementación de solicitud de permisos en tiempo de ejecución.
- **Git**: `.gitignore` configurado con reglas estándar y específicas del agente.

### 🎓 Lecciones Aprendidas
- La importancia de `requestLegacyExternalStorage` en Android 10 para evitar Scoped Storage complejo innecesariamente en este MVP.
- La necesidad de aislar el entorno de compilación (Gradle Wrapper) para reproducibilidad.

## 🚀 Compilación Exitosa v1.0-dev.2 | 19-Feb-2026
### 📜 El Problema
La compilación inicial falló debido a una incompatibilidad entre el JDK 17 del sistema y Gradle 6.7.1, además de la falta de configuración para AndroidX.

### 🛠️ La Solución
1. **Upgrade de Infraestructura**:
   - Gradle Wrapper actualizado a 7.5.
   - Android Gradle Plugin actualizado a 7.2.2.
   - `android.useAndroidX=true` añadido a `gradle.properties`.
2. **Validación**:
   - Build exitoso en 32s.
   - APK generado: 3.1MB.

### 🎓 Lecciones Aprendidas
- **JDK 17 vs Gradle**: Las versiones antiguas de Gradle (6.x) no soportan clases Java 61 (JDK 17). Es mandatorio usar Gradle 7.3+ para entornos modernos.
- **AndroidX**: Aunque AGP moderno suele implicarlo, la ausencia explícita de `gradle.properties` puede causar fallos de classpath en builds limpios.

## 🚀 Fase 2: Motor de Escucha (Foreground) | 19-Feb-2026
### 📜 El Problema
Android 10 encadena restricciones severas a las apps en segundo plano. Una simple Activity escuchando el micrófono sería destruida por MIUI en minutos.

### 🛠️ La Solución
Implementación de una arquitectura de servicio persistente:
- **OidoService**: Elevado a `startForeground` con canal de notificación de baja prioridad (silencioso pero visible).
- **AudioSentinel**: Hilo dedicado para el procesamiento de audio crudo (PCM), desacoplado de la UI.
- **Robustez**: Manejo explícito de `AudioRecord.release()` para evitar fugas de memoria nativa.

### 🎓 Lecciones Aprendidas
- Es vital usar `android.R.drawable` para iconos rápidos en prototipado si `ic_launcher` no está generado en vectorial.
- La
## 🚀 Corrección Lógica de Inicio v1.0-dev.4 | 19-Feb-2026
### 📜 El Problema
Un bug lógico en `MainActivity` impedía que el servicio de escucha arrancara si los permisos ya habían sido concedidos previamente (e.g., al reiniciar la app). El bloque `checkAndRequestPermissions` solo iniciaba el servicio en el callback de `onRequestPermissionsResult`, ignorando el caso donde `listPermissionsNeeded` estaba vacío.

### 🛠️ La Solución
Se añadió un bloque `else` explícito para manejar el caso "Permisos ya concedidos":
- Si no hay permisos faltantes -> `startOidoService()` inmediato.
- Si faltan permisos -> `requestPermissions` (flujo original).

### 🎓 Lecciones Aprendidas

### ✅ Despliegue Exitoso v1.0-dev.4 | 19-Feb-2026
- **Build**: `./gradlew assembleDebug` (Clean build).
- **Install**: `adb install -r` (Update preserving data).
- **Verificación**: La app inició correctamente y el servicio `OidoService` arrancó de inmediato sin requerir re-concesión de permisos (Fix validado).

