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

## 🚀 Fase 2: Motor de Grabación WAV Dinámico v1.0-dev.5 | 20-Feb-2026
### 📜 El Problema
Necesitábamos dotar al centinela de inteligencia para discernir verdaderos ruidos de falsos positivos, además de una forma de persistir el audio capturado con una cabecera WAV válida, todo ello pudiendo reaccionar en caliente a cambios de configuración.

### 🛠️ La Solución
1. **SharedPreferences Dinámicas**: Lectura cíclica de parámetros (`DETECTION_ENABLED`, `SHIELD_ENABLED`, `SPIKE_THRESHOLD`, etc.) directamente en el hilo de grabación sin bloquearlo.
2. **Standby (Kill Switch)**: Si se desactiva la detección, el hilo duerme usando `Thread.sleep` (0% CPU).
3. **Escudo Anti-Falsos Positivos**: Lógica de acumulación de picos (`REQUIRED_SPIKES`) en una ventana temporal (`SHIELD_WINDOW_MS`) para confirmar una alarma.
4. **Perro Guardián (Retrigger)**: Extensión dinámica de la grabación continua si se detectan nuevos picos.
5. **Motor WAV**: Volcado continuo a un `FileOutputStream` con cabecera de 44 bytes escrita al inicio y sobrescrita al final usando `RandomAccessFile` para ajustar el tamaño.

### 🎓 Lecciones Aprendidas
- La inyección del `Context` en `AudioSentinel` permite que el hilo hijo reaccione a cambios de preferencias hechos por la UI inmediatamente, formando la base para el frontend web futuro.
- El uso de `RandomAccessFile` es indispensable para fijar el tamaño final del archivo `.wav` sin corromper el flujo crudo grabado inicialmente.

## 🚀 Fase 3: Panel Web y Telemetría v1.0-dev.6 | 20-Feb-2026
### 📜 El Problema
Para gobernar el centinela desde otro dispositivo en la misma red y monitorizar sus detecciones en tiempo real (sin necesidad de mirar el móvil físico), necesitamos exponer su estado interno vía red.

### 🛠️ La Solución
1. **Telemetría en AudioSentinel**: Añadimos variables volátiles (`currentAmplitude`, `isRecordingStatus`) para ser leídas de forma *thread-safe* desde fuera.
2. **WebServer**: Implementación ligera usando `NanoHTTPD` en el puerto 8080.
3. **Endpoint API**: La ruta `/api/status` devuelve un JSON estructurado con la amplitud de ruido en tiempo real, estado de grabación y estado de `DETECTION_ENABLED`.
4. **Endpoint UI**: La ruta raíz `/` escupe por ahora un HTML temporal en crudo.
5. **Integración**: `OidoService` inicializa el servidor junto con el centinela, encriptando el ciclo de vida de ambos en `onCreate()` y `onDestroy()`.

### 🎓 Lecciones Aprendidas
- La inicialización y apagado coordinado (`start`/`stop`) de hilos secundarios y servidores web dentro de un `Service` previene bloqueos de puerto (`BindException`) cuando Android intenta reiniciar el componente tras liberaciones de memoria por doze-mode.
- El objeto estándar `JSONObject` de la API de Android agiliza la construcción de los payloads JSON sin necesidad de importar librerías pesadas como GSON para esta etapa temprana.

## 🚀 Hotfix v1.0-dev.7: Permiso INTERNET | 20-Feb-2026
### ❌ Intento Fallido (v1.0-dev.6)
El despliegue en dispositivo físico falló en tiempo de ejecución. El logcat reveló: `java.net.SocketException: socket failed: EACCES (Permission denied)` en la línea de `fi.iki.elonen.NanoHTTPD.start()`.

### 🛠️ La Solución
Android impone una restricción férrea de seguridad: cualquier apertura de socket (incluso si es `localhost:8080`) requiere obligatoriamente el tag `<uses-permission android:name="android.permission.INTERNET" />` en el `AndroidManifest.xml`. Se inyectó esta línea y se relanzó la batería de despliegue.

### 🎓 Lecciones Aprendidas
- Nunca subestimar la burocracia de permisos de Android. Un servidor local (NanoHTTPD) exige permisos de internet globales, rompiendo la asunción de que las conexiones loopback están exentas.

## 🚀 Fase 3.2: Frontend Dashboard y Vúmetro AJAX v1.0-dev.8 | 20-Feb-2026
### 📜 El Problema
No podíamos incrustar páginas HTML estáticas y aburridas como *String constants* en Java; era sucio, poco mantenible e impedía separar la lógica backend de la UI. 

### 🛠️ La Solución
1. **Directorio Assets**: Se ha construido la arquitectura de Frontend dentro de `app/src/main/assets/web/`.
2. **Vanilla JS & UI Muteada**: `index.html` sirve un Dashboard en "Modo Oscuro" usando Variables de CSS puras (`--bg-color`, `--status-green`, etc.).
3. **Vúmetro en Tiempo Real**: Un polling agresivo de JS cada 200ms `fetch('/api/status')` altera dinámicamente el ancho (`width`) y color de background del vúmetro.
4. **Respuesta Chunked en NanoHTTPD**: El viejo endpoint raíz `/` de `WebServer.java` ahora lee dinámicamente `.getAssets().open("web/index.html")` y envía el HTML con un `newChunkedResponse` sin saturar la RAM.

### 🎓 Lecciones Aprendidas
- Emplear `InputStream` de Android Assets directo al `newChunkedResponse` de NanoHTTPD es la vía más limpia y eficiente (`0-copy` conceptual) para escupir Frontend complejo en aplicaciones IoT.
- El polling a 200ms es perfectamente tolerado por NanoHTTPD en redes locales sin degradar la memoria de Android.
