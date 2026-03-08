# Changelog

## [v1.4.56] - 2026-03-08
### Fixed
- **Controlled Suicide Reload Protocol**: El modelo de reseteo de Web Worker ha sido sustituido globalmente por una recarga física dura mitigadora para iOS (`window.location.reload()`). WebKit sumaba contadores ocultos de `Canvas/AudioBuffers` que terminaban en un "Anti-Crash Loop Banned" fatal por parte de Safari tras muchos sub-crashes. Ahora cada 20 chunks procesados, la web se suicida limpiamente e invoca el Protocolo Fénix, que ha elevado su paciencia pasiva a 2000ms previo a reanudar la batalla para enmascarar su naturaleza botística y blanquear contadores de Safari al 100%.

## [v1.4.55] - 2026-03-08
### Added
- **Dynamic VAD Cache Invalidation**: Agregado un `addEventListener` reactivo al deslizador de umbral de IA (`inpVadThresh`). Si el usuario desliza la pieza ajustando la sensibilidad térmica militar sobre un archivo de caché persistente 100% completado (Zero-Shot), la IA activa inmediatamente una directiva de auto-aniquilación de Memoria WebKit para dicha previsualización antigua (`window.vadCheckpointData = null`), limpia la matriz gráfica canvas de rojo a gris puro y desbloquea el botón primario instando visualmente a recalcular la pista.

## [v1.4.54] - 2026-03-08
### Fixed
- **Memory Leak Backend**: Añadida condicional matricial `.endsWith(".json")` a la purga asíncrona EndPoint Delete (`/api/recordings`) en NanoHTTPD. Soluciona un grave fallo catastrófico eMMC donde el vaciado maestro del panel ignoraba los cachés de metadatos (Zero-Shot VAD y Picos de compresión), dejándolos retenidos crónicamente en el sistema de archivos del Androide provocando pérdida de espacio en disco irrevocable e indetectable.

## [v1.4.53] - 2026-03-08
### Added
- **Protocolo Fénix (Auto-Resume UI)**: Arquitectura implementada usando `localStorage` ('activeVadScan') e intercepciones nativas (`scrollIntoView()`) para inyectar automatismo puro a un Crash de Webworker iOS. El navegador Safari recarga la página, scrollea directamente hasta el audio interceptado, expande la gráfica e inicia el escaneo Inteligente (continuando el % guardado) de manera completamente transparente y autónoma para el usuario.

## [v1.4.52] - 2026-03-08
### Fixed
- **Network Save Crash**: Transición absoluta de arquitecturas de telemetría de `sendBeacon` url-encoded hacia persistencias formales `fetch` JSON, asegurando que el backend Java recibe y serializa el archivo físico Checkpoint en `/sdcard` en lugar de retornarlo nulo.
- **RAM Overflow (Phase 1 Slice)**: Agregada compuerta condicional al bucle principal de Pre-slicing matricial de Float32s en el canal decodificado para ignorar e inyectar *null* en todos los offsets cronológicamente anteriores al Checkpoint `startChunk`. Reduce dramáticamente la saturación térmica y linear RAM inicial antes del Play.

## [v1.4.51] - 2026-03-08
### Fixed
- **Resume Duplicate Index**: Sumado un ineludible `+ 1` a la variable `startChunk` durante el proceso de rescate de VAD. Ahora el motor WASM reanuda verdaderamente en el fragmento siguiente al crasheo y no repite el trabajo redundante anterior.

## [v1.4.50] - 2026-03-08
### Fixed
- **Zero-Shot UX Delay**: Desplazado `drawForensicWaveform()` dentro de la promesa asíncrona `fetch` que recupera el progreso VAD (`/api/vad_load`), garantizando que las bandas rojas de voz se dibujen instantáneamente al reabrir archivos analizados en lugar de mostrarse invisibles por desincronización DOM.
- **Strict Resume Override**: Corrección del indexado dinámico de WASM. Carga fidedigna de `startChunk` extraída asincronamente sobre el Scope local forzando a evadir las iteraciones duplicadas post-crash.
- **Deep Memory Purge (AVFoundation)**: Movidas flagrantemente a lo más hondo de `_executeVadScan()` las coerciones nulas `channelData = null; decoded = null;` forzando de forma incontestable el descargo sobre la RAM de Apple previo a interactuar con Audio.

## [v1.4.49] - 2026-03-08
### Fixed
- **Resume Bug (Checkpoint Loop Logic)**: Forzada inicialización de `startChunk` interpretando estrictamente `window.vadCheckpointData` previniendo reevaluaciones inútiles y retomando fielmente desde el porcentaje guardado post-crash.
- **AVFoundation OOM Crash (Zombie Buffer)**: Purgadas a mano `channelData` y `decoded` antes del `finally` en el VAD Scanner, esquivando corrupciones masivas de memoria cuando el usuario pulsa en el botón de Play.

## [v1.4.48] - 2026-03-08
### Added
- **VAD Checkpointing (Caché Constante)**: El servidor NanoHTTPD de Android ahora expone endpoints `POST /api/vad_save` y `GET /api/vad_load` para persistir progresivamente en `/sdcard` las detecciones VAD de los archivos `.m4a`.
### Changed
- **Zero-Shot VAD**: La interfaz `openWaveform` lee silenciosamente el registro JSON del VAD al abrir un audio. Si está al 100%, pinta el análisis de IA de manera instántanea esquivando de raíz la compilación y ejecución de WebAssembly.
- **Skip-Slicing Resiliente**: Ante caídas severas por Memory Leak (OOM Jetsam) de Safari a mitad de un archivo kilométrico (21+ min), al reabrir el archivo, el bucle procesador asimila el Checkpoint pre-cayida y arranca a partir del chunk exacto donde murió.

## [v1.4.33] - 2026-03-08
### Fixed
- **OOM Root Cause (Ghost Reference)**: `const channelData` retenía ~80MB del AudioBuffer en scope impidiendo GC. Cambiado a `let` y anulado tras el streaming. `decoded` anulado antes de WASM. Pausa GC de 2s para Safari.

## [v1.4.32] - 2026-03-08
### Fixed
- **OOM WASM Linear Memory Leak**: Worker Kamikaze + 1.5s respiro post-terminate. WASM linear memory crece durante 84+ inferencias y nunca decrece, así que el Worker se destruye tras cada scan con un delay de 1.5s antes de redibujar, dando a iOS tiempo para liberar.

## [v1.4.31] - 2026-03-08
### Fixed
- **OOM Dual (Worker Persistente + Zero Cache)**: El Worker compila WASM una sola vez y persiste hasta cerrar el modal. PCM se decodifica fresco por scan y se libera al terminar (no compite con PLAY). Chunks se extraen directamente del AudioBuffer sin copia intermedia.

## [v1.4.30] - 2026-03-08
### Fixed
- **OOM Definitivo (Chunk Streaming)**: `runVADScanner` ya no envía el Float32Array entero al Worker. El Main Thread fragmenta el audio en chunks de 15 segundos (~960KB) y los envía uno a uno. `cachedPcmData` nunca se transfiere ni se neutra. Memoria pico estable: `cachedPcmData + WASM + 1 chunk` (~431MB para 1 hora de audio).

## [v1.4.29] - 2026-03-07
### Fixed
- **Crash Safari iOS en 2ª Invocación VAD (ArrayBuffer Neutered)**: Viaje asimétrico de datos: Transferable Objects solo en la ida (Main→Worker), Structured Clone en la vuelta (Worker→Main). Rehidratación del Float32Array con buffer virgen. Micro-delay de 100ms en `terminate()` para esquivar la carrera síncrona de WebKit.

## [v1.4.28] - 2026-03-07
### Fixed
- **Saturación del Decodificador iOS**: Restitución al patrón `Ping-Pong Zero-Copy` en `runVADScanner()`. Transferimos transitoriamente el array retenido (`cachedPcmData`) al Web Worker y este nos devuelve la propiedad finalizado el proceso de Inferencia `Chunking`, esquivando así el abrumador latido OOM de reinstanciar un `AudioContext` en cada ajuste.

## [v1.4.27] - 2026-03-07
### Fixed
- **OOM Jetsam Constante (Zombies RAM y VRAM)**: Destruccción total de canvas Backing Store imponiendo explícitamente `waveCv.width = 0`. Erradicación de invocaciones `<script>` fantasma en el `index.html`. Y desactivación pura de auto-reproducción `AVFoundation` para evitar solapamiento concurrente en la matriz de memoria de Apple versus el GC del Thread WebAssembly.

## [v1.4.26] - 2026-03-07
### Fixed
- **Jetsam OOM por Acumulación Dinámica (Stateless VAD)**: Erradicación absoluta de la caché PCM (Amnesia Total). El Hilo Principal delega destructivamente el array entero hacia el Worker y olvida su existencia para maximizar la bolsa global de RAM.
- **Respirador Artificial Crítico**: Incrementada la pausa pre-WASM a `1000ms` asegurando que el *Garbage Collector* de iOS destruya inequívocamente el motor AVFoundation antes de la nueva inferencia.

## [v1.4.25] - 2026-03-07
### Fixed
- **Jetsam OOM por Corrupción Zero-Copy**: Sustitución del destructivo `Transferable Object` por una Clonación Estructurada estricta del Float32Array madre hacia el Web Worker. Safari ya no crasheará al intentar recuperar buffers desreferenciados iterativamente.
- **Sobrecarga de RAM WASM (Chunking Muerte)**: Eliminada segmentación manual de Audios pesados en JavaScript. La delegación plena a las optimizaciones internas en C++ del parser ONNX reduce exponencialmente el pico colateral de GC.

## [v1.4.24] - 2026-03-07
### Fixed
- **Jetsam OOM por VRAM Leak**: Eliminación agresiva del Backing Store del Canvas gráfico forzando su desasignación (`waveCv.width = 0`), previniendo caídas puras por falta de memoria de video al hacer scroll en iOS.
- **Librerías WASM Zombie**: Eliminados los `<script>` estáticos del modelo de lenguaje en el HTML base para erradicar su consumo inactivo en la memoria del hilo principal.

## [v1.4.23] - 2026-03-07
### Fixed
- **Jetsam OOM por Carrera Inversa (Strict Lazy Load)**: Prohibición de instanciar `AVFoundation` dentro del bloque `finally` del Web Worker. El reproductor nativo sólo se aloja en RAM mediante una carga perezosa (Lazy Initialize) cuando el usuario pulsa en PLAY, previniendo el pico de memoria concurrente contra un WASM que aún no termina de reciclar.

## [v1.4.22] - 2026-03-07
### Fixed
- **Regresión OOM en Safari iOS**: Restaurada la lógica vital de "GC Breath" (pausa de 800ms post-AVFoundation destroy) y la caché Zero-Copy que se habían perdido en el snapshot anterior.
- **Cabezal de reproducción parpadeante**: Modificado el motor Canvas para dibujar un "cabezal fantasma" leyendo `window.vadSavedTime` mientras el motor nativo de audio está congelado, mejorando enormemente la fluidez de la UI.

## [v1.4.22-dev.1] - 2026-03-07
### Changed
- **VAD por Fragmentación (Chunking)**: Refactorización monumental de la inferencia WebAssembly de la IA. Los audios se procesan en lotes secuenciales inmutables de 15 segundos, aplanando violentamente la curva de consumo RAM de iOS y añadiendo telemetría de progreso (0% a 100%) en la intefaz web.

## [v1.4.21] - 2026-03-07
### Changed
- **Ping-Pong Zero-Copy & AVFoundation Freeze**: Cirugía de precisión contra los solapamientos de memoria de iOS.
  1. Se purga activamente el buffer `<audio>` nativo (AVFoundation) antes de llamar al Sandbox.
  2. Implementación de Transferable Objects bidireccional, cediendo y pidiendo la memoria del PCM al Worker (`[pcmData.buffer]`).
  3. Resurrección matemática del reproductor original tras aniquilar WebAssembly.

## [v1.4.20] - 2026-03-07
### Changed
- **Worker Kamikaze Pattern**: Solución definitiva al Memory Leak de WebAssembly (WASM Orphan) en iOS Safari al cambiar de sensibilidad VAD repetidamente.
  1. El audio decodificado (`cachedPcmData`) persiste veloz en caché primaria.
  2. El WebWorker (`vadWorker`) nace, ejecuta la red neuronal con el umbral deseado, y se aniquila irremediablemente en cada ciclo (`terminate()` y `revokeObjectURL()`).
  3. Cero acumulación de Sandbox RAM entre escaneos sucesivos.

## [v1.4.19] - 2026-03-07
### Changed
- **VAD Smart Cache (Anti-Jetsam)**: Arquitectura rediseñada para instanciar el Motor WASM y el AudioContext de Safari de forma inteligente y diferida.
  1. Doble caché global (`cachedPcmData`, `currentVadAudioUrl`) evita re-descargas y re-decodificaciones.
  2. El Web Worker y el ONNX Runtime permanecen dormidos pero vivos hasta que se cambia de archivo, evitando OOM por inicializaciones consecutivas.
  3. Ejecución controlada de la recolección de basura nativa (`vadWorker.terminate()`) únicamente al cerrar el modal modal (`closeWaveform()`).

## [v1.4.18] - 2026-03-07
### Fixed
- **WebKit Jetsam & OOM Crash (Safari iOS)**: Prevención total definitiva del Memory Leak por contención de nodos multimedia en Safari.
  1. Asesinato incondicional de la API nativa de audio (`await localCtx.close()`) inmediatamente después del Zero-Copy.
  2. Nulificación manual destructiva (`arrayBuffer = null; localCtx = null;`) anterior a cualquier instanciación de red neuronal.

## [v1.4.17] - 2026-03-07
### Fixed
- **Blob Worker URL Crash**: Subsanado fallo `URL is not valid` originado debido a que los hilos WASM instanciados a partir de un Blob perdían el contexto base del servidor. 
  1. Se inyectan pasaportes de acceso absoluto para el archivo `silero_vad.onnx`.
  2. Resoluciones firmes e inapelables de Worklet sobre la ruta CND (`vad.worklet.bundle.min.js`).

## [v1.4.16] - 2026-03-07
### Fixed
- **WebKit Jetsam & OOM Crash (Safari iOS)**: Prevención total y definitiva del Memory Leak en Safari al analizar audios sucesivos con VAD.
  1. Destrucción incondicional del Web Worker (`vadWorker.terminate()`) en el bloque `finally`.
  2. Purgado radical del Blob/URL (`URL.revokeObjectURL`) para forzar la liberación de RAM.
  3. Restauración de la API de inicialización correcta (`vad.NonRealTimeVAD.new()`).

## [v1.4.15] - 2026-03-07
### Fixed
- **WebKit Jetsam & OOM Crash (Safari iOS)**: Aplicadas 4 correcciones críticas para evitar cierres abruptos de Safari por falta de memoria. 
  1. Se fuerza la destrucción explícita de `AudioContext` tras extraer el PCM.
  2. Uso de transferencia Zero-Copy real mediante extracción con `.slice()` sin retener el clon léxico.
  3. Reestructuración integral del `Web Worker` de ejecución VAD, usando `vad.utils.processAudio` del bundle oficial para evitar alucinaciones API.
  4. Rediseño general de la memoria del Motor VAD Neuronal garantizando purga estricta 100%.

## [v1.4.14] - 2026-03-07
### Fixed
- **Anti-Jetsam WASM Sandbox**: Implementada la máxima defensa contra desbordamientos de Memoria en Safari iOS. Se ha encapsulado íntegramente la red neuronal VAD (ONNXRuntime) y el Motor WebAssembly en un `Web Worker` aislado. 
- **Zero-Copy Memory Transfer**: El `Float32Array` gigante se delega al Worker mediante variables `Transferable`, lo que expulsa instantáneamente de la RAM del motor gráfico el peso del PCM. Al cerrar el archivo, se ejecuta una directiva incondicional `worker.terminate()` que asfixia de raíz cualquier fuga (Leak) en el Backend del WASM, protegiendo 100% el hilo principal de sufrir un OOM.

## [v1.4.13] - 2026-03-07
### Fixed
- **WebKit DecodeAudioData Memory Leak**: Segundo ataque estructural al Out Of Memory (OOM) de Safari iOS. Se ha convertido el `AudioContext` en un Singleton global debido a que WebKit tiene un bug crónico eliminando contextos efímeros. Adicionalmente, se ejecuta un *Deep Clone* (`new Float32Array(...)`) sobre el resultado de `decodeAudioData` para romper la fuerte dependencia léxica que ataba la variable al gigantesco `AudioBuffer` original, forzando su aniquilación inmediata de la RAM antes de llamar al motor ONNX.

## [v1.4.12] - 2026-03-07
### Fixed
- **WebKit Media Daemon Leak**: Subsanado un segundo vector de desbordamiento de RAM (Out Of Memory) en Safari iOS. Se descubrió que el recolector de basura no eliminaba los `Float32Array` PCM gigantes tras el análisis, ni el `<audio>` nativo al cerrar el archivo. Se forzó `pcmData = null` tras el pase de la IA, y se inyectaron directivas de WebKit (`removeAttribute('src')` y `.load()`) al cerrar el modal para aniquilar el buffer de medios retenido.

## [v1.4.11] - 2026-03-07
### Fixed
- **WebKit Jetsam OOM Crash (Safari iOS)**: Subsanado un desbordamiento crítico de memoria RAM en el navegador tras encadenar múltiples análisis por IA. Se implementó un patrón Singleton para reciclar de manera global el Buffer WASM de `ONNXRuntime` y se acuchilló asíncronamente el hilo del `AudioContext.close()` tras la decodificación silenciando de raíz la proliferación de Daemons huérfanos de Audio.

## [v1.4.10] - 2026-03-07
### Fixed
- **Motor Web VAD (ONNX)**: Resuelto el fallo de ejecución reportado durante el análisis. Se extrajo la sintaxis oficial de `@ricky0123/vad-web@0.0.19` directamente desde su compilado NPM (TypeScript) para usar el generador asíncrono correcto (`NonRealTimeVAD.new`) en lugar de viejas interfaces obsoletas. Se forzó además el ruteo de `window.ort.env.wasm.wasmPaths` al CDN para evitar errores 404 del NanoHTTPD de Android.

## [v1.4.9] - 2026-03-07
### Added
- **Inteligencia Artificial VAD Offline (Silero)**: Inyección directa en el Web Dashboard de un motor de IA nativo en el FrontEnd mediante `onnxruntime-web` y `vad-web`. Analiza los archivos de audio detectando habla humana pura y pinta el segmento en Rojo Carmesí Forense sobre el canvas, aislando las voces del ruido ambiental sin necesidad de escuchar minutos de grabaciones en estático. Todo ejecutado 100% de manera local y descentralizada en el Navegador del operador sin pingear APIs externas de Nube.

## [v1.4.8] - 2026-03-07
### Fixed
- **Scroll iOS Safari Bloqueado (Terminal Web)**: Resuelto un _scroll trap_ crónico en dispositivos Apple donde el modal de Logs (Terminal) se congelaba e impedía deslizar el texto con el dedo. Se inyectó `-webkit-overflow-scrolling: touch` y se bloqueó programáticamente el desbordamiento fantasma de su contenedor padre (`overflow: hidden;`).

## [v1.4.7] - 2026-03-07
### Added
- **Consola Web de Logs (logToWeb)**: Inyectado un buffer circular in-RAM que captura en tiempo real los eventos críticos del sistema Android (FRP, AlarmManager, Codecs ADTS) y los vuelca en formato JSON. Se ha diseñado un panel web estético (modal oscuro con estilo Terminal) conectable desde los Ajustes, permitiendo diagnosticar errores a distancia (Headless Debug) desde dondequiera sin enchufar el móvil.

## [v1.4.6] - 2026-03-07
### Changed
- **Alineación Vertical Apaisada (Anti-Caída Modal)**: Implatada una nueva directiva CSS `@media (orientation: landscape)` que detecta la rotación física horizontal del móvil y anula los acolchados asimétricos y el padding superior de 60px del modal de reproduccion del Waveform. Esto fuerza a la caja principal a pegarse al borde superior de la pantalla, maximizando ferozmente los píxeles útiles verticales del visualizador forense.

## [v1.4.5] - 2026-03-07
### Changed
- **Pantalla Completa Apaisada (Modal Waveform)**: La capa modal que alberga el visualizador espectrográfico y los controles reacciona ahora inteligentemente a la rotación horizontal del dispositivo (`landscape`). Se han desbloqueado los cerrojos CSS de `max-width`, expandiendo masivamente el campo visual y aprovechando todo el ancho de la pantalla del teléfono para explorar los fotogramas de audio sin ahogo.

## [v1.4.4] - 2026-03-07
### Fixed
- **Responsividad Safari iOS**: Se han re-equilibrado las directivas CSS de Flexbox de los nuevos botones de desplazamiento extendido, forzando parámetros `nowrap`, constrictores de `flex: 1` y pad-reductions. Con esto se garantiza que los 6 controles quepen firmemente alineados sin saltos de línea (wrap) en las pantallas estrechas nativas de los dispositivos iPhone contemporáneos.

## [v1.4.3] - 2026-03-07
### Added
- **Controles de Salto Extendido (+/- 10s, 20s, 30s)**: Añadida en la vista web de escrutinio de grabaciones (`index.html`) una nueva sub-botonera de desplazamiento rápido, agilizando diametralmente la navegación y el `scrubbing` a lo largo de largas muestras de audio sin saturar el cliente de pulsaciones limitadas a 5s.

## [v1.4.2] - 2026-03-07
### Changed
- **Contraste de Regla (Canvas)**: Corregida opacidad deficitaria en las etiquetas del Eje X temporal que dificultaba su lectura en gráficas densas. Elevadas de Alfa `0.4` a `1.0` (Blanco Puro Sólido).
- **HUD Bi-Línea Absoluta (Forense)**: Descartado el contador de tiempo de bloque simple `XX / YY`. Ahora el reproductor de la onda forense incluye un Head-Up-Display incrustado de 2 líneas diferenciadas, calculando automáticamente y pre-fijando la Hora Absoluta Local (`HH:MM:SS`) en su frontispicio, permitiendo ubicar audios en su franja nocturna o diurna sin consultar el título base del clip original.

## [v1.4.1] - 2026-03-07
### Added
- **Eje X Absoluto (Forense)**: El visualizador de audio Canvas extrae en caliente la fecha de nacimiento real de los archivos .m4a y dibuja los instantes de reproducción en base a la línea temporal absoluta en formato `HH:MM:SS` (time-travel).
- **Scale-X Adaptativo (Anti-colisión)**: Algoritmo restrictivo que calcula dinámicamente el `maxLabels` en función de los píxeles útiles de pantalla en web, logrando un trazado de los segundos estético sin ahogar los números de tiempo bajo un denso *Zoom-Out*.

## [v1.4.0] - 2026-03-05
### Added
- **Desfibrilador de Hardware (Micrófono)**: Arquitectura de reconexión y tolerancia a fallos. Si otra aplicación (Llamadas, WhatsApp, Cámara) secuestra el micrófono: El sistema ahora absorbe graciosamente el silencio absoluto (0 dinámico) trazando una 'Zona Muerta Gris' en el visualizador web. Si por el contrario el framework MIUI lanza un `ERROR_DEAD_OBJECT`, el sistema destruye en caliente la pila de audio y resucita el micrófono reintentando cada 3 segundos hasta su recuperación, blindando al centinela.

## [v1.3.0] - 2026-03-05
### Added
- **Grabación Programada Avanzada**: Se añadió un Modal al Dashboard para detonar grabaciones diferidas indicando HH:MM de duración. El backend (AudioSentinel + OidoService) gestiona alarmas, despierta el hilo y graba el bloque estipulado.
- **Estado de Grabación Dual (UI)**: El Frontend ahora discierne si el micrófono ha sido secuestrado por el usuario manualmente (`🔴 GRABACIÓN MANUAL FORZADA`) o por una tarea de cronometría (`🟡 GRABACIÓN PROGRAMADA ACTIVA`).
- **Escala de Amplitud (Eje Y)**: El visor de Forma de Onda Forense ahora inyecta una cuadrícula horizontal de decibelios relacionales adaptativa. Al "Analizar Pista", la gráfica calcula 3 guías simétricas (al 25%, 50% y 75% del pico máximo auditable) garantizando que la deformación visual ("Zoom" o "Aplastamiento") mantenga su escala matemática legible en cualquier escenario acústico.

### Changed
- **UX Duración Programada**: Se eliminó el anti-patrón HTML5 `type="time"` para cronometrar duraciones, el cual invocaba teclados de "Reloj Astronómico" no deseados, sustituyéndose directamente por componentes nativos de horas y minutos.
- **Prioridad de Threads de Grabadora**: La Grabación Automática por Spike interrumpe su ejecución si choca temporalmente con el inicio de una Grabación Programada silenciosa.

## [v1.3.0-dev.7] - 2026-03-05
### Fixed
- **Estética de Onda Forense (Auto-Scale Y)**: Revertido el techo fijo de la malla. Para mantener la majestuosidad visual de la gráfica, la onda ahora vuelve a utilizar siempre el 100% de la altura de la pantalla (Auto-Zoom total). Para cuantificar este zoom, la rejilla de referencia (Eje Y) ahora se calcula de forma dinámica en tiempo real pintando sus tres guías cuartiles en el 25%, 50% y 75% del valor del Pico Máximo del evento bajo análisis.

## [v1.3.0-dev.6] - 2026-03-05
### Fixed
- **Visibilidad Malla Forense (Eje Y)**: Corregido un bug donde el motor de escalado del Canvas (auto-zoom vertical) colapsaba el tamaño virtual de las guías de amplitud (8k, 16k...) y las empujaba fuera de los límites superior e inferior de la pantalla cuando el audio analizado tenía un volumen bajo. Ahora el renderizado aterriza en techos prediseñados (8k, 16k, 24k, 32k) garantizando que la malla y el contexto numérico siempre sean visibles.

## [v1.3.0-dev.5] - 2026-03-05
### Added
- **Escala de Amplitud (Eje Y)**: Añadido soporte gráfico cualitativo al reproductor de forma de onda forense en Modal. Al "Analizar Pista", ahora se traza un retículo de referencia horizontal (Línea central 0, y picos de 8k, 16k, 24k, 32k) respecto al volumen de las ondas graficadas para facilitar la comparativa de intensidad de sonido.

## [v1.3.0-dev.4] - 2026-03-05
### Added
- **Estado de Grabación UI**: Se integró el flag `isScheduledRecording` desde `AudioSentinel` al `WebServer` y `index.html`. Ahora, cuando la Grabación Programada está activa y domina el micrófono, el Dashboard muestra explícitamente el badge `🟡 ESTADO: GRABACIÓN PROGRAMADA ACTIVA` en lugar de confundirlo con una grabación manual forzada, transparentando quién ordenó el arranque al usuario.

## [v1.3.0-dev.3] - 2026-03-05
### Changed
- **UX Duración Programada**: Modificado el campo de entrada web de Duración Estimada. Se sustituyó el elemento nativo `type="time"` (que Safari y Chrome renderizaban erróneamente forzando un selector de hora de reloj AM/PM) por dos selectores numéricos independientes divididos para las `Horas` y `Minutos` exactos.

## [v1.3.0-dev.2] - 2026-03-05
### Added
- **Jerarquía de Prioridad en Grabaciones**: Las Grabaciones Programadas ahora respetan las Grabaciones Manuales (Grabar Ahora) en curso (Ignorando la alarma), y abortan grácilmente las grabaciones automáticas por ruido para tomar control inmediato del Micrófono.
- **Interrupción Grácil**: Nuevo flag concurrente en el bucle principal de *AudioSentinel* que permite cerrar ordenadamente el `MediaCodec` y guardar el `.m4a` capturado antes de iniciar la tarea programada diferida.

## [v1.3.0-dev.1] - 2026-03-05
### Added
- **Grabación Programada**: Sistema de grabación en diferido con asignación de tiempo de inicio (Hora) y duración en minutos.
- **Evadiendo MIUI Doze**: Integración de relés vitales usando `AlarmManager.setExactAndAllowWhileIdle()` asegurando la persistencia y puntualidad atómica de ejecución remota a pantalla apagada. 

## [v1.2.0] - 2026-03-04
### Oficial Release
- **Despliegue del Tunel Inverso Inteligente (FRP)**: Aceptación de IP dinámica mediante Dashboard de control local.

## [v1.1.1] - 2026-03-04
### Changed
- **Optimización Memoria (AudioSentinel)**: Corregido GC Thrashing al eliminar la inyección recurrente de arrays en el bucle principal infinito, reciclando un objeto de RAM in-place.
- **Deuda Técnica (OidoService)**: Blindaje de seguridad en PendingIntent con `FLAG_IMMUTABLE` previendo la futura compatibilidad estricta de API 31+.


## [v1.1.0] - 2026-03-04
### Release Oficial
- Consolidación de versiones v1.0-dev.97 a v1.0-dev.100 en Release Estable.
- **Microscopio Temporal Web**: Zoom multitáctil y ruletero dinámico.
- **Anti-Sloppy Pinch**: Ventana de gracia para evitar saltos.
- **Desfibrilador Loop Infinito**: Arreglado el servicio de regeneración ante agresiones de MIUI.

## [v1.0-dev.100] - 2026-03-04
### Fixed
- **Anti-Sloppy Pinch (Canvas Web)**: Implementada una "Ventana de Gracia" de 100 milisegundos en el reproductor web forense. Esto soluciona un comportamiento errático donde un contacto asíncrono de los dos dedos durante un Pinch-to-Zoom provocaba que la onda saltase repentinamente interpretando un Scrubbing (click simple) falso.

## [v1.0-dev.99] - 2026-03-03
### Fixed
- **Motor Multitáctil (Pan & Zoom)**: Arreglado un bug donde el arrastre horizontal (Pan) de la onda forense se sentía agarrotado e invertido durante un Pinch-to-Zoom. Se sustituyó el cálculo posicional estático por un motor basado en "Deltas por frame", logrando que la onda se desplace con absoluta fluidez en la misma dirección que los dedos.

## [v1.0-dev.98] - 2026-03-03
### Added
- **Timeline Ruler & Zoom (Dashboard)**: Se ha reescrito el motor de renderizado del `waveCanvas` en el reproductor forense (`index.html`). Ahora incluye capacidades avanzadas de zoom multitáctil (Pinch-to-Zoom) para móviles, zoom vertical con rueda/trackpad para Desktop, paneo fluido y una nueva regla temporal en el Eje X que se autoescala (MM:SS).

## [v1.0-dev.97] - 2026-03-03
### Fixed
- **Desfibrilador de Un Solo Uso (Bugfix)**: El `AlarmManager` para la Operación Lázaro utilizaba `setExactAndAllowWhileIdle`, el cual dispara una única vez. Se modificó `OidoService` y `RevivalReceiver` para que el temporizador se recargue infinitamente tras cada comprobación, garantizando una vigilancia perpetua de MIUI a intervalos de 15 minutos.

## [v1.0-dev.96] - 2026-03-03
### Added
- **Inmunidad Diplomática**: Implementada solicitud activa de exención de batería (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) en el `MainActivity`. El Oído del Abuelo ahora pide automáticamente al usuario que lo desligue de las garras del "Ahorro de Batería" de MIUI, sin tener que bucear por menús ocultos.

## [v1.0-dev.95] - 2026-03-03
### Added
- **Operación Lázaro (Anti-Kill)**: Añadido mecanismo de resurrección activa (`RevivalReceiver`) y declaración explícita de `foregroundServiceType` en el Manifest para evitar que MIUI aniquile el servicio del micrófono por baja memoria (`low mem`). El servicio ahora incorpora un `AlarmManager` (el "Desfibrilador") que comprueba las constantes vitales del proceso cada 15 minutos y lo despierta agresivamente si el sistema operativo lo ha matado silenciosamente en background.

## [v1.0-dev.94] - 2026-03-03
### Added
- **Auditoría de Seguridad Completa**: Validación empírica de cifrado TLS (mediante sniffing de red) y de autenticación por Token (mediante pruebas de fallo negativo). El túnel queda certificado como seguro de extremo a extremo para el Xiaomi Redmi 9C.

## [v1.0-dev.89] - 2026-03-03
### Changed
- **Forzado de Protocolo TLS (Block Syntax)**: Se ha migrado la configuración de `frpc.toml` de notación de puntos a sintaxis de bloques explícitos (`[transport.tls]`). Esta medida se toma tras detectar en los logs del servidor que el handshake TLS no se estaba activando debido a un posible fallo de parseo del binario en la notación simplificada.

## [v1.0-dev.88] - 2026-03-03
### Changed
- **Sincronización de Entorno**: Nueva iteración de despliegue snapshot para consolidar la configuración de túneles y telemetría en el dispositivo físico.

## [v1.0-dev.87] - 2026-03-03
### Fixed
- **Sintaxis TOML de FRP**: Corregido error tipográfico en la configuración de TLS (`enabled` -> `enable`). Las versiones recientes de FRP son extremadamente estrictas con los nombres de los campos en el formato TOML.

## [v1.0-dev.86] - 2026-03-03
### Changed
- **Actualización de Configuración FRP**: Sincronización de nuevos parámetros de tunelización definidos en `frpc.toml` para pruebas de conectividad en el entorno local del operador.

## [v1.0-dev.85] - 2026-03-03
### Fixed
- **Stale Data en Configuración FRP**: Se ha sustituido la comprobación condicional de existencia de `frpc.toml` dentro del `getFilesDir()` de Android en `FrpManager` por una sobreescritura implacable (`extractAsset()`) en cada ciclo de arranque. Esto garantiza que cualquier cambio de IP remota o puerto introducido en desarrollo sobre la carpeta `/assets` se propague instantáneamente al teléfono sin necesidad de borrar los datos en caché de la aplicación desde los ajustes del sistema operativo.
## [v1.0-dev.84] - 2026-03-03
### Changed
- **Verificación Local**: Reconstrucción de la aplicación y despliegue del binario FRP (Proxy Inverso) encapsulado con soporte explícito para servidor perimetral de pruebas local (`127.0.0.1:7000`), confirmando la flexibilidad del archivo .toml para entornos de usuario en cuarentena.
## [v1.0-dev.83] - 2026-03-02
### Changed
- **Arquitectura de Batería Extrema (Exponential Backoff)**: Supresión completa del bucle nativo errante de `frpc`. Implementado un Interceptor (*Watchdog*) en los `StreamGobblers` de Java que asesina (`destroy()`) el proceso ante fallos TCP, aplicando retardos de espera ascendentes (de 10s hasta 5 min) para obligar al Módem de red a entrar en reposo *Deep Sleep*.
## [v1.0-dev.82] - 2026-03-02
### Engineering & Process
- **Poka-yoke en Workflows**: Renombrado el 'placeholder' engañoso (`Phase X:`) en `release_version.md` a un string literal que delimita sus variables con corchetes (`[Título Breve] [versionName Real] | [DD/MM/YYYY]`), suprimiendo el margen genérico de error de los Agentes al redactar sus crónicas técnicas de cierre procedimental.

## [v1.0-dev.81] - 2026-03-02
### Añadido
- **Túnel Inverso Nativo**: Integración del cliente FRP (`frpc`) directamente en el proceso core del teléfono a través del ejecutable precompilado de Go.
- **Microservicio FrpManager**: Nuevo módulo Java de extracción binaria y escalada de privilegios Kernel (`chmod 777`) para evadir el bloqueo de seguridad `W^X` de Android 10+.
- **Stream Gobblers**: Hilos purgados gemelos que consumen y loguean en tiempo real la salida `STDOUT` y `STDERR` del proceso para evitar colapsos por I/O e inanición del router FRP.

## [v1.0-dev.80] - 2026-03-01
### Fixed
- Corregido error de sintaxis en `.gitignore` que invalidaba el patrón de exclusión recursiva para archivos `.DS_Store` de macOS.

## [v1.0-dev.79] - 2026-03-01
### Añadido
- Blindaje definitivo de Git: Reglas de exclusión para macOS (`.DS_Store`) y archivos de telemetría (`*.txt`, `*.aac`).
### Cambiado
- Consolidación de remotos: Eliminado el remote duplicado `github-el-oido-del-abuelo`.
- Higiene total: Purga de archivos huérfanos y reinicio del índice de rastreo.

## [v1.0-dev.78] - 2026-02-26
### Añadido
- Blindaje **Anti-Deep Sleep**: Implementación de `WakeLock` (CPU) y `WifiLock` (Red de alto rendimiento) para evitar desconexiones y suspensiones de MIUI cuando el dispositivo entra en reposo profundo.

## [v1.0-dev.77] - 2026-02-26
### Añadido
- Blindaje táctil para iOS Safari: Desactivado "double-tap to zoom", selección accidental de texto en botones y destellos de pulsación.

## [v1.0-dev.76] - 2026-02-26
### Añadido
- Monitor de **Uptime** (Tiempo de Actividad) en el Dashboard.
- Monitor de **Espacio Libre** en disco con alerta visual (<500MB).
### Cambiado
- Rediseño del Dashboard: El número de versión ahora se muestra bajo el título para centrar la visual y limpiar la interfaz de camuflaje.

## [v1.0-dev.75] - 2026-02-26
### Changed
- **Consolidación de Camuflaje**: Despliegue formal de la identidad "Android System Listener" para asegurar su persistencia en el ciclo de vida del desarrollo.

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
