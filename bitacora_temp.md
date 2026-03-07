## 🚀 Purga de VRAM y Dependencias Fantasma v1.4.24 | 07/03/2026

### 📜 El Problema
A pesar del Strict Lazy Load implementado en v1.4.23, los usuarios reportaban cuelgues (Jetsam OOM) en Safari iOS simplemente al hacer "scroll" vertical rápido por el historial de audios después de haber usado la IA. Las métricas de WebKit revelaron dos fugas de memoria gigantescas ajenas a la decodificación de audio:
1. **Librerías Fantasma**: El hilo principal del HTML seguía descargando y albergando pasivamente las pesadas librerías de `onnxruntime-web` y `vad-web` a pesar de que el motor real había sido migrado a un Web Worker.
2. **Fat Canvas (VRAM Leak)**: Al cerrar el modal (`closeWaveform()`), el navegador retenía en su memoria gráfica (*Compositor Backing Store*) todos los píxeles dibujados de la enorme onda roja del audio escaneado, saturando la VRAM subyacente de la que el recolector básico de JavaScript no tiene control.

### 🛠️ La Solución
Implementación agresiva de saneamiento Frontend:
1. **Erradicación del Hilo Principal**: Se han fulminado las etiquetas `<script>` estáticas de la cabecera HTML. WebKit ya no gastará ni un kilobyte en cargar estas dependencias de IA, que ahora viven exclusiva y epímeramente dentro del `importScripts()` de nuestro Worker Kamikaze.
2. **Demolición Estructural del Canvas**: Al cerrar la visualización de la onda, en lugar de limpiar lógicamente con un `clearRect` (que retiene el buffer gráfico), inyectamos un `waveCv.width = 0; waveCv.width = w;`. Esta manipulación forzosa del atributo físico destruye instantáneamente el contexto OGL subyacente, forzando la desasignación de la costosa VRAM en el chip móvil.

### 🎓 Lecciones Aprendidas
- **No es lo mismo RAM que VRAM**: Tapar agujeros lógicos (Variables nulas) no sirve de nada si el DOM retiene estructuras gigantes en memoria de vídeo para un "posible renderizado futuro". Un canvas gigante devora megabytes por sí solo en iOS; destruirlo explícitamente es la única garantía contra un Jetsam asíncrono. Y una librería no usada pero "Declarada" en el Head, es un parásito invisible.
