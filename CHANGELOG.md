# Changelog: El Oído del Abuelo

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
