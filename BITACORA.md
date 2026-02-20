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

## 🚀 Fase 3.3: Control Bidireccional y API POST v1.0-dev.9 | 20-Feb-2026
### 📜 El Problema
El panel web construido en la fase anterior era de "solo lectura". Si detectábamos que el entorno se mantenía ruidoso o queríamos "apagar" el Centinela temporalmente (Standby), debíamos usar la interfaz física del teléfono. Se requería una API Inversa (POST) para setear la inteligencia viva.

### 🛠️ La Solución
1. **Modal de Ajustes**: Inyección de un botón `⚙️ Ajustes` que despliega un Panel flotante oscuro en `index.html`. 
2. **Hidratación de Estado**: Al abrir el modal, la UI se "hidrata" (rellena) de forma *Stateless* con las variables escaneadas del último `fetch` al GET `/api/status`, sin requerir una consulta extra.
3. **Endpoint POST `/api/settings`**: Actualización de la función iteradora de URI en `WebServer.java`.
4. **Parseo de Cuerpos JSON**: Para sortear el clásico comportamiento de `NanoHTTPD` en el manejo de peticiones de datos crudos (`application/json`), se instanció un `Map` para recolectar las salidas del método nativo `session.parseBody()`. NanoHTTPD arroja allí el RAW payload JSON bajo la key genérica `postData`.
5. **Ajuste en Caliente**: Extraído el `postData`, construimos el `JSONObject` y reescribimos vía un `SharedPreferences.Editor` el esqueleto del `Context` principal del `AudioSentinel`, logrando control reactivo sin necesidad de matar el proceso maestro.

### 🎓 Lecciones Aprendidas
- El truco del mapa (`files.get("postData")`) es el estándar *de facto* más estable para obligar a un servidor primitivo como NanoHTTPD a tragar JSON arrays transparentes sin saturarse.
- Usar un Endpoint unificado (`/api/status` devolviendo toda la configuración) simplifica masivamente la arquitectura JS reduciendo asincronías y estados cruzados en IoTs de bajos recursos (Xiaomi Redmi 9C).

## 🚀 Fase 4: Historial Forense y Streaming de Audio v1.0-dev.10 | 20-Feb-2026
### 📜 El Problema
De nada sirve detectar un problema si no podemos evaluar las pruebas de inmediato. Las grabaciones de audio en crudo `.wav` quedaban aisladas en la memoria local del Redmi 9C forzando al operador a extraerlas manualmente por cable o administrador de archivos de Android. Tarea tediosa en despliegue.

### 🛠️ La Solución
1. **Endpoint REST API (`/api/recordings`)**: Se ordenó a `WebServer.java` leer `DIRECTORY_MUSIC`. Un filtro anónimo depura iteraciones listando solo archivos `.wav` y los ordena cronológicamente (más recientes primero). La metadata calculada es devuelta en un JSONArray.
2. **Audio Streaming Engine (`/api/audio`)**: Implementación del endpoint dinámico que acepta el `queryParam` string `file`. Se protege la integridad del sistema anulando cualquier intento de *Path Traversal* (`../` o `/`). Se canaliza el byteflow de disco puro hacia la red mediante `newChunkedResponse` alimentado por un crudo `FileInputStream`.
3. **Frontend AJAX**: Se acopló la capa de control *Historial de Alertas* al `index.html`. Una función pura JS `loadHistory` maqueta iterativamente bloques `div` y les incrusta etiquetas HTML5 `<audio controls preload="none">`.
4. **Protección de Red Core**: Forzar el uso indiscriminado de `preload="none"` es la diferencia entre un dashboard funcional y estampar la RAM del NanoHTTPD contra el suelo. Impide que 10-20 audios pesados carguen su byterate anticipado sobre el Thread UDP principal del servidor web al mismo tiempo en el *refresh*.

### 🎓 Lecciones Aprendidas
- Emplazar el tag `<audio controls preload="none">` protege la salud y la memoria de servidores ligeros emulados permitiendo listar infinitas pistas consumiendo cero bandwidth inicial de red.
- Enviar el objeto crudo `FileInputStream` a NanoHTTPD es la verdadera panacea Zero-Copy inter-procesos para Android embebido TCP.

## 🚀 Fase 5: Modo Walkie-Talkie (Streaming de Audio Real-Time) v1.0-dev.11 | 20-Feb-2026
### 📜 El Problema
Solo teníamos datos históricos o telemetría de amplitud, pero en un caso de alarma, es vital escuchar el entorno en ese preciso microsegundo antes de que se grabe, de forma continua e ilimitada. Los audios HTML5 asumen que siempre conocen el final del archivo.

### 🛠️ La Solución
1. **AudioSentinel como Difusor Concurrent**: Añadimos una `CopyOnWriteArrayList<OutputStream>` que registra y desvincula iterativamente descriptores HTTP abiertos. Cada vuelta del bucle de captura empuja bytes en caliente a todo array list vivo.
2. **Endpoint `/api/stream` y el Espejismo WAV**: Se instanció la dupla gloriosa de Java `PipedInputStream` y `PipedOutputStream`.
3. **El Engaño a Safari/Chrome**: Escribimos *a mano* los 44 bytes sagrados del Header WAV. En el tamaño absoluto de los chunks (`SubChunk2Size` y `ChunkSize`) inyectamos el valor hexadecimal tope de un entero sin signo de 32 bits: `0xFFFFFFFF`.
4. **Respuesta Transaccional**: Esto convence al reproductor del Frontend de que acaba de descargar un archivo que dura teóricamente el equivalente a meses ininterrumpidos. Se engancha por ChunkedResponse y engulle los bytes *Little Endian* del Sentinel en directo bajo una latencia ridícula de milisegundos.

### 🎓 Lecciones Aprendidas
- Emplear la técnica del Header `0xFFFFFFFFL` sobre un `PipedOutputStream` es el pináculo de la piratería legal TCP para forzar HTML5 a reproducir streams PCM crudos sin intermediarios WebSocket ni librerías de terceros NodeJS/WebRTC. Una arquitectura 100% nativa.

## 🚀 Hotfix v1.0-dev.12: Soporte de Reproducción en Safari (iOS) | 20-Feb-2026
### 📜 El Problema
Al reproducir los audios grabados (`.wav` o `.m4a`) desde la interfaz web usando un iPhone (Safari), el reproductor nativo HTML5 arrojaba un "Error" y se negaba a iniciar la reproducción. Safari es extremadamente estricto con los archivos multimedia y exige soporte de peticiones HTTP `Range` (byte-ranges) para permitir buscar (seek) y reproducir los audios.

### 🛠️ La Solución
1. **Soporte `Accept-Ranges: bytes`**: Se reescribió el endpoint `/api/audio` en `WebServer.java` abandonando el viejo `newChunkedResponse`.
2. **Peticiones HTTP 206 Partial Content**: El endpoint ahora lee activamente el Header `Range` de la petición web. Calcula los offset (inicio y fin) y hace uso de `FileInputStream.skip()` para entregar el segmento exacto demandado por el navegador.
3. **MIME dinámico**: El framework inyecta dinámicamente cabeceras de longitud (`Content-Length`, `Content-Range`) ajustadas al mime.

### 🎓 Lecciones Aprendidas
- Servir un estado HTTP `200 OK` con un stream genérico para audio en HTML5 funciona en Android o Escritorio, pero en el ecosistema Apple (Webkit) fracasa. Safari necesita confirmaciones `206 Partial Content` para habilitar los componentes nativos.

## 🚀 Fase 3.4: Reproducción Exclusiva (Solo-Play) v1.0-dev.13 | 20-Feb-2026
### 📜 El Problema
En el Centro de Mando Web, si el usuario abría múltiples alertas del historial a la vez, o si le daba al botón "Escuchar en Vivo" sin detener la alerta previa, la API HTML5 colisionaba los audios generando una cacofonía incomprensible de múltiples orígenes simultáneos.

### 🛠️ La Solución
1. **Event Delegation en Fase de Captura**: Se inyectó en `index.html` un listener global `document.addEventListener('play', ..., true)`. Usamos *captura* porque los eventos de media (`play`, `pause`) no burbujean hacia arriba en el DOM de forma natural.
2. **Silenciamiento DOM**: Cuando cualquier `<audio>` dispara el evento, el código itera sobre todos los elementos `<audio>` de la página invocando su método `.pause()`, excepto para aquel que originó el evento.
3. **Cross-Silencing (Historial vs Live)**: Si el Objeto `Audio` global (`liveAudio`) está instanciado, se mata y resetea la UI a OFF. Simétricamente, al activar manualmente "Escuchar en Vivo", recorremos el DOM apagando de forma preemptiva cualquier alerta que estuviera sonando (con `a.pause()`).

### 🎓 Lecciones Aprendidas
- Para interceptar eventos de medios (`play`, `pause`) creados dinámicamente sin atar listeners a cada nodo individual, la delegación de eventos vía la fase de *capturing* (tercer argumento `true` en `addEventListener`) es el patrón más limpio y de menor consumo de memoria para Vanilla JS.

## 🚀 Mantenimiento Estratégico: Blindaje de ADB (Race Conditions) v1.0-dev.14 | 20-Feb-2026
### 📜 El Problema
Al aplicar en cadena relámpago los comandos de `deploy_snapshot.md` (`build && install && am start`), la app no lograba lanzarse en el Xiaomi. El volcado forense `dumpsys` descubrió que MIUI 12 (Android 10) descartaba y bloqueaba peticiones de `am start` que se invocaban escasos milisegundos después de finalizar una instalación, ya que para el cerebro del dispositivo, el paquete aún se consideraba "bloqueado por re-registro".

### 🛠️ La Solución
1. **Doma de la Meta-Inteligencia**: Se alteró el propio "Libro de Reglas" (`.agent/workflows/deploy_snapshot.md`).
2. **Ralentización Impuesta**: Añadido un escalón de enfriamiento (`sleep 2`) expresamente documentado entre la línea de `adb install` y `adb shell am start`. Ahora, el script general aguarda pacientemente a que se purguen los broadcasts remanentes (`com.david.eloidodelabuelo flg=0x4000010`) antes de presionar el botón de inicio.

### 🎓 Lecciones Aprendidas
- Las integraciones continuas locales y los encadenamientos binarios en Bash (`&&`) no tienen piedad. A diferencia de un humano que por la limitación física tardaría un segundo en tipear el siguiente comando ADB, los scripts compiten contra los mecanismos de seguridad de Android. Forzar delays mecánicos es indispensable en testing autónomo sobre móviles.
