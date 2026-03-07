## 🚀 Worker Persistente + Zero Cache (Dual Anti-OOM) v1.4.31 | 08/03/2026

### 📜 El Problema
El crash después de pulsar PLAY (sin segundo escaneo) con archivos de 10 minutos reveló que el enemigo no estaba en los Transferable Objects ni en el tamaño de los chunks. Estaba en la **acumulación de memoria residual**:
1. **WASM Zombie**: `worker.terminate()` marca la memoria WASM para recolección, pero iOS no la libera inmediatamente. Al pulsar PLAY, AVFoundation intenta reservar buffers → la suma de WASM residual + cachedPcmData + AVFoundation excede el umbral Jetsam.
2. **Doble Compilación WASM**: Al hacer el 2º escaneo, se creaba un NUEVO Worker que compilaba ONNX/WASM desde cero, duplicando la huella.

### 🛠️ La Solución: Worker Persistente + Zero Cache
1. **Worker Persistente**: El Web Worker compila ONNX + Silero VAD una SOLA VEZ y permanece vivo hasta que el usuario cierra el modal (`closeWaveform()`). Cada escaneo reutiliza el motor WASM ya compilado.
2. **Zero Cache de PCM**: `cachedPcmData` se libera al inicio de cada scan. El audio se decodifica fresco, se envían chunks desde el AudioBuffer del contexto, y al terminar el scan NO queda ningún Float32Array retenido. PLAY ya no compite con datos PCM.
3. **Streaming desde AudioBuffer**: Los chunks se extraen directamente del AudioBuffer decodificado (`channelData.slice()`), sin crear una copia intermedia del array completo. El AudioContext se cierra ANTES de iniciar WASM.
4. **Ciclo de Vida Limpio**: Worker Persistente se destruye únicamente en `closeWaveform()`, asegurando limpieza total al cerrar el modal.

### 🎓 Lecciones Aprendidas
- **`terminate()` No Es Inmediato en iOS**: La liberación de memoria WASM tras `terminate()` es asíncrona e impredecible. La única forma de evitar el solapamiento es no destruir el Worker entre escaneos.
- **La Caché es un Lujo que iOS No Se Puede Permitir**: Retener 38MB+ de PCM decodificado ahorra 2 segundos de re-decode, pero cuesta la vida de la pestaña cuando AVFoundation necesita sus propios buffers. El re-decode es un precio aceptable por la estabilidad.
