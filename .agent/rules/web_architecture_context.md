---
trigger: always_on
---

# Contexto de Arquitectura Web: El Oído del Abuelo

Este documento define el funcionamiento base de la interfaz web que servirá NanoHTTPD desde el Xiaomi, garantizando que el Agente entienda el flujo de telemetría de audio.

## Funcionamiento del Panel de Control (Dashboard)

En la página principal del navegador servido por el móvil se carga un panel con el estado en tiempo real:
* **Estado del Centinela**: Escuchando, Grabando Alarma o Llamada en Curso.
* **Telemetría en Vivo (AJAX)**:
    * **Vúmetro (Barra de Volumen)**: Muestra la amplitud actual del ruido captado por el micrófono en tiempo real.
    * **Sensores del Sistema**: Batería del Xiaomi, estado de carga (bypass) y espacio libre en disco para grabaciones.
* **Configuración (Modal)**: Permite ajustar el `NOISE_THRESHOLD` (umbral de disparo en decibelios) sin recompilar la app.

### Historial de Alertas (Grabaciones)
Debajo del panel en vivo, se muestra la lista de detecciones de ruido almacenadas.
* **Tarjeta de Audio**: Cada elemento muestra la hora de la detección, duración del archivo `.m4a`, pico máximo de ruido alcanzado y tamaño del archivo.
* **Reproductor**: Al hacer clic en una tarjeta, se abre un reproductor HTML5 nativo para escuchar la grabación de la alerta directamente desde el navegador del Mac.