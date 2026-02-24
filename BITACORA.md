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

## 🚀 Hotfix v1.0-dev.15: Streaming en Vacío durante Standby | 20-Feb-2026
### 📜 El Problema
El usuario reportó que el botón "Escuchar en Vivo" no producía ningún sonido. El diagnóstico reveló que al estar el sistema en modo Standby (`DETECTION_ENABLED = false`), el hilo del centinela `AudioSentinel` entraba en un bucle ciego de `Thread.sleep(1000)` para ahorrar batería, dejando de abastecer al buffer de streaming (`liveListeners`).

### 🛠️ La Solución
1. **Consciencia de Oyentes**: Se reubicó la comprobación `boolean hasListeners = !liveListeners.isEmpty();` al tope del bucle.
2. **Letargo Condicional**: Se alteró la sentencia del Standby a `if (!detectionEnabled && !hasListeners)`. Ahora, si la detección está apagada pero hay alguien esuchando la radio, el móvil no duerme y continúa despachando bytes PCM.
3. **Protección Forense**: Se blindó la lógica de disparo (Analizador de Picos y Escudo) exigiendo `detectionEnabled == true`. Esto garantiza que, aunque el micrófono despierte temporalmente por culpa de un oyente remoto, el ruido recogido no detone falsas alertas ni genere grabaciones `.wav` en Standby.

### 🎓 Lecciones Aprendidas
- Emplazar *Kill-Switches* de ahorros de energía abruptos (como `Thread.sleep` en hilos infinitos) puede causar "Daños Colaterales" funcionales si el hilo tiene responsabilidades duales (Detección y Streaming). Modular el flag de letargo con estados adyacentes evita interrupciones de disponibilidad (Downtime).

## 🚀 Mejora UX v1.0-dev.16: Identidad y Versión en Frontend | 20-Feb-2026
### 📜 El Problema
El usuario requería una forma visual y directa de confirmar qué constelación de código (versión) estaba ejecutando el servidor web integrado (NanoHTTPD) desde el navegador, para descartar falsos positivos de testing por cachés de Chrome o del SO.

### 🛠️ La Solución
1. **Inyección de BuildConfig**: El endpoint `/api/status` en `WebServer.java` fue modificado para despachar la constante en tiempo de compilación `BuildConfig.VERSION_NAME` dentro del JSON periódico.
2. **Estampado Dinámico de DOM**: En `index.html`, la rutina `updateDashboard()` intercepta `data.version`, pinta el string junto al cabecero del H1 (con color atenuado) e inyecta la versión en el `document.title` (pestaña del navegador).

### 🎓 Lecciones Aprendidas
- Exponer metadatos de compilación a través de las APIs de telemetría viva (`/api/status`) es una estrategia elegante que evita tener que templatear HTML dinámicamente o añadir endpoints superfluos como `/api/version`.

## 🎶 Fix Estructural v1.0-dev.17: Streaming WAV Estricto (Safari/iOS) | 20-Feb-2026
### 📜 El Problema
El streaming "Escuchar en Vivo" producía bloqueos silenciosos en Safari (iPhone), quedando la etiqueta `<audio>` atascada indefinidamente, mientras que en Chrome (Mac) funcionaba pero acumulando un fuerte retardo de buffering.

### 🛠️ La Solución
1. **Reconstrucción Matemática de Cabeceras WAV**: El código Java (`WebServer` y `AudioSentinel`) estaba emitiendo un torrente con `BlockAlign = 4`, que es inválido para frecuencias de 16-bit Mono. Se ha corregido la firma inyectando la fórmula estricta: `channels * 16 / 8 = 2` y `ByteRate = 32000`.
2. **Purgado de MIME HTTP**: Se forzó en el servidor la cabecera `audio/wav` explícita en lugar de texto plano o mime experimental, junto con los directivos `keep-alive` y `no-cache`.

### 🎓 Lecciones Aprendidas
- **Safari / CoreAudio es implacable**: Mientras que Blink (Chrome) es tolerante frente a metadatos corruptos (trata de ingerir la persistencia PCM cueste lo que cueste, pagando el precio en buffering), el motor de WebKit rechaza preventivamente cualquier cabecera geométrica `RIFF` que no cuadre a la perfección para evitar deadlocks de decodificación.

## 🧠 Upgrade Arquitectónico v1.0-dev.18: Web Audio API Streamer | 20-Feb-2026
### 📜 El Problema
Tras pulir las cabeceras WAV en la v17, Safari de iOS seguía negándose a reproducir el "Audio en Vivo", mientras Chrome lo reproducía con un indeseable lag o buffering inicial. La investigación reveló que el engine WebKit de Apple rechaza estricta y activamente cualquier streaming de Longitud Infinita (HTTP Chunked / sin Content-Length) inyectado directo a una etiqueta nativa `<audio>`.

### 🛠️ La Solución
1. **Destrucción de la Etiqueta HTML5 Nativa**: La UI web ya no delega el streaming al reproductor encapsulado de los navegadores (`new Audio('/api/stream')`).
2. **Inyección de Web Audio API (Vanilla JS)**: Se ha escrito una rutina en `index.html` que usa un `fetch()` asíncrono y la clase `ReadableStream` para atrapar cada pedazo (chunk) de bytes puro conforme salen del servidor NanoHTTPD.
3. **Conversión Aritmética Dinámica**: Javascript intercepta el array de Little-Endian 16-Bit PCM, decapitamos (ignoramos) los primeros 44 bytes para destruir el cabecero falso del WAV, y mapeamos matemáticamente cada short int a un `Float32Array` normalizado entre -1.0 y 1.0. 
4. **Reproducción Programática Continua**: Se inyectan colas consecutivas al `AudioContext` de la tarjeta gráfica del navegador (gapless playback schedule).

### 🎓 Lecciones Aprendidas
- Nunca confíes en el estándar `<audio>` multiplataforma si sirves streaming infinito en HTTP genérico sin formatos paquetizados complejos (como HLS/M3U8). Escribir el descodificador en la capa de Javascript `AudioContext` no solo garantiza compatibilidad con las políticas paranoicas de iOS, sino que **elimina permanentemente el retraso de buffering** en cualquier navegador de escritorio como Chrome.

## 🎨 Upgrade Estético v1.0-dev.19: The "El Ojo" Design System | 20-Feb-2026
### 📜 El Problema
El usuario solicitó erradicar el diseño morado básico ("Material Design Default") argumentando que parecía "una web feminista de Podemos". Propuso explícitamente adoptar e igualar el *Look & Feel* y la paleta de colores del proyecto hermano "El Ojo del Abuelo" (Cyber-Sec, Dark UI).

### 🛠️ La Solución
1. **Extracción Genética**: Se clonó e inspeccionó dinámicamente el código inyectado en red (`NanoHttpServer.java`) del repositorio del proyecto "El Ojo".
2. **Migración de CSS**: En `index.html` de "El Oído", se reescribieron las CSS variables raíz: 
   - Fondos: `#121212`, `#1f1f1f`.
   - Elementos Activos: Botón "Live" reconfigurado al icónico rojo vivo (`#d32f2f`) con la animación `@keyframes pulse` transplantada directamente.
3. **Rediseño de Componentes**: Las tarjetas planas del historial de grabaciones pasaron a ser redondeadas, sin gradiente y reactivas al hover/active, integrando colores de severidad (verde, rojo, amarillo) dinámicamente mediante JS.
4. **Android Native**: El `colors.xml` del launcher Android también se oscureció (PrimaryDark `#000000`) para no desentonar con el portal web.

### 🎓 Lecciones Aprendidas
- Aislar el diseño en variables `:root` globales (CSS Custom Properties) ha permitido refactorizar toda la personalidad de la app en menos de 5 minutos, garantizando a largo plazo un mantenimiento de Frontend rapidísimo.

## 🐛 Hotfix v1.0-dev.20: Desconexión JSON de Frontend | 20-Feb-2026
### 📜 El Problema
Al inyectar el diseño de "El Ojo del Abuelo" en la V19, el bloque iterativo de JavaScript (`files.forEach`) en `loadHistory()` asumió la existencia de variables (`f.lastModified`, `f.maxAmplitude`) que el Microservicio NanoHTTPD **nunca** enviaba, provocando que el renderizado de fechas crasheara mostrando "Invalid Date" y enmascarando el orden real descendente.

### 🛠️ La Solución
- Limpieza Javascript: Se ha re-esamblado el parseo numérico a `new Date(f.timestamp)` y formateado puro para el FileSize.
- El algoritmo `Arrays.sort()` en Java (Backend) que usa `Long.compare(f2.lastModified(), f1.lastModified())` estaba y sigue estando matemáticamente perfecto para devolver los archivos más nuevos primero; el error era solamente del visualizador.

## 👻 Arquitectura Headless v1.0-dev.21: Interfaz Invisible | 22-Feb-2026
### 📜 El Problema
Dado que la interacción al 100% con El Oído del Abuelo ocurre de forma remota vía Dashboard Web, la actividad principal en el teléfono gastaba pantalla, batería, y resultaba anti-estética al abrir una vista vacía solo para mantener vivo el Foreground Service. Había que convertir la app en un demonio en segundo plano (Daemon).

### 🛠️ La Solución
1. En `styles.xml` se configuró un tema `Theme.Headless` (`android:windowBackground="@android:color/transparent"`).
2. Se inyectó este tema de invisibilidad al `<activity>` en el `AndroidManifest.xml`.
3. Se extirpó el dibujado de vistas (`setContentView`) del método `onCreate` de `MainActivity.java`.
4. Se conectó una directiva de autodestrucción (`finish()`) tras lanzar con éxito el `OidoService`, permitiendo que la ventana muera al milisegundo mientras el micrófono y el servidor NanoHTTPD se independizan y viven en el Service.

## 🐛 Hotfix v1.0-dev.22: Ceguera de Caché Web | 22-Feb-2026
### 📜 El Problema
Al completar el salto arquitectónico de la v21, descubrimos que los iPhones, MacBooks y otros clientes HTTP ignoraban la nueva aplicación servida en el puerto 8080. El navegador se empeñaba en mostrar "v20" desde el disco local. Esto ocurría porque `NanoHTTPD` entrega sus paquetes limpios, sin cabeceras directivas que inhiban el agresivo almacenamiento en caché de los navegadores modernos para peticiones `GET`.

### 🛠️ La Solución
- Inyectar en cada Endpoint GET clave en `WebServer.java` (`/`, `/api/status`, `/api/recordings`) las directivas:
  - `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`
  - `Pragma: no-cache`
Esto fuerza permanentemente una conexión real Full-Duplex entre el Frontend del celular del Abuelo y nuestro ordenador local, ignorando archivos "muertos" que pueda guardar Safari o Chrome.

## 🚀 Feature v1.0-dev.23: Autostart Ignición (Boot Receiver) | 22-Feb-2026
### 📜 El Problema
Al despojar a la App de toda interfaz visual (Headless), el usuario aún se veía obligado a pulsar el icono de **El Oído del Abuelo** cada vez que el Redmi 9C se reiniciaba por accidente o apagón. Un sistema de alarma profesional debe restaurarse solo y retomar la vigilancia sin intervención humana (CCTV-Concept).

### 🛠️ La Solución
1. En `AndroidManifest.xml` añadimos `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>` para que Android nos autorice a escuchar su corazón.
2. Hemos registrado el `<receiver>` `BootReceiver` anclado al evento `BOOT_COMPLETED`.
3. Hemos creado `BootReceiver.java`, una clase asíncrona que despierta a `OidoService` exigiéndole reanudar el Micrófono y encender NanoHTTPD en cuanto el usuario teclea el PIN de su móvil al encender.

### ⚠️ Lección del Día (La Dictadura MIUI)
El Framework original de Google permite este Autoarranque, pero los teléfonos Xiaomi (MIUI) implementan un firewall de batería que **bloquea físicamente los Receptores de Arranque**. 
- **Acción Manual Necesaria**: Para que el código inyectado en la V23 triunfe, es obligatorio ir a (Ajustes -> Aplicaciones -> El Oído del Abuelo) y encender el botón nativo de **"Inicio Automático"**. Si no se presiona ese botón gráfico en el sistema del Xiaomi, este parche no servirá de nada.

## 👻 Feature v1.0-dev.24: Modo Fantasma (Decapitación del Launcher) | 22-Feb-2026
### 📜 El Problema
Manteniendo la filosofía de diseñar un **Microservicio IoT Puro**, carece de sentido que una aplicación que se enciende sola al arrancar el terminal, y cuya interfaz entera vive servida por IP remota 8080 (Headless), ensucie el cajón de aplicaciones del Sistema Operativo con un ícono interactivo irrelevante.

### 🛠️ La Solución
He intervenido el ADN principal en `AndroidManifest.xml`. He localizado el Intent-Filter del Componente `MainActivity` y ejecutado un borrado quirúrgico de la directiva `<category android:name="android.intent.category.LAUNCHER" />`. Se ha reemplazado por la directiva vacía `DEFAULT`. 
A partir de este instante estricto, **El Oído del Abuelo** ha dejado de tener representación gráfica local y Android prohíbe dibujarlo en los menús para el usuario casual del hardware. El único medio de control restante es mediante ADB por cable/Wifi o la web externa vía puerto 8080.

## 🔋 Feature v1.0-dev.25: Telemetría de Hardware en Tiempo Real | 22-Feb-2026
### 📜 El Problema
Al construir un "Sensor Domótico" ininterrumpido (IoT) que está operando en formato "Modo Fantasma" e ilocalizable en la capa visual, el desconocimiento del estado físico del hardware (Nivel de batería, voltaje transitorio, picos de temperatura del SoC) es un riesgo ciego. El Xiaomi Redmi 9C podría recalentarse procesando audio o estar desenchufado y el administrador remoto no se enteraría hasta que la línea cayera catastróficamente.

### 🛠️ La Solución
Se ha conectado el motor asíncrono `NanoHTTPD` directamente a la arteria del Hardware de Android (`BatteryManager`).
1. **Core Java**: `WebServer.java` envía un request pasivo (Null Intent Listener) por cada refresco GET de `/api/status`, extrayendo instantáneamente el % de Carga (`EXTRA_LEVEL`), el Switch de Enchufe Físico (`EXTRA_PLUGGED`) y la Temperatura Core en décimas de grado Celsius (`EXTRA_TEMPERATURE`).
2. **UI Web**: En `index.html` se ha empotrado bajo el título web un *Toolbar Telemetría* de estética Glassmorphism, inyectado nativamente en el ciclo AJAX de `updateDashboard()`.
3. **UX (Semáforo Biométrico)**: El frontend lee esos metadatos e interpola visualmente la salud termal y eléctrica. Verde radiante si carga. Avisos cálidos escalonados (Orange 35°C, Rojo Peligro > 40°C) y metamorfosis iconográfica entre la Pila llena y Vacía.

## 🗑️ Feature v1.0-dev.26: Purga Forense de Grabaciones (Botón del Pánico) | 22-Feb-2026
### 📜 El Problema
En un sistema de vigilancia autónomo y "Headless", el almacenamiento del Xiaomi Redmi 9C es un recurso finito que puede saturarse rápidamente con falsos positivos o grabaciones de larga duración (vía `RECORD_DURATION_MS`). Hasta ahora, la única forma de liberar espacio era mediante comandos manuales ADB o borrado físico, lo cual rompe la experiencia de administración remota "Zero-Touch".

### 🛠️ La Solución
Se ha implementado una terminal de destrucción segura controlada desde el Dashboard:
1. **Backend (Java)**: Se ha dotado a `WebServer.java` de la capacidad de recibir el método HTTP `DELETE` en la ruta `/api/recordings`. El código realiza un barrido atómico de la carpeta `DIRECTORY_MUSIC`, filtrando solo archivos `.wav` y eliminándolos uno a uno, devolviendo un reporte JSON con el conteo de bajas (`deleted_count`).
2. **Frontend (HTML/JS)**: Se ha añadido una "ZONA DE PELIGRO" resaltada en rojo carmesí dentro del modal de Ajustes. El botón "🗑️ Purgar Todo el Historial" dispara un `confirm()` preventivo en el navegador antes de ejecutar la purga asíncrona vía `fetch`.
3. **UX (Auto-Refresh)**: Tras la ejecución exitosa, la lista de grabaciones se vacía instantáneamente en el Dashboard sin necesidad de refrescar la página.

### 🎓 Lecciones Aprendidas
- La segregación de métodos HTTP (`GET` para listar, `DELETE` para purgar) en una misma URI es una práctica de diseño de APIs (REST) que simplifica enormemente la legibilidad del código del servidor `NanoHTTPD`, permitiendo que un mismo bloque condicional maneje lógicas opuestas de forma elegante.
328: 
329: ## 🍃 Feature v1.0-dev.27: Optimización Energética (Eco-Mode) | 22-Feb-2026
330: ### 📜 El Problema
331: El monitoreo constante de audio es una de las tareas más costosas para un SoC móvil. En versiones anteriores, el motor de audio despertaba a la CPU con demasiada frecuencia debido a buffers pequeños y realizaba lecturas de disco compulsivas (SharedPreferences) en cada ciclo del bucle, disparando el consumo de batería innecesariamente en reposo.
332: 
333: ### 🛠️ La Solución
334: Se ha realizado una cirugía de bajo consumo en el núcleo de la aplicación:
335: 1. **Buffering Táctico**: Se ha cuadruplicado el tamaño del buffer de `AudioRecord`. Al procesar ráfagas de audio más grandes, la CPU puede "dormir" más tiempo entre ciclos, reduciendo drásticamente los Wake-ups del procesador.
336: 2. **Cache RAM de Preferencias**: Se ha implementado un `OnSharedPreferenceChangeListener`. El hilo de audio ya no consulta el disco; ahora lee constantes volátiles en RAM que se actualizan solo cuando el usuario cambia algo en el Dashboard. Esto elimina miles de accesos a archivos XML por minuto.
337: 3. **Proxy de Telemetría**: El servidor web ya no interroga al hardware de batería en cada petición GET. Se ha implementado una caché con refresco de 60 segundos, minimizando el impacto de tener el Dashboard web abierto.
338: 
339: ### 🎓 Lecciones Aprendidas
340: - En sistemas embebidos/Android 10, es preferible procesar datos en ráfagas (Batch processing) que en flujo continuo mínimo, ya que permite que los estados de bajo consumo del núcleo (C-States) se activen de forma efectiva.

## 🚀 v1.0-dev.28 (2026-02-22) - El Salto del Oído: AAC Universal y Modo Tri-Estado

### 📜 El Problema
El uso de archivos WAV en crudo (16kHz, 16bit, Mono) generaba tasas de datos inaceptablemente grandes (32KB/s) que colapsaban el ancho de banda del websocket remoto y devoraban el almacenamiento local y la batería durante las operaciones prolongadas. Además, la lógica bi-estado (Stanby vs Recording) no era suficiente para cubrir todo el espectro táctico que un vigilante remoto requiere (ej: escuchar pero no grabar permanentemente si estamos en directo).

### 🛠️ La Solución
Se ha pivotado la arquitectura del core de audio de `AudioSentinel` desde un simple dump de buffers WAV hacia una codificación en tiempo real acelerada por hardware (MediaCodec de Qualcomm).

1.  **Motor AAC Universal**: Todo el audio (tanto el grabado a disco, como el enviado por red en vivo) pasa por `MediaCodec` (MIMETYPE_AUDIO_AAC, AACObjectLC) para lograr tasas de compresión de alta eficiencia sin sacrificar la inteligibilidad de la voz.
2.  **Streaming Nativo (ADTS)**: Abandonamos los wrappers de cabecera pre-calculada WAV infinita para el streaming web. En su lugar, empaquetamos manualmente los frames binarios del `MediaCodec` añadiendo una cabecera de 7 bytes *Audio Data Transport Stream* (ADTS) por frame.
    *   *Ventaja Bruta*: El Frontend (`index.html`) ahora puede tratar el stream de 1 bit como un simple tag `<audio autoplay src="/api/stream">` nativo soportado por Chrome y Safari móvil sin tener que levantar el *Web Audio API Context* con decodificación `Float32` por software (Ahorro de batería y latencia cero para el cliente final).
3.  **Matriz Tri-Estado (Modos de Vigilancia)**:
    *   `[0]` **Reposo Absoluto**: El Micrófono descansa por completo (Máximo ahorro de energía de MIUI).
    *   `[1]` **Escudo de Detección**: Se evalúan picos por software, si sobrepasa el umbral (SPIKE), se levanta el Codec para grabar el tramo.
    *   `[2]` **Grabación Continua**: Se puentea la lógica de evaluación y se alimenta al MediaCodec en bucle ciego infinito para una monitorización permanente (dashcam auditiva).
4.  **Phantom Codec para Streaming**: Si el usuario entra a escuchar en vivo desde el Dashboard pero el móvil está en Modos 0 o 1 (sin grabar a disco en ese instante), `AudioSentinel` es capaz de arrancar un codec "fantasma" que devora batería *exclusivamente* mientras haya oyentes conectados, apagándolo automáticamente cuando el Frontend cierra la conexión (por TCP pipe rotura).

### 🎓 Lecciones Aprendidas
1.  **Indentaciones Asesinas**: Migrar grandes bloques lógicos monolíticos (`AudioSentinel.runSentinel()`) con dependencias ramificadas generó errores de compilación por falta de actualización del cache al leer las Settings en bucle. Es imperativo limpiar completamente la *baseline* de las variables antiguas (`detectionEnabled` vs `recordingMode`) antes de soltar la lógica nueva en crudo sobre el buffer viejo.
2.  **Web Audio API overkill**: Muchas veces intentamos reinventar la rueda por JavaScript para decodificar audios en crudo (PCM -> Float32Array). Si el origen inyecta envoltorios estándares como ADTS + AAC, un tag html5 estático de 1 línea puede hacerlo mejor, gastando un décimo de energía de renderización en Chrome.

### ❌ Intento Fallido (v1.0-dev.28): Colapso por Compresión (GZIP)
Durante la validación en vivo de la transmisión ADTS nativa mediante Chrome, el servidor HTTP (NanoHTTPD) sufrió un Kernel Panic de su Pool de Hilos asíncronos induciendo la caída de la Telemetría (Error de Red en el Backend) y de la interfaz ADB al colapsar el Xiaomi.
*   **Motivo**: NanoHTTPD detecta automáticamente las peticiones de los navegadores comerciales (`Accept-Encoding: gzip`) y envuelve de forma ciega todo el tráfico bajo un `GZIPOutputStream`.
*   **Problema Anatómico**: Al envolver un *Stream Infinito* y tratar de comprimirlo al vuelo en formato `.gz`, destrozaba las tramas vitales de inicio ADTS; y lo que es peor, no lograba finalizar la cabecera comprimida, por lo que el navegador se colgaba intentando descodificar, asfixiando todos los Data Sockets de la API.
*   **Solución Quirúrgica (v1.0-dev.29)**: La instrucción estándar `r.setGzipEncoding(false)` fue ignorada por la versión 2.3.1. Se aplicó una **Inyección por Reflexión Java** (`reflect.Field`) en el Endpoint `/api/stream` de `WebServer.java` para vulnerar el acceso privado de la clase `Response` y forzar `encodeAsGzip = false`. Esto destrabó el cuello de botella dejando salir libremente el torrente *MPEG-A AAC*, sin asfixiar la Telemetría de la Interfaz Web.
*   **Lecciones Aprendidas 🎓**: Nunca confíes en la magia automática de las librerías Web en sistemas embebidos. Si un Stream es infinito, la compresión *Lossless* en capa 7 (HTTP) es un veneno letal. Reflexión en Java es un bisturí peligroso, pero ideal para desarmar librerías testarudas.

### ❌ Intento Fallido (v1.0-dev.29): Mutismo Nativo en Chrome y Safari (El Triple Fallo)
Tras habilitar la inyección del flujo AAC desnudándolo del GZIP, el navegador recibía pacíficamente los datos con `Transfer-Encoding: chunked`. Sin embargo, los reproductores `<audio>` de iOS y Chrome permanecían estancados en el segundo 0 (`readyState = 0`) sin emitir estática, error ni ruido alguno. La auditoría exhaustiva reveló un triple fallo concatenado letal:
1.  **Buffer Overflow por Bloqueo Ineficiente**: En el Modo de Reposo (Standby), el `AudioSentinel` entraba en un `Thread.sleep(1000)` saltándose la lectura bloqueante `audioRecord.read()`. Al no succionar la caché acústica del procesador, el subsistema de Audio de Xiaomi (MIUI) causaba un *Overflow* inmediato, enloqueciendo e imposibilitando capturar una sola trama sana posterior cuando los escuchas entraban al vuelo. Se solucionó permitiendo que la propia lectura nativa (`read()`) operase como Sleeper con coste de CPU cero, purgando la basura contínuamente.
2.  **Cabeceras Erróneas (MPEG-2 vs MPEG-4)**: Confiábamos en una Inyección ADTS con formato `0xF9` (perfil MPEG-2). Pero, en rigor, nuestro códec forjaba buffers en **MPEG-4** (`0xF1`). Safari, siendo draconiano, expulsaba directamente la mezcla de datos al ver la asimetría de diccionarios. Se rectificó cambiando el byte base a `0xF1`.
3.  **El Veneno CSD y la Muerte Arterial**: Cada vez que el códec hardware se iniciaba arranca escupiendo un flag `BUFFER_FLAG_CODEC_CONFIG` (CSD) de 2 bits ajeno al audio. Al envolver ese escombro con una cabecera ADTS completa, el primer paquete entregado al navegador estaba flagrantemente corrupto. Chromium lo bloqueaba por seguridad aduciendo a un stream insalubre. Se añadió lógica filtradora para ignorar los flags CSD, pues AAC-ADTS es auto-descriptivo frame a frame.
4. **Desconexión Arterial PipedOutputStream**: Por un efecto secundario de mis reestructuraciones pasadas en `WebServer.java` (durante la purga de GZIP), la línea clave `sentinel.addLiveListener(pipedOutputStream);` había sido borrada accidentalmente. El servidor web abría la conexión al visitante pero el Sentinel jamás se enteraba ni encendía su *Phantom Codec*. Fue restaurada y fortificada inyectando `KEY_MAX_INPUT_SIZE` al formateador de compresión en vuelo.

## 🚀 Rediseño de UI y Ajustes de Scroll en Modal v1.0-dev.31 | 23-Feb-2026
### 📜 El Problema
El panel de control "Modos de Grabación" ("Reposo Absoluto", "Vigilancia", "Continuo") era ambiguo y rígido. Además, al abrir la nueva ventana modal de "Ajustes del Centinela", se solapaba con las barras de navegación de los navegadores móviles (Safari/Chrome) y presentaba el infame bug de "Scroll Bleeding" sumado a botones inaccesibles por culpa del `100vh`.

### 🛠️ La Solución
1. **Rediseño Táctico de Interfaz**: Se reemplazó el Select de modos por un Gran Botón Maestro ("⏺️ GRABAR AHORA") en el dashboard.
2. **Interruptores Reactivos**: La activación del micrófono ("Vigilancia Activa") y el "Filtro Anti-Falsas Alarmas" se movieron al Modal de Ajustes convertidos en Toggle Switches (estilo iOS).
3. **Cronómetro en Vivo**: El Backend Java ahora emite el `recordingStartTimestamp`, permitiendo al Frontend JS inyectar un contador en tiempo real sobre el botón de grabación continua.
4. **Erradicación del Scroll Bleeding**: Se inyectó dinámicamente en Javascript una clase `.modal-open` con `overflow: hidden;` al `<body>` al invocar el modal, petrificando el fondo temporalmente.
5. **Evasión de Barras Nativas (75vh)**: Se suplantaron los offsets matemáticos por un acotado `max-height: 75vh` en el Modal, combinado con paddings sobredimensionados abajo (`60px`) y arriba, además de un botón de Cierre `&times;` incrustado mediante `flexbox` en la cabecera.

### 🎓 Lecciones Aprendidas
- El parámetro absoluto `100vh` en CSS Web Móvil es defectuoso por diseño (ignora las barras de UI inferiores y superiores del navegador dinámico). Sustituirlo por porcentajes relativos blindados (`75vh`) elimina los estancamientos de scroll en las capas flotantes de las Single Page Applications IoT.
- Anclar listeners de CSS classes dinámicas al bloque `body` es el anti-patrón de scroll nativo más liviano y fiable para modales *Full-Screen*.

## 🚀 Reproductor Forense WebAudio Avanzado v1.0-dev.32 | 23-Feb-2026
### 📜 El Problema
El Historial de Alertas listaba los eventos mediante etiquetas nativas HTML5 `<audio>`, lo que forzaba descargas opacas en el navegador y no permitía la auditoría visual de la amplitud (picos de decibelios) para localizar rápidamente las anomalías acústicas sin tener que escuchar la pista entera de forma lineal.

### 🛠️ La Solución
1. **Delegación de Carga al Cliente (Opción 1)**: Para no colapsar la RAM de Xiaomi calculando ondas, se construyó un `<canvas>` en `index.html`. El Mac/iPhone se encarga de descargar la pista vía `fetch()`, usar el chip propio mediante `AudioContext().decodeAudioData()` y extraer numéricamente los miles de picos PCM.
2. **Interfaz de Waveform (Modal)**: Los audios nativos se suprimieron a favor del botón hipervínculo gigante "👁️ Analizar Pista Auditiva". Este gatillo levanta una Modal de pantalla completa oscura donde se inyecta el Canvas.
3. **Reproductividad Táctil (Seeking)**: El Canvas reacciona a los clics evaluando al X en pantalla (`e.clientX`) vs la Anchura del Rectángulo, disparando una macro interna que redirige el cabezal `waveAudioSource.start(0, ratio * waveAudioBuffer.duration)`.
4. **Amplificación de Falsos Silencios**: Una grabación casi silenciosa dibuja picos minúsculos. Se inyectó una magnificación matemática de rango logarítmico `(max - min) * 5.0` con un `Math.max(1, ...)` para que la onda siempre levante 1px, resultando en un rastro visible para silencios y montañas rojas/verdes gigantes para ruidos estridentes.
5. **Cabezal No Invasivo**: El puntero de avance de `playHeadX` se dividió en dos estacas de longitud 10px (Superior e Inferior) en vez de cruzar verticalmente los 100px ahogando u ocultando el dibujo de la onda original.

### 🎓 Lecciones Aprendidas
- **Canvas y Variables Nativas CSS**: La declaración global estricta de `ctx.fillStyle` no traduce directamente de selectores `var(--color)` extraídos del DOM. Forzar Hexadecimales directos (`#4caf50`) evadió un bug masivo de renderizado Blanco puro persistente a pesar de estar la onda calculada correctamente en memoria.

## 🚀 Navegación Fluida de Onda: Drag-to-Seek v1.0-dev.33 | 23-Feb-2026
### 📜 El Problema
Aunque la v32 permitía saltar en el tiempo haciendo clic, la experiencia de usuario era rígida. En dispositivos móviles (Xiaomi/iPhone), el usuario espera poder arrastrar el cabezal de forma fluida (Scrubbing) para inspeccionar visualmente la onda mientras busca un punto exacto sin tener que soltar el dedo.

### 🛠️ La Solución
1. **Máquina de Estados de Interacción**: Se implementó la variable `isDragging` para gestionar el ciclo de vida del gesto (Pulsar -> Arrastrar -> Soltar).
2. **Soporte Híbrido Ratón/Táctil**: Se inyectaron Event Listeners específicos:
    - **Escritorio**: `mousedown`, `mousemove`, `mouseup`, `mouseleave`.
    - **Móvil**: `touchstart`, `touchmove`, `touchend`.
3. **Optimización de Renderizado (Ghost-Scrubbing)**: Durante el movimiento (`mousemove`/`touchmove`), el sistema solo actualiza el valor de `waveCurrentTime` y redibuja el Canvas, pero NO reinicia el `AudioContext`. El salto real del motor de audio (operación costosa) solo se ejecuta en el evento `mouseup` o `touchend`, garantizando una fluidez de 60 FPS durante el arrastre.
4. **Prevención de Scroll Nativo**: Se usó `e.preventDefault()` en el evento `touchmove` del Canvas para evitar que el navegador intente hacer scroll en la página mientras el usuario está deslizando el dedo lateralmente por la onda.

### 🎓 Lecciones Aprendidas
- **Interacciones Táctiles vs Mouse**: La API de Touch (`e.touches[0].clientX`) difiere de la de Mouse (`e.clientX`). Crear una función agnóstica de normalización de coordenadas es vital para proyectos multiplataforma.
- **Debouncing de AudioContext**: Reiniciar una fuente de audio (`bufferSource`) en cada evento de movimiento de ratón genera clics auditivos y saturación de memoria. La técnica de "Actualización Visual Continua + Salto de Audio al Soltar" es el estándar de oro para reproductores eficientes.

## 🚀 Persistencia de Estado en Scrubbing v1.0-dev.34 | 23-Feb-2026
### 📜 El Problema
Al arrastrar el dedo sobre la onda (v33), el sistema pausaba el audio para permitir el movimiento fluido. Sin embargo, al soltar el dedo, la aplicación "olvidaba" si el usuario estaba en modo Play antes de iniciar el arrastre, obligándole a pulsar el botón de Play manualmente cada vez.

### 🛠️ La Solución
1. **Delegación de Responsabilidad**: Se extrajo la creación del `BufferSource` a la función `playFromWaveTime(time)`.
2. **Memoria de Estado**: El sistema ya no resetea `isWavePlaying` a `false` durante el arrastre. Al disparar el evento `mouseup/touchend`, si `isWavePlaying` es verdadero, se invoca inmediatamente `playFromWaveTime`.
3. **Blindaje de Eventos**: Se añadió una guardia `!isDragging` en el callback `onended`. Esto evita que la llamada manual a `stop()` (necesaria para mover el cabezal) sea interpretada erróneamente por el navegador como el "fin del audio", lo que reseteaba la UI de forma prematura.

### 🎓 Lecciones Aprendidas
- En la Web Audio API, los eventos `onended` se disparan tanto por el fin natural del buffer como por una llamada manual a `stop()`. Distinguir estas dos causas mediante una bandera de estado (`isDragging`) es crítico para mantener una interfaz reactiva y predecible.

## 🚀 Estabilización de Onda y Telemetría v1.0-dev.35 | 23-Feb-2026
### 📜 El Problema
Aunque la v34 corregía la persistencia, el cabezal seguía comportándose de forma errática al arrastrarlo mientras el audio estaba en "PLAY". Esto se debía a que el bucle de animación visual seguía calculando la posición según el reloj antiguo, compitiendo violentamente con el movimiento del dedo del usuario. Además, se detectaron errores `null pointer` en la telemetría del dashboard al solapar el modal.

### 🛠️ La Solución
1. **Prioridad de Usuario (Scrubbing-First)**: Se inyectó una guardia en `updateWaveformAnim` que detiene la actualización del reloj si `isDragging` es verdadero. El dibujo ahora obedece exclusivamente al desplazamiento táctil hasta que se suelta el dedo.
2. **Saneamiento de Consola**: Se rediseñó `updateDashboard` con comprobaciones de nulidad estrictas y un `return` preventivo si los elementos del dashboard no son accesibles, eliminando el ruido de errores en las herramientas de desarrollador.

### 🎓 Lecciones Aprendidas
- **Interacción vs Animación**: En interfaces de alto rendimiento, los bucles de `requestAnimationFrame` deben estar subordinados a las banderas de interacción. Forzar la actualización visual manual durante el arrastre es la única forma de evitar el "ghosting" o los saltos de cabezal.
- **Robustez de Telemetría**: La arquitectura de un dashboard web debe ser tolerante a la ausencia temporal de elementos visuales (modales, cambios de vista), especialmente en ciclos de polling agresivo.

## 🚀 Rescate del Dashboard Mudo v1.0-dev.36 | 23-Feb-2026
### 📜 El Problema
Tras la v35, el Dashboard dejó de mostrar telemetría de batería, temperatura y actividad del micrófono. El General sospechó que el servidor estaba "dormido", pero la realidad era que el cliente estaba "paralizado" por mi propia guardia de seguridad: al no encontrar el ID `statusBadge` (que se había perdido en un refactor previo), el script ejecutaba un `return` preventivo antes de siquiera realizar el `fetch`.

### 🛠️ La Solución
1. **Restauración Anatómica**: Se ha vuelto a inyectar el div `#statusBadge` en el corazón del HTML del dashboard.
2. **Filosofía Tolerante a Fallos**: Se ha refactorizado `updateDashboard` para que, en lugar de abortar la misión (`return`), simplemente marque una bandera `hasDashboard` y proceda con el `fetch`. La actualización de los elementos visuales ahora está protegida individualmente, permitiendo que el resto del sistema siga vivo aunque falte una pieza.

### 🎓 Lecciones Aprendidas
- **Las Guardias de Seguridad son Espadas de Doble Filo**: Un `return` agresivo puede proteger contra un crash, pero puede "matar" el sistema si la pieza que falta es secundaria. Siempre es mejor fallar de forma elegante (graceful degradation) que detener el motor por completo.
- **Verificación de DOM**: Los IDs son contratos sagrados entre el HTML y el JS. Romper uno es romper el contrato de comunicación del sistema.

## 🚀 Sincronización de Preferencias v1.0-dev.37 | 23-Feb-2026
### 📜 El Problema
El General detectó que al pasar de "Micrófono Apagado" a "Activo" en los ajustes y guardar, la pantalla principal seguía mostrando "MICRÓFONO APAGADO" durante un intervalo o de forma indefinida. La lógica de "Vigilando" estaba incorrectamente anidada dentro del estado de "Grabando Alarma", lo que impedía que se mostrara en el estado de reposo inicial.

### 🛠️ La Solución
1. **Máquina de Estados de 4 Vías**: Se ha rediseñado el flujo `if/else` en `index.html` para que los estados sean mutuamente excluyentes y jerárquicos:
    - **Nivel 0**: Kill Switch (Micro OFF).
    - **Nivel 1**: Forzado Manual (REC Continuo).
    - **Nivel 2**: Detección Activa (Grabando Alarma).
    - **Nivel 3 (Default)**: Vigilancia Pasiva (Reposo Activo).
2. **Refresco Instantáneo**: Se ha asegurado que la llamada a `updateDashboard()` tras el `POST` de ajustes sea efectiva al estar ahora los estados correctamente mapeados.

### 🎓 Lecciones Aprendidas
- **Anidamiento Peligroso**: Evitar meter lógica de estado base dentro de condicionales de excepción (como una grabación en curso). El estado base debe ser el `else` final o el punto de entrada principal.
- **Resiliencia de UI**: Una UI que no reacciona al "Guardar" genera desconfianza en el usuario aunque el backend esté haciendo su trabajo. La reactividad es parte de la corrección funcional.

## 🚀 Dicotomía de Mando v1.0-dev.38 | 23-Feb-2026
### 📜 El Problema
El General planteó un dilema ético y técnico: ¿Debe funcionar la escucha en vivo si el sistema de vigilancia está apagado? La respuesta corta fue "No". Si el usuario apaga el sistema por privacidad (Kill Switch), nada debe salir del móvil. Sin embargo, surge la necesidad de monitorizar sin llenar el disco de alertas automáticas.

### 🛠️ La Solución
1. **Doble Mando V38**: Se ha implementado una jerarquía de dos niveles:
    - **Master (Hardware Micrófono)**: Si se apaga, el servidor mata el `AudioRecord` y el `MediaCodec`. El flujo de datos es CERO. El Dashboard bloquea el botón de escucha en vivo.
    - **Sub-lógica (Detección Automática)**: Si está OFF pero el Master está ON, el audio fluye (Escucha en Vivo) pero el motor de picos ignora los ruidos.
2. **Badge de Monitorización**: Se ha creado un estado visual intermedio "Sólo Escucha (Detección OFF)" con colores de advertencia (Amarillo/Negro) para indicar que el micro está "caliente" pero no "vigilante".

### 🎓 Lecciones Aprendidas
- **Metáfora del Grifo**: En sistemas de vigilancia, siempre debe haber una "Llave Maestra" que el usuario identifique como fuente única de verdad para su privacidad.
- **Dicotomía de Control**: Separar Hardware de Software permite casos de uso híbridos (Escucha pura) que antes eran imposibles por estar las lógicas acopladas.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V38) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.38 | ✅ |
| 5. Bloqueo Hardware (Privacidad) | ✅ |
| 6. Modo "Sólo Escucha" Funcional | ✅ |
| 7. Sync Push GitHub | ✅ |

## 🚀 Refinio Semántico y Cronómetro v1.0-dev.40 | 23-Feb-2026
### 📜 El Problema
El General detectó dos fricciones en la UI:
1. La terminología "Picos Directos" era técnica y poco descriptiva.
2. El cronómetro implementado en la v39.1 fallaba, mostrando `0:00:00` en grabaciones automáticas por culpa de un fallo de asignación en el bucle principal de Java.

### 🛠️ La Solución
1. **Literalidad Militar**: Se han adoptado los términos exactos: "**VIGILANDO (DETECTANDO SONIDO)**" y "**GRABANDO SONIDO DETECTADO**". Además, el botón principal ahora refleja el estado del sistema incluso en reposo, reforzando el concepto de "Vigilancia".
2. **Sincronización de Tiempo (V39.1 Core)**: Se ha corregido `AudioSentinel.java` para que asigne el `recordingStartTimestamp` en el mismo milisegundo en que se dispara el trigger de audio, unificando la telemetría para grabaciones manuales y automáticas.

### 🎓 Lecciones Aprendidas
- **El "0" es el enemigo**: Ver un contador estático en una situación de "alarma" genera ansiedad técnica. La telemetría de tiempo debe ser lo más robusta y redundante posible.
- **Botón Espejo**: En una SPA (Single Page Application) de control, el botón principal no debe ser solo un disparador, sino un indicador de estado vivo.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V40) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.40 | ✅ |
| 5. Cronómetro Auto-Detección OK | ✅ |
| 6. Semántica "Detectando Sonido" | ✅ |
| 7. Clean Build & Cache Purge | ✅ |

## 🚀 Fix v1.0-dev.41: Scrubbing Estabilizado (Audio Muerde Audio) | 23-Feb-2026
### 📜 El Problema
Al usar los botones +5s y -5s en el reproductor de ondas mientras la pista estaba sonando ("PLAY" activo), el código en `index.html` asfixiaba el motor HTML5 `AudioContext`. El cabezal se desplazaba, pero el estado interno del `BufferSource` viejo se encadenaba con el nuevo al no purgar el callback `onended`, generando inconsistencias visuales y parpadeo en el botón Play/Pause.

### 🛠️ La Solución
1. **Muerte Silenciosa (Orphan Callback)**: Se alteró el método `setWaveformTime(newTime)`. Antes de ejecutar `waveAudioSource.stop()`, ahora inyectamos proactivamente `waveAudioSource.onended = null;`.
2. **Centralización del Playback**: En lugar de repetir manualmente la creación del `BufferSource`, reconducimos la lógica de re-ignición directamente hacia `playFromWaveTime(waveCurrentTime)`, reciclando el código robusto probado en la V34.

### 🎓 Lecciones Aprendidas
- **Efecto Dominó en Asincronía**: Cortar por la fuerza un `stop()` en la Web Audio API desencadena instintivamente su evento `onended`. Si la UI confía ciegamente en ese evento para alterar su estado visual (cambiar a "Play" o resetear el cabezal al final), un salto manual introducido por el usuario estallará el diseño. La decapitación preventiva (`onended = null`) es el antídoto.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V41) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.41 | ✅ |
| 5. Muerte Silenciosa de `onended` | ✅ |

## 🚀 Hotfix v1.0-dev.42: Silenciamiento Físico `killCurrentAudio` | 23-Feb-2026
### 📜 El Problema
Al presionar los botones de +5s y -5s durante la reproducción de la onda, el cabezal visual se desplazaba correctamente a la nueva posición temporal (e.g. 15s), pero el audio que se escuchaba seguía siendo el que estaba por debajo (10s) de forma continua. El método `waveAudioSource.stop()` estándar no estaba consiguiendo desenganchar el motor de manera fiable, dejando una "pista fantasma" sonando mientras la nueva pista se ignoraba o colisionaba en silencio.

### 🛠️ La Solución
1. **Arma de Destrucción Masiva `killCurrentAudio`**: Se ha sustituido el débil bloque de `stop()` condicional por una función unificada y despiadada. Ahora, cualquier cambio de estado (Pausa, Scrubbing, o Saltos +/-) invoca un protocolo de extirpación garantizada:
   - Resetea el callback `onended` a `null`.
   - Lanza un `stop(0)` estricto (inmediato) envuelto en un `try-catch`.
   - Lanza un `disconnect()` físico para desenchufar el nodo del `audioDestination` del Hardware.
   - Destruye la variable en memoria `waveAudioSource = null`.
2. **Defensa Anticipada**: Se inyectó la llamada a `killCurrentAudio()` al principio exacto de `playFromWaveTime()`, asegurando que es matemáticamente imposible que dos fuentes intenten nacer o superponerse, incluso si un evento Asíncrono o táctil intentara lanzar dos playbacks simultáneamente.
3. **Loop de Animación Seguro**: Se capturó correctamente el ID de la animación `waveAnimationId = requestAnimationFrame(...)` tras presionar PLAY, para que `cancelAnimationFrame` obre su magia al pausar.

### 🎓 Lecciones Aprendidas
- **Desconexión Física vs Parada Lógica**: En Web Audio API, confiar únicamente en `.stop()` es arriesgado cuando se realizan manipulaciones algorítmicas de tiempo en milisegundos. Arrancar físicamente el nodo del gráfico de sonido usando `.disconnect()` es la única bala de plata (`Silver Bullet`) contra los *Ghost Nodes* o fallos silentes de reproducción solapada de WebKit.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V42) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.42 | ✅ |
| 5. Aniquilación de Nodo Fantasma | ✅ |

## 🚀 Decision v1.0-dev.46: Rollback Architectónico de Túneles (Go vs Android) | 24-Feb-2026
### 📜 El Problema
A lo largo de las versiones v43, v44 y v45, intentamos integrar el binario nativo oficial en C/Go `cloudflared` dentro de los *assets* del código base de Android para levantar una vía remota Zero Trust encapsulada. 
A pesar de librar con éxito dos batallas faraónicas (Encontrar un binario compatible con la JVM y saltar las restricciones W^X de SELinux en Android 10 mediante la extracción forzada por JNI de `libcloudflared.so`), el proceso moría al instante tras iniciar con `Connection Refused` sobre puertos UDP/53.

### 🛠️ La Solución (Retirada Táctica)
La investigación determinó que la red subyacente de Golang (lenguaje en el que está escrito Cloudflare) asume la existencia de la configuración clásica de Linux `/etc/resolv.conf` para inicializar sus *resolvers* de DNS (`1.1.1.1` u `8.8.8.8`). **Android no utiliza `/etc/resolv.conf`, sino que la resolución de red pasa por su propio demonio interno protegido (`netd`)**.
Por tanto, el contenedor del túnel estaba "ciego" y el proceso terminaba abruptamente. Para mantener la base de código estable, las integraciones Cloudflare han sido movidas a la rama paralela paralela aislada `experiment/cloudflare` para análisis forense, y `main` se ha revertido forzosamente y limpiado a su estado puro (v42 → v46).

### 🎓 Lecciones Aprendidas
- **La Ceguera de Go en la Máquina Virtual Dalvik**: Cualquier binario de Golang importado "en crudo" a Android que requiera una salida al mundo exterior de Internet (TCP/UDP) se estrellará internamente contra el Muro de Piedra del DNS, salvo que tenga *flags* o código inyectado específicamente diseñado para conectarse explícitamente a un DNS por Socket puro eludiendo el estándar Linux base. El hardware físico en Android no obedece al POSIX de GNU/Linux normal.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V46) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.46 | ✅ |
| 5. Rollback Purificado | ✅ |

## 🚀 UX Feature v1.0-dev.47: Tarjetas Inteligentes (Visitada + Highlight) | 24-Feb-2026
### 📜 El Problema
El historial de alertas del Dashboard Web tenía todas las tarjetas visualmente idénticas (fondo gris `#2c2c2c`). Tras revisar varias alertas, el operador no podía distinguir cuáles ya había analizado y cuáles eran nuevas. Además, no existía forma de "marcar" una tarjeta como interesante para revisarla después sin abrir un bloc de notas externo.

### 🛠️ La Solución
Dos intervenciones quirúrgicas en el CSS y JavaScript de `index.html`:

1. **Estado "Visitada" (Azul Medianoche)**: Al abrir el waveform de una tarjeta mediante `openWaveform()`, se guarda el nombre del archivo en `sessionStorage` y se aplica la clase CSS `.visited`. El fondo muta a un azul oscuro profundo (`#1a2a3a`) con borde lateral azul suave (`#4a90d9`), transmitiendo visualmente "ya revisado". Al recargar el historial con `loadHistory()`, cada tarjeta consulta `sessionStorage` para restaurar su estado visual.

2. **Highlight por Long-Press (Ámbar/Dorado)**: Se registran listeners `touchstart`/`touchend` (móvil) y `mousedown`/`mouseup` (desktop) en cada tarjeta. Un `setTimeout` de 600ms detecta la pulsación prolongada y hace toggle de la clase `.highlighted`. El fondo se ilumina en ámbar cálido (`#3a2f1a`) con borde dorado (`#f5a623`) y un resplandor sutil (`box-shadow: 0 0 12px rgba(245, 166, 35, 0.25)`). El estado es efímero (solo in-memory).

### 🎓 Lecciones Aprendidas
- **`sessionStorage` vs `localStorage`**: Para estados de "sesión de revisión", `sessionStorage` es el punto óptimo: sobrevive a navegación interna (F5) pero muere al cerrar la pestaña, evitando acumulación de datos obsoletos en dispositivos IoT de almacenamiento limitado.
- **Detección de Long-Press sin librerías**: La combinación `touchstart` + `setTimeout` + cancelación en `touchmove` es el patrón estándar para detectar pulsaciones largas en Vanilla JS sin arrastrar dependencias de Hammer.js o similares.
- **Paleta Intencional**: Azul medianoche para "procesado" (frío, neutro) y Ámbar para "destacado" (cálido, urgente) siguen las convenciones universales de semáforo visual que el cerebro humano procesa instintivamente.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V47) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.47 | ⬜ |
| 5. CSS Visitada Funcional | ✅ |
| 6. Long-Press Highlight Toggle | ✅ |

## 🚀 UX Feature v1.0-dev.48: Duración de Audio en Tarjetas | 24-Feb-2026
### 📜 El Problema
Las tarjetas del historial mostraban la fecha y el tamaño del archivo, pero no la duración del audio. El operador no podía saber de un vistazo si una alerta era un ruido de 3 segundos o una grabación continua de 2 minutos.

### 🛠️ La Solución
1. **Backend (Java)**: En el endpoint `/api/recordings` de `WebServer.java`, se instancia un `MediaMetadataRetriever` por cada archivo para extraer `METADATA_KEY_DURATION`. El valor en milisegundos se envía como `durationMs` en el JSON. Cada extracción está envuelta en `try-catch` individual con `finally { mmr.release() }` para garantizar que un archivo corrupto no rompa el listado completo.
2. **Frontend (JS)**: En `loadHistory()` de `index.html`, se formatea `durationMs` a `MM:SS` y se muestra como `⏱️ 01:23 · 📁 45.2 KB` en la cabecera de la tarjeta.

### 🎓 Lecciones Aprendidas
- **`MediaMetadataRetriever` es económico**: A diferencia de decodificar el audio completo, MMR solo lee las cabeceras del contenedor (MP4/AAC), resultando en una operación de I/O mínimo por archivo.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V48) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.48 | ⬜ |
| 5. Duración en JSON Backend | ✅ |
| 6. Duración Visible en Tarjetas | ✅ |

## 🚀 Arquitectura V49: Patrón Chivato JSON (Mini Waveforms) | 24-Feb-2026
### 📜 El Problema
El reproductor forense (v32) calculaba la forma de onda descargando el archivo completo de audio y decodificándolo con `AudioContext.decodeAudioData()` **en el cliente**. Esto significaba que cada tarjeta del historial requería una descarga masiva (megas de audio) solo para mostrar una vista previa. Era imposible mostrar ondas en miniatura en la lista sin colapsar el ancho de banda y la RAM del Xiaomi.

### 🛠️ La Solución
Implementación de un patrón de "Chivato de Metadatos" que desacopla la captura de picos del Frontend:

1. **AudioSentinel.java (Captura)**: Durante cada ciclo de lectura del micrófono, si `isRecording == true`, el pico de amplitud (`int`) se añade a una `ArrayList<Integer>` llamada `wavePeaks`. Al finalizar la grabación (bloque de cierre de `fos` y `codec`), se serializa la lista **diezmada** (1 de cada 2 picos, loop `pi += 2`) como un archivo `.json` con el mismo nombre que el `.m4a`. Se usa `FileWriter` con `StringBuilder` manual para evitar la sobrecarga de `JSONArray` de Android. La lista se limpia con `wavePeaks.clear()` inmediatamente después.

2. **WebServer.java (Transporte)**: En el endpoint `/api/recordings`, dentro del bucle de listado, se comprueba si existe un archivo `.json` hermano de cada `.m4a`/`.aac`. Si existe, se lee con `BufferedReader`, se parsea como `JSONArray` y se inyecta en el objeto JSON bajo la clave `"peaks"`.

3. **index.html (Renderizado)**: Se inyecta un `<canvas>` de 40px de alto en la plantilla de cada tarjeta (oculto si no hay picos). Una nueva función `drawMiniWaveform(canvasId, peaks)` normaliza cada pico contra `32767`, calcula el paso horizontal (`step = width / peaks.length`) y dibuja barras verticales con `ctx.fillRect` en color teal (`#03dac6`). La función se invoca tras insertar la tarjeta en el DOM.

### 🎓 Lecciones Aprendidas
- **Diezmado (Downsampling)**: Guardar todos los picos sería redundante para una vista previa de 400px de ancho. Saltar 1 de cada 2 reduce el tamaño del JSON a la mitad sin pérdida visual perceptible.
- **Separación de Responsabilidades**: El móvil captura y guarda los metadatos (coste marginal de I/O al cerrar grabación). El navegador del Mac solo recibe un array de enteros y dibuja. Cero decodificación de audio en ningún lado.
- **Archivos `.json` huérfanos elegantes**: Si se borra el audio, el JSON queda huérfano pero no molesta (no aparece en el listado porque solo se filtran `.m4a`/`.aac`). El botón de purga también los limpiará si añadimos el filtro en el futuro.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V49) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.49 | ⬜ |
| 5. AudioSentinel wavePeaks | ✅ |
| 6. WebServer peaks injection | ✅ |
| 7. Frontend drawMiniWaveform | ✅ |

## 🚀 Refinamiento V50: Boost Visual de Mini Waveforms | 24-Feb-2026
### 📜 El Problema
La primera versión de `drawMiniWaveform()` (V49) normalizaba contra el valor absoluto teórico del PCM 16-bit (`32767`). En la práctica, las grabaciones domésticas rara vez superan los 10.000 de amplitud máxima, lo que producía ondas minúsculas y apenas visibles en el canvas de 40px.

### 🛠️ La Solución
Reescritura de la función con tres mejoras:
1. **Normalización Dinámica**: En lugar de dividir por `32767`, se busca el pico real máximo (`localMax = Math.max(...peaks)`) de la grabación concreta.
2. **Techo Visual Inteligente**: Se establece `visualCeiling = Math.max(localMax, 8000)`. Esto evita amplificar ruido blanco de grabaciones casi silenciosas mientras permite que grabaciones con volumen moderado usen todo el espacio vertical.
3. **Boost x1.5 + Centrado**: Se multiplica la altura normalizada por 1.5 (clipping a `height` si se excede) y se centra verticalmente la barra (estilo analizador de audio) en lugar de anclarla al suelo del canvas.

### 🎓 Lecciones Aprendidas
- **Normalización vs Acotación**: Normalizar contra un máximo teórico inalcanzable es un error de diseño visual clásico. El techo debe ser contextual (per-recording) para usar eficientemente los pixeles disponibles.

| Punto de Verificación | Estado |
| :--- | :--- |
| 1. Incremento de Versión (V50) | ✅ |
| 2. Actualización BITACORA.md | ✅ |
| 3. Actualización CHANGELOG.md | ✅ |
| 4. Commit v1.0-dev.50 | ⬜ |


