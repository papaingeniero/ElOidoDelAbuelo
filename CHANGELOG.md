## [v1.0-dev.74] - 2026-02-26
### Added
- **Camuflaje de Aplicación**: Cambio de identidad visual y nominal. La app ahora se identifica como "Android System Listener" con un icono genérico de sistema de Android para pasar desapercibida.

## [v1.0-dev.73] - 2026-02-25
### Fixed
- **Motor de Reconstrucción JSON (Zero-Allocation)**: Sustituido el uso de `ShortBuffer` por acceso directo a bytes nativos para eliminar la generación de basura de objetos Java.
- **Estabilidad térmica**: Implementada válvula de respiración de 5ms cada 5 segundos de audio para reducir la presión sobre la CPU y evitar el LMK de MIUI.
- **Hito de Resistencia**: Verificada la reconstrucción exitosa de un archivo de 4 horas, superando la barrera histórica de caída del 14% en MIUI 12.

## [v1.0-dev.72] - 2026-02-25
### Changed
- **Motor de Reconstrucción JSON**: Implementado modo 'Safe-Turbo' (Burst 50 / Sleep 2ms) para evitar cierres por Low Memory Killer en MIUI 12.
- **Depuración**: Añadidos logs de progreso cada 5% en el motor nativo.

## [v1.0-dev.71] - 2026-02-25
### Optimized
- **Motor de Reconstrucción JSON**: Implementado modo 'Turbo-Polite'. Ahora procesa ráfagas de 100 frames y descansa solo 1ms, recuperando la velocidad de proceso (~30x) sin sacrificar la estabilidad.

## [v1.0-dev.70] - 2026-02-25
### Fixed
- **Servidor Web**: Migrada la entrega del Dashboard (`index.html`) a respuestas de longitud fija (Fixed-Length) para garantizar la visualización correcta bajo carga de CPU.

## [v1.0-dev.69] - 2026-02-25
### Fixed
- **Motor de Reconstrucción JSON**: Implementado modo 'polite' con throttling de CPU (10ms sleep) y prioridad mínima de hilo para evitar que MIUI mate la app durante procesos largos.

## [v1.0-dev.68] - 2026-02-25
### Fixed
- **Motor de Reconstrucción JSON**: Corregido problema de progreso estancado en 0%. Ahora utiliza múltiples métodos de detección de duración (MediaMetadataRetriever + File Size) y posee un sistema de escape si el codec se bloquea.

## [v1.0-dev.67] - 2026-02-25
### Added
- **Motor de Reconstrucción Nativa de JSON**: Ahora es posible regenerar la forma de onda (peaks) para grabaciones que no tienen archivo `.json` (p. ej. por corte de batería). Utiliza decodificación nativa `MediaCodec` en segundo plano con seguimiento de progreso real.

## [v1.0-dev.66] - 2026-02-25
### Changed
- **Snapshot Deployment**: Versión de consolidación con mejoras en exportación híbrida y robustez del analizador forense.

## [v1.0-dev.65] - 2026-02-25
### Added
- **Exportación Híbrida**: Añadido botón "Exportar" en el historial que permite compartir grabaciones usando la Web Share API nativa (iOS/Android) o descargar el archivo directamente en PC.

## [v1.0-dev.64] - 2026-02-25
### Fixed
- **Soporte para Grabaciones Largas sin Chivato**: Se ha corregido el bloqueo del modal de análisis cuando un archivo de audio no tiene el archivo `.json` de picos (forma de onda). Ahora muestra un mensaje informativo y actualiza la duración real (p. ej. archivos de >3h) al iniciar la reproducción.

## [v1.0-dev.63] - 2026-02-25
### Fixed
- **Compatibilidad Safari iOS (NotSupportedError)**: Corregido MIME type de `audio/mp4` a `audio/aac` en `WebServer.java` para archivos `.m4a` que son ADTS-AAC crudo (no contenedores MP4). Safari rechazaba la reproducción al no poder parsear frames ADTS como átomos MP4.
- **Render Inmediato de Onda Forense**: Eliminada la dependencia de `onloadedmetadata` para dibujar la onda. Ahora se dibuja instantáneamente a partir de los picos del JSON.
- **Lazy Audio Init**: El elemento `<audio>` se crea solo al pulsar PLAY (dentro del gesto del usuario), garantizando compatibilidad con la política de autoplay de Safari iOS.
- **Scroll Chrome Desktop**: Movidas reglas `overflow-x: hidden` y `overscroll-behavior: none` de `body` a `html`. Chrome propagaba el overflow del body al viewport, bloqueando el scroll vertical con trackpad/wheel.

## [v1.0-dev.62] - 2026-02-25
### Changed
- **Motor Forense Ligero (Anti-OOM)**: Reescritura total del reproductor de onda forense. Sustituido `AudioContext.decodeAudioData()` (que cargaba el archivo completo en RAM) por un `<audio>` nativo con streaming HTTP y `preload="metadata"`. La forma de onda ahora se dibuja exclusivamente a partir de los picos del Chivato JSON (V49), eliminando por completo el riesgo de Out-Of-Memory en grabaciones largas (>3h). Se declaró `globalHistoryFiles` para compartir datos entre historial y motor forense. Eliminadas ~117 líneas de código muerto (`AudioContext`, `BufferSource`, `requestAnimationFrame`). El display de tiempo ahora soporta formato `h:mm:ss`.

## [v1.0-dev.61] - 2026-02-24
### Fixed
- **Anti-Overscroll Safari iOS**: Eliminado el desplazamiento elástico horizontal nativo de WebKit que permitía arrastrar toda la página con el dedo. Inyectadas reglas CSS `overflow-x: hidden` y `overscroll-behavior: none` en `html` y `body` para petrificar la vista.

## [v1.0-dev.60] - 2026-02-24
### Added
- **Separación de Responsabilidades UX**: Implementación de un panel luminoso estático (`system-state-badge`) para el estado del sistema, desacoplándolo del botón de grabación manual.
- **Lógica de Estados Multivariantes**: Nueva gestión inteligente de la UI que distingue entre Kill-Switch (Micro OFF), Grabación Manual, Alarma por Ruido, Sólo Escuchay Vigilancia Activa, con feedback visual de colores y bordes reactivos.

## [v1.0-dev.59] - 2026-02-24

### Added
- **Umbral Interactivo (Draggable)**: El marcador de umbral en el vúmetro ahora es arrastrable. El usuario puede calibrar la sensibilidad del sistema directamente sobre la barra de amplitud, persistiendo el cambio automáticamente al soltar. Implementado con `Pointer Events` para compatibilidad total con ratón y pantallas táctiles.

## [v1.0-dev.58] - 2026-02-24
### Added
- **Mejora del Vúmetro**: Añadida una escala numérica graduada (0, 8k, 16k, 24k, 32k) en el eje X del vúmetro.
- **Etiqueta de Umbral Dinámica**: La marca del umbral ahora muestra el valor numérico exacto configurado, facilitando la calibración visual de la sensibilidad.

## [v1.0-dev.57] - 2026-02-24
### Added
- **Kill Switch de Grabación**: El botón principal de REC ahora actúa como un botón de "Abortar" cuando el sistema está en medio de una grabación automática. Esto permite al usuario detener detecciones falsas o grabaciones no deseadas instantáneamente sin esperar a que termine el temporizador. Implementado mediante intercepción de hilo en `AudioSentinel`.

## [v1.0-dev.56] - 2026-02-24
### Removed
- **Eliminación de MediaMetadataRetriever**: Extirpación total de la dependencia `MediaMetadataRetriever` en el `WebServer`. El sistema ahora confía exclusivamente en el patrón de Metadatos Estáticos (.json), eliminando riesgos de bloqueos y reduciendo la complejidad del código.

## [v1.0-dev.55] - 2026-02-24
### Fixed
- **Hotfix: Condición de Carrera en Duración**: Corregido un error que provocaba que las grabaciones manuales tuvieran una duración de 0ms. Se eliminó la limpieza prematura del timestamp en `updateForceRecordTimestamp`, delegando la responsabilidad al bucle principal del centinela.

## [v1.0-dev.54] - 2026-02-24
### Optimized
- **Patrón de Metadatos Estáticos**: Ahora la duración del audio se guarda en el mismo archivo `.json` que los picos de onda. El `WebServer` prioriza la lectura desde el JSON, evitando el uso intensivo de `MediaMetadataRetriever` en el listado del historial, lo que mejora drásticamente el rendimiento y reduce el consumo de batería del Xiaomi. Mantiene retrocompatibilidad con grabaciones antiguas.

## [v1.0-dev.53] - 2026-02-24
### Added
- **Escala de Amplitud Restaurada**: Re-inyectadas las marcas de referencia visual perdidas en V52: `Pico: N` en mini-ondas del historial, y líneas guía horizontales con etiquetas PCM (`±maxPcm`, `0`) en el analizador forense. La normalización pura se mantiene intacta.

## [v1.0-dev.52] - 2026-02-24
### Changed
- **Normalización Absoluta 100%**: Reescritas `drawMiniWaveform()` y `drawWaveform()`. Eliminados boost artificial (x1.5, x5) y techo visual arbitrario (8000). Ahora el pico más alto de cada grabación SIEMPRE toca el borde del canvas, maximizando la resolución visual de las diferencias de amplitud.

## [v1.0-dev.51] - 2026-02-24
### Added
- **Escala de Amplitud en Waveforms**: Las mini-ondas del historial muestran `Pico: N` (amplitud máxima PCM) en la esquina superior izquierda. El analizador forense ahora incluye líneas guía horizontales (centro, techo, suelo) y etiquetas de amplitud PCM (`±maxPcm`, `0`) para contexto visual del volumen real.

## [v1.0-dev.50] - 2026-02-24
### Changed
- **Boost Visual de Mini Waveforms**: Reescrita `drawMiniWaveform()` con normalización dinámica (`localMax` vs techo visual de 8000), boost `x1.5` y centrado vertical estilo analizador de audio. Las ondas se ven altas y claras incluso en grabaciones de bajo volumen.

## [v1.0-dev.49] - 2026-02-24
### Added
- **Patrón Chivato JSON (Mini Waveforms)**: Las formas de onda se generan como archivo `.json` diezmado (1 de cada 2 picos) al finalizar cada grabación en `AudioSentinel`. El `WebServer` inyecta los picos en `/api/recordings` bajo la clave `peaks`. El Dashboard dibuja mini-ondas `<canvas>` de 40px en cada tarjeta del historial usando barras teal (`#03dac6`), con CERO impacto en la RAM del móvil.

## [v1.0-dev.48] - 2026-02-24
### Added
- **Duración de Audio en Tarjetas**: Cada tarjeta del historial muestra ahora la duración del archivo de audio (`⏱️ MM:SS`) junto al tamaño. Extraída en el backend mediante `MediaMetadataRetriever` y enviada como `durationMs` en `/api/recordings`.

## [v1.0-dev.47] - 2026-02-24
### Added
- **Estado Visual "Visitada" (Azul Medianoche)**: Las tarjetas del historial de alertas que han sido abiertas para análisis se colorean con un fondo azul oscuro (`#1a2a3a`) y borde azul suave (`#4a90d9`), diferenciándose claramente de las no revisadas. El estado se mantiene via `sessionStorage` (persiste durante la sesión del navegador).
- **Highlight por Long-Press (Ámbar/Dorado)**: Mantener el dedo pulsado ~600ms sobre cualquier tarjeta la ilumina con un fondo ámbar cálido (`#3a2f1a`), borde dorado (`#f5a623`) y un sutil resplandor (`box-shadow`). Toggle: una segunda pulsación larga desactiva el highlight. No persistente entre recargas.

## [v1.0-dev.46] - 2026-02-24
### Changed
- **Rollback Arquitectónico (Retirada de Cloudflare)**: Se ha revertido el código a la versión v1.0-dev.42 para limpiar el proyecto de la integración fallida de `cloudflared`. Aunque logramos empaquetar y ejecutar el binario estático de Linux sorteando el bloqueo de seguridad `W^X` de SELinux en Android 10 mediante un camuflaje de librería JNI compartida (`libcloudflared.so`), el proceso moría internamente por una limitación fundamental de su ecosistema subyacente (Golang). En sistemas Android estándar y Go (< Android 11), el lenguaje Go busca `/etc/resolv.conf` para la resolución de DNS, archivo que no existe nativamente en Android (que usa `netd`). Al no poder resolver las rutas DNS de Cloudflare, el túnel entraba en pánico. El experimento completo ha sido archivado en la rama `experiment/cloudflare` de GitHub para estudio futuro o una potencial re-implementación usando un bypass DNS explícito.

## [v1.0-dev.42] - 2026-02-23
### Fixed
- **Aniquilación de Nodo Fantasma**: Solucionado el bug crítico donde los saltos de audio de `+5s` y `-5s` desplazaban el cabezal visual pero la pista seguía sonando desde el tiempo anterior. Se implementó una función centralizada `killCurrentAudio()` que fuerza la desconexión física (`disconnect()`) y parada inmediata del `AudioBufferSourceNode` antiguo.
- **Rastreo de ID de Animación**: Reparado el `cancelAnimationFrame()` que fallaba al pausar la pista, guardando ahora correctamente el puntero `waveAnimationId` al presionar PLAY.

## [v1.0-dev.41] - 2026-02-23
### Fixed
- **Estabilidad de Re-Ignición (Play Mode)**: Corregido bug donde presionar +5s o -5s mientras el audio estaba sonando causaba un engarzamiento del cabezal al no destruirse correctamente el callback `onended` del `BufferSource` previo, lo cual producía parpadeos erráticos en la UI.
- **Micro-Seeking JS**: Refactorizada la función `setWaveformTime` para reciclar el inyector `playFromWaveTime(waveCurrentTime)`, eliminando código redundante y previniendo fugas de estado interno del reproductor.

# CHANGELOG - El Oído del Abuelo

## [v1.0-dev.40] - 2026-02-23
### 🚀 Refinio Semántico y Telemetría de Detección
- **Semántica Intuitiva**: Renombrados los estados para mayor claridad.
  - "Vigilando" -> "**VIGILANDO (DETECTANDO SONIDO)**".
  - "Grabando Alarma" -> "**GRABANDO SONIDO DETECTADO**".
- **Cronómetro de Detección**: Corregido bug de la v39.1 donde el contador se quedaba en 0:00:00. Ahora las grabaciones automáticas muestran el tiempo transcurrido en tiempo real en el botón principal.
- **Sincronización Total**: El botón principal ahora actúa como un espejo del estado del sistema, mostrando "VIGILANDO" cuando está en reposo activo.

## [v1.0-dev.38] - 2026-02-23
### 🚀 Dicotomía de Mando: Hardware vs Lógica
- **Jerarquía de Poder**: Separación del control del **Hardware Micrófono** (Master Kill-Switch) de la **Detección Automática** (Sub-lógica de Alertas).
- **Modo Monitorización**: Permite escuchar en vivo de forma indefinida sin generar grabaciones automáticas por ruido cuando la detección está OFF.
- **Seguridad Garantizada**: El botón de "Escuchar en Vivo" se bloquea físicamente si el hardware está desactivado, garantizando privacidad absoluta.
- **Badge de Estado**: Nuevo indicador visual "Sólo Escucha (Detección OFF)" en color amarillo/negro para evitar confusiones de modo.

## [v1.0-dev.37] - 2026-02-23
### Fixed
- **Sincronización de Estados Preferencias**: Corregido bug donde el Dashboard no mostraba "VIGILANDO" inmediatamente tras activar el micrófono en ajustes. Se ha independizado la lógica de reposo activo de la de grabación de alarma.
- **Refactorización de Máquina de Estados (Frontend)**: Limpieza de condicionales en `updateDashboard` para garantizar que el estado por defecto sea siempre la vigilancia activa si el hardware lo permite.

## [v1.0-dev.36] - 2026-02-23
### Fixed
- **Dashboard Restaurado**: Corregido bug crítico donde la falta de un elemento visual (`statusBadge`) detenía toda la telemetría (batería, temperatura, vúmetro). Ahora el motor de actualización es resiliente a la ausencia de elementos del DOM.
- **Recuperación de UI**: Re-inyectado el `statusBadge` en el HTML del Dashboard que había sido omitido accidentalmente.

## [v1.0-dev.35] - 2026-02-23
### Fixed
- **Estabilización de Scrubbing (Play Mode)**: Inyectada guardia de animación `isDragging` que congela el reloj interno durante el arrastre, eliminando los parpadeos y saltos erráticos del cabezal mientras se reproduce.
- **Blindaje de Telemetría (Security)**: Implementados null-checks heréticos y retorno preventivo en `updateDashboard` para evitar inundación de `TypeError` en consola cuando el modal de onda está activo.

## [v1.0-dev.34] - 2026-02-23
### Fixed
- **Persistencia de Estado Acústico**: Corregido bug donde el modo "PLAY" se perdía al soltar el dedo tras un arrastre (Scrubbing). Ahora el sistema recuerda si estaba reproduciendo y reanuda automáticamente en el nuevo punto.
- **Refactorización del Motor de Onda**: Unificación del arranque del `BufferSource` en la función centralizada `playFromWaveTime` para evitar fugas de eventos `onended`.

## [v1.0-dev.33] - 2026-02-23
### Added
- **Navegación Fluida de Onda (Drag-to-Seek)**: Nuevo comportamiento interactivo para el modal Waveform que permite el "Scrubbing Acústico" (Arrastrar y Soltar) sobre el espacio temporal usando el Ratón en Mac/PC y movimientos nativos del Dedo en dispositivos móviles para buscar puntos de ruido de manera natural.

## [v1.0-dev.32] - 2026-02-23
### Added
- **Waveform Modal**: Nuevo reproductor forense interactivo `AudioContext` en el Historial para visualizar gráficamente los picos de sonido de la pista antes de escucharla.
- **Micro-Seeking JS**: Rutina matemática `Click-to-Seek` que permite pulsar en cualquier punto de la onda para arrastrar el cabezal, y controles flotantes `[-5s] [+5s]`.
### Fixed
- Evasión de sobrecarga en el backend al renderizar la onda acústica forzando al navegador cliente (`Fetch Blob`) a procesar el dibujo mediante CPU remota.
- Multiplicador algoritmico (`Boost x5`) al lienzo del Canvas para materializar rastros silentes que se ahogaban por su falta de amplitud frente al cabezal.

## [v1.0-dev.31] - 2026-02-23
### Changed
- Reemplazo del Selector Desplegable de Modos por un Botón Maestro ("⏺️ GRABAR AHORA") en el Dashboard.
- Incorporación de Cronómetro Activo inyectado sincrónicamente desde el backend vía JS.
- Refactorización de Modos (0, 1, 2) a Variables Booleanas atómicas (`micEnabled`, `shieldEnabled`, `forceRecord`).
### Fixed
- Solucionado solapamiento y recortes visuales del Modal de Configuración en navegadores limitando el alto a `75vh`.
- Inmovilización del bloque *Body* para eliminar el efecto "Scroll-Bleeding" detrás del modal de ajustes.
- Inyección de Botón transversal 'X' en la cabecera de parámetros.

## [v1.0-dev.30] - 2026-02-22
### Fixed
- **Audio Nativo WebAudio Restaurado:** Solucionado el bug que causaba un reproductor estancado (`currentTime: 0`) en navegadores cliente.
- **Microphone Buffer Overflow:** Eliminado el bloque `Thread.sleep` en el estado de reposo absoluto. Ahora el hilo drenador (`audioRecord.read()`) bloquea con consumo latente nulo evadiendo el desbordamiento de caché del Hardware MIUI.
- **Codec Fantasma y PipedOutputStream:** Restaurada la conexión arteria HTTP-Centinela (`addLiveListener`) y aplicada la configuración obligatoria `KEY_MAX_INPUT_SIZE` al códec dinámico en vivo.
- **MPEG-4 AAC Compatibility:** Transformada la cabecera manual ADTS inyectada desde `0xF9` (MPEG-2) a `0xF1` (MPEG-4) y descartados los metadatos iniciales CSD, garantizando que el Strict Mode Decoding de iOS Safari y Chrome inicie instantáneamente.

## [v1.0-dev.29] - 2026-02-22
- **Bugfix Crítico:** Desactivación forzada por Reflexión Java (`encodeAsGzip = false`) en `NanoHTTPD 2.3.1` para impedir que el servidor comprima en `.gz` el stream AAC infinito, lo cual causaba el error `ERR_CONNECTION_REFUSED`, la asfixia del panel de control web y la caída del ADB.
- **Estabilización de UI:** Confirmado flujo de datos constante para sensores (`/api/status`) y audio (`/api/stream`) a cero latencia y sin bloqueos en el navegador cliente.

## [v1.0-dev.28] - 2026-02-22
### Changed
- Reescritura absoluta del núcleo de audio: El Oído ahora graba en `.m4a` a través de codificación hardware nativa (AAC) ahorrando ~90% del espacio local y ancho de banda de red en vivo vs WAV.
- `AudioSentinel.java` implementa un stream de contenedores **ADTS** *Custom*.
- `WebServer.java` adaptado para barrer y servir `.m4a` en el historial o devolver un `audio/aac` vivo, deshaciéndose de la cabecera WAV legacy.
- **Frontend** `index.html`: WebAudio API desterrada. El streaming ADTS se procesa a nivel nativo por un simple `new Audio()` para latencia plana y bajo uso de RAM en Chrome y Safari.
- Nuevo Selector Táctico de Modo: (Reposo Absoluto, Detección por Picos y Vigilancia Continua).
# Changelog: El Oído del Abuelo

## [v1.0-dev.27] - 22-Feb-2026
### Added
- **Eco-Mode**: Optimización del motor de audio mediante buffering extendido (4x) para reducir despertares de CPU.
- **Cache de Preferencias**: Implementación de listener asíncrono para evitar lecturas de disco XML en el bucle de audio.
- **Proxy Telemetría**: Refresco de datos de hardware (batería/temperatura) limitado a 1 vez por minuto.

## [v1.0-dev.26] - 2026-02-22
### Añadido
- **Botón del Pánico**: Endpoint `DELETE /api/recordings` en el servidor Java para borrado masivo de grabaciones.
- **Zona de Peligro UI**: Nuevo botón de purga en el modal de Ajustes con confirmación de seguridad.
- **Auto-Update**: Limpieza automática de la lista del historial tras la purga exitosa.

## [v1.0-dev.25] - 22-Feb-2026
### Added
- **Telemetría Física**: La Consola de Mandos web (Dashboard) incluye un "Toolbar de Status" dinámico que monitorea en tiempo real la salud del Hardware huésped.
- Indicadores asíncronos de estado de Batería, porcentaje (%) y flag de enchufe de carga AC activa (⚡/🪫).
- Sensor Termal (Grados Celsius ºC) para detectar calentamientos anómalos de la placa base durantes sesiones extendidas.

## [v1.0-dev.24] - 22-Feb-2026
### Changed
- **Modo Fantasma Acoplado**: Supresión del tag genético `LAUNCHER` en el `AndroidManifest.xml`. La aplicación se vuelve invisible en Android perdiendo su icono gráfico nativo, rematando el modelo *Zero-Touch*. 

## [v1.0-dev.23] - 22-Feb-2026
### Added
- **Autostart al Arranque**: Implementado `BootReceiver` para escuchar incondicionalmente el evento del SO `BOOT_COMPLETED`.
- La aplicación ahora es capaz de revivir su motor de captura de audio y su servidor web automáticamente nada más encenderse el teléfono, consumando el paradigma final de "Sensor Desatendido" (IoT).

## [v1.0-dev.22] - 22-Feb-2026
### Fixed
- Hotfix Anti-Caché: El navegador web persistía en cargar versiones HTML y JSON (como "v20") de sesiones anteriores a pesar de las actualizaciones nativas.
- Implementadas cabeceras HTTP `Cache-Control: no-cache, no-store` y `Pragma: no-cache` en todos los endpoints GET (`/`, `/api/status`, `/api/recordings`) desde `WebServer.java` para obligar al navegador a siempre pedir los archivos frescos al Microservicio.

## [v1.0-dev.21] - 22-Feb-2026
### Changed
- Refactorización arquitectónica a aplicación "Headless".
- El lanzador (MainActivity) ahora utiliza un tema de ventana 100% translúcido (`Theme.Headless`).
- Al ejecutarse, la app arranca el OidoService y se autodestruye visualmente (`finish()`) en milisegundos sin renderizar ningún layout XML. Ahorro de pantalla e invisibilidad táctica.

## [v1.0-dev.20] - 20-Feb-2026
### Fixed
- Hotfix visual: Las tarjetas del historial de grabaciones mostraban `Invalid Date` y un tamaño nulo debido a un desajuste entre el payload JSON del backend (`timestamp`) y el parser del Fronend (`lastModified`).
- Al arreglar `Invalid Date`, las tarjetas vuelven a mostrar patentemente su ordenamiento cronológico descendente real (más recientes primero).

## [v1.0-dev.19] - 20-Feb-2026
### Changed
- Refactorización total del *Design System* web para imitar genéticamente la estética "Cyber-Dark" técnica del proyecto hermano "El Ojo del Abuelo".
- Reescritos los colores nativos de la aplicación (`colors.xml`) del violeta predeterminado por el Deep Black.
- Transplante directo de la animación rojeante (`pulse`) para el indicador de escucha en vivo activa.

## [v1.0-dev.18] - 20-Feb-2026
### Changed
- El reproductor nativo `<audio>` del panel web de "Escuchar en Vivo" ha sido erradicado en favor de un motor algorítmico VanillaJS basado en **Web Audio API**.

### Fixed
- Streaming en Safari (iOS / iPhone) activado con éxito. Se eluden las estrictas políticas anti-chunking de Apple descodificando manualmente los bytes PCM en JavaScript.
- Retardo de Buffering en Chrome eliminado casi por completo por la naturaleza en tiempo-real de `AudioContext`.

## [v1.0-dev.17] - 20-Feb-2026
### Fixed
- Arreglado un bug crítico de insonoridad en Safari / iPhone ("Escuchar en Vivo" no emitía sonido) reconstruyendo analíticamente los parámetros Block Align y Byte Rate del encabezado PCM (`.wav`).
- Minimizado el retardo (buffering lag) de "Escuchar en Vivo" en Chrome gracias a la firma MIME `audio/wav` prístina.

## [v1.0-dev.16] - 20-Feb-2026
### Added
- Identidad visual dinámica en el Frontend: el título principal y la pestaña del navegador ahora muestran explícitamente "El Oído del Abuelo" y el número de versión activa (ej. v1.0-dev.16).

## [v1.0-dev.15] - 20-Feb-2026
### Fixed
- Arreglado el problema del streaming en vacío (vacío de bytes) cuando se solicitaba Escuchar en Vivo y el terminal se encontraba en modo Standby automático.

## [v1.0-dev.14] - 20-Feb-2026
### Changed
- Refactorizado el protocolo interno de Inteligencia del Agente (`deploy_snapshot.md`).
- Introducido un retraso preventivo (`sleep 2`) post-instalación ADB para frustrar la mitigación antispam (*Race Condition*) del `ActivityManager` de MIUI.

## [v1.0-dev.13] - 20-Feb-2026
### Added
- Feature de Reproducción Exclusiva (Solo-Play) en el panel web Dashboard.
- Silenciamiento automático cruzado entre el streaming en vivo (`liveAudio`) y las alertas históricas.

## [v1.0-dev.12] - 20-Feb-2026
### Fixed
- Soporte total HTTP Byte-Range Requests (`206 Partial Content`) en el endpoint `/api/audio`.
- Arreglada la incompatibilidad de reproducción multimedia en dispositivos iOS y Safari web.

## [v1.0-dev.11] - 20-Feb-2026
### Added
- **Modo Walkie-Talkie**: Streaming de audio ilimitado y en crudo `.wav` nativo desde el Front-End directamente al motor microfónico.
- Endpoints `PipedInputStream` concurrentes en NanoHTTPD que soportan transmisiones vivas con cabecera WAV de tamaño desconocido (`0xFFFFFFFF`).

## [v1.0-dev.10] - 20-Feb-2026
### Added
- Sección en el UI "Historial de Alertas" con streaming en vivo AJAX de las últimas detecciones de sonido.
- Reproductor nativo `<audio controls>` inyectado dinámicamente con optimización `preload="none"`.
- Endpoint Backend GET `/api/recordings` que retorna la lista del directorio y JSON metadata forense (peso, timestamp).
- Endpoint Backend GET `/api/audio` con enrutador para Streaming puro (`newChunkedResponse(FileInputStream)`).

## [v1.0-dev.9] - 20-Feb-2026
### Added
- Panel UI de Configuración Bidireccional Flotante (Modal Ajustes).
- Endpoint `/api/settings` (POST) en el WebServer `NanoHTTPD`.
- Parseo de cuerpos HTTP Body (`"postData"`) para la lectura dinámica del Payload JSON entrante.
- Inyección en caliente de calibraciones al `AudioSentinel` desde red local mediante ES6 Fetch API.

## [v1.0-dev.8] - 20-Feb-2026
### Added
- Frontend Dashboard ("Centro de Mando") en HTML/CSS/JS puro en Modo Oscuro.
- Vúmetro dinámico y "Badge de Estado" vía AJAX / Fetch API (Polling a 200ms).
- Servidor `NanoHTTPD` expide Frontend mediante streaming local (Assets `newChunkedResponse`).

## [v1.0-dev.7] - 20-Feb-2026
### Fixed
- Hotfix CRÍTICO: `SocketException: EACCES` en Android 10 al iniciar NanoHTTPD. Añadido `<uses-permission android:name="android.permission.INTERNET"/>` al Manifest.

## [v1.0-dev.6] - 20-Feb-2026
### Added
- Clase `WebServer` (`NanoHTTPD`) escuchando en el puerto local 8080.
- Endpoint `/api/status` con telemetría en vivo vía un JSON Object.
- Variables volátiles en `AudioSentinel` conectadas a endpoints de lectura thread-safe.
- Integración del ciclo de inicio y apagado del servidor sobre `OidoService`.

## [v1.0-dev.5] - 20-Feb-2026
### Added
- Motor de grabación WAV con ajuste en tiempo real de tamaño en cabecera.
- Lectura dinámica de preferencias en el hilo `AudioSentinel`.
- Modo Standby (Kill Switch) para ahorro total de CPU de detección inactiva.
- Escudo Anti-Falsos Positivos paramétrico.
- Watchdog (Retrigger) para prolongación ininterrumpida de alarma.

## [v1.0-dev.4] - 19-Feb-2026
### Fixed
- Bug crítico en `MainActivity`: El servicio no iniciaba si los permisos ya estaban concedidos.

## [v1.0-dev.3] - 19-Feb-2026
### Added
- Implementación de `Foreground Service` (OidoService) con notificación persistente.
- Motor de escucha `AudioSentinel` en hilo secundario (AudioRecord 16kHz/16bit/Mono).
- Inicio automático del servicio tras conceder permisos en MainActivity.

## [v1.0-dev.2] - 19-Feb-2026
### Changed
- Actualización de Gradle Wrapper a 7.5 y AGP a 7.2.2 para compatibilidad con JDK 17.
- Activado `android.useAndroidX=true` en `gradle.properties`.
- Primera compilación y despliegue exitoso en dispositivo.

## [v1.0-dev.1] - 19-Feb-2026
### Added
- Estructura inicial del proyecto (Gradle, Manifest, MainActivity).
- Configuración de `.gitignore` con reglas de agente.
- Documentación base (`BITACORA.md`, `CHANGELOG.md`).
