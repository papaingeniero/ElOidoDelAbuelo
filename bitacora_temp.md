## 🚀 Controles de Búsqueda Extendida [v1.4.3] | 07/03/2026

**📜 El Problema:**
1. **Navegación Lenta en Audios Densos:** El equipo y los usuarios advirtieron que para grabaciones forenses extensas (de varios minutos o incluso horas), los botones base de `⏮ -5s` y `+5s ⏭` resultaban insuficientes. Desplazarse grandes segmentos implicaba interactuar compulsivamente repetidas veces con el botón, derivando en frustración y lentitud en el análisis de una grabación.

**🛠️ La Solución:**
1. **Panel de Salto Multi-Resolución:** Se inyectó una fila inferior de botones debajo de los controles principales de reproducción en `index.html`. Esta nueva "botonera táctica" cuenta con 6 botones nuevos dedicados a dar saltos en el espectrograma (Scrubbing) de alta velocidad (`-30s`, `-20s`, `-10s`, `+10s`, `+20s`, `+30s`). Se han estilizado con `flex-wrap` y un margen comprimido (`gap: 8px`) para que el renderizado de la interfaz fluya en un dispositivo estrecho como el Xiaomi Redmi 9C sin empujar la onda fuera de la pantalla.
2. **Reutilización del Motor de Scrub:** Todos los botones fueron enganchados directamente contra la función nativa `seekWaveform(seconds)` que re-dibuja de manera óptima y atómica el Canvas con la nueva `currentTime` e invoca inteligentemente al `drawForensicWaveform()`.

**🎓 Lecciones Aprendidas:**
- Acondicionar la interfaz del lado del cliente (Web UI distribuida a través del NanoHTTPD) con nuevas bondades no requiere reconstruir el `OidoService` backend en Android, siendo una táctica de *Desacoplamiento Front-Back*. Hemos ampliado la capacidad de rastreo forense sin tocar una sola API de audio de bajo nivel.
