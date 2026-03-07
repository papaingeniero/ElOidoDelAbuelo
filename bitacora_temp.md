## 🚀 AVFoundation Freeze & Ping-Pong Zero-Copy v1.4.21 | 07/03/2026
### 📜 El Problema
A pesar de destruir el Worker en la versión v1.4.20, iOS Safari continuaba sufriendo eventos `Jetsam` (Out-Of-Memory) ocasionales al re-escanear. El cuello de botella real de WebKit radica en la superposición de recursos: el motor nativo de reproducción de iOS (`AVFoundation`, anclado al tag `<audio>`) mantenía retenidos pesados buffers multimedia en memoria bloqueada del sistema mientras simultáneamente se le exigía a la pestaña del navegador ramificar un Web Worker y compilar el pesado runtime WASM (40MB+) para la inferencia ONNX. La suma de ambas reservas colapsaba el hard-limit de la aplicación.

### 🛠️ La Solución
Implementación de Arquitectura de Evasión de Solapamientos y Memoria Destructiva:
1. **AVFoundation Freeze**: Antes de invocar a WebAssembly, forzamos un desahucio completo del reproductor nativo. Purgamos su fuente (`removeAttribute('src')`) y forzamos su re-lectura (`load()`). Durante los dolorosos segundos de análisis WASM, la RAM de AVFoundation queda garantizada en cero.
2. **Ping-Pong Zero-Copy**: Implementado en el Worker Kamikaze para evadir el duplicado de memoria (clone-by-value). Mandar 120 segundos de PCM al Worker duplica la memoria asignada si no hay "Transferencia de Posesión". 
    - a) El Hilo Principal cede la propiedad bruta de la RAM (`postMessage(..., [cachedPcmData.buffer])`).
    - b) El Worker finaliza y re-transfiere la propiedad de vuelta intacta (`postMessage(..., [e.data.pcmData.buffer])`).
3. **Resurrección del Dominio (Restauración de Estado)**: En el bloque `finally`, tras la completa aniquilación del WASM, el reproductor nativo es reiniciado de cero y su aguja es colocada matemáticamente en el `currentTime` original (`savedTime`) sin que el usuario lo note.

### 🎓 Lecciones Aprendidas
- **Los navegadores móviles mienten**: Mantener en pausa una etiqueta `<audio>` en iOS no libera la memoria gráfica/nativa asociada, sólo suspende el reloj. Es crítico destruir activamente el enlace de recurso del DOM antes de entrar en tareas intensivas de memoria para evitar crasheos silenciosos por límites del sistema operativo.
