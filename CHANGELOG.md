# Changelog: El Oído del Abuelo

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
