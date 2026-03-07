## 🚀 Purga Extrema de RAM y Amnesia Absoluta (Anti-Jetsam) v1.4.27 | 07/03/2026

### 📜 El Problema
Los Crashes y OOM (Out Of Memory - _Jetsam_) en iOS persistían al analizar audios consecutivamente a pesar de purgar las variables. La causa: Javascript y el motor Retina/Metal retienen "Zombies" invisibles. En concreto:
1. **Scripts Zombie**: El modelo ONNX local está declarado en `<head>`, cargando redundante e inútilmente todo el runtime en el hilo principal sin siquiera usarlo (todo corre ahora en el Web Worker). Masiva RAM perdida de base.
2. **Backing Store Leaks**: Destruir la variable de canvas no es suficiente para Apple. WebKit retiene en la memoria de vídeo (VRAM) el gigantesco lienzo generado. Si se cierran y abren formas de onda iterativamente, se satura la RAM del compositor gráfico.
3. **AVPlayer Ghosts**: Resucitar y auto-arrancar el audio `forensicAudio.currentTime = window.vadSavedTime` instantes después de que la VAD se detenga crea un cuello de botella fatal. Se empalma el pico de memoria destructivo WASM (`terminate()`) con la exigente inicialización de `AVFoundation` para pre-cachear el audio antes del `PLAY`.

### 🛠️ La Solución
Se han ejecutado estas 4 contramedidas quirúrjicas directamente sobre `index.html` sin contemplaciones:
1. **Erradicación de Scripts Zombie**: Se eliminaron definitivamente las invocaciones globales de `ort.js` y `bundle.min.js`.
2. **Purgante VRAM (`waveCv.width = 0`)**: En `closeWaveform()`, se fuerza a Apple a liberar el Backing Store gráfico reseteando el ancho del canvas a 0 antes de ocultarlo.
3. **Carga Perezosa Auditiva Estricta**: En `runVADScanner()`, se aniquila el flag de reactivación automática. El renderizado posterior está 100% desconectado de la RAM nativa de Audio. El usuario DEBE pulsar explícitamente `PLAY` de nuevo si quiere reproducir el Audio IA.
4. **Resurrección Blindada**: `initForensicAudio()` ha adoptado un comportamiento `Lazy`. Solo invoca a los buffers tras el evento táctil, asimilando además suavemente el salto del Cabezal (`vadSavedTime`).

### 🎓 Lecciones Aprendidas
- **El Garbage Collector es Pobre**: En iOS, un Canvas oculto sigue ocupando decenas de MegaBytes de Texturas Activas. Un `width = 0` explícito es la única cura.
- **La Superposición Mata**: Si WASM está bajando, NUNCA subas `AVFoundation` a la vez. Separar temporalmente la IA del Multimedia es la única manera de convivir pacíficamente bajo el umbral estricto del Jetsam de Apple.
