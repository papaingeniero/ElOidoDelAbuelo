## 🚀 VAD Smart Cache v1.4.19 | 07/03/2026
### 📜 El Problema
A pesar de aniquilar el Buffer (`AudioContext`) de manera precoz, se evidenciaba un evento Jetsam (crasheo por OOM) latente en iOS Safari si el usuario "jugaba" deslizando repetidamente el control de umbral (Sensibilidad VAD). El origen residía en que la función `runVADScanner()` descargaba el `.m4a`, lo decodificaba a PCM, y levantaba la superestructura WASM de cero en cada simple solicitud del usuario, agotando el límite de recursos en ciclos consecutivos rápidos.

### 🛠️ La Solución
Implementación de un sistema **Smart Cache** (Arquitectura de Instanciación Diferida):
1. **Doble Caché Global**: Inyección de las variables `cachedPcmData` y `currentVadAudioUrl`. Solo se decodifica y transfiere el audio (`slice`) si la URL diverge del escaneo inmediato anterior.
2. **Reutilización del Motor WASM**: El Web Worker (`vadWorker`) y el modelo neuronal interno (`myvad = vad.NonRealTimeVAD.new()`) ya no son exterminados tras cada éxito. El worker permanece vivo, y la red neuronal se reconstruye en su sub-proceso únicamente si el valor de `e.data.threshold` varía (mediante variable reactiva `lastThreshold`).
3. **Limpieza Controlada**: `closeWaveform()` asume la responsabilidad final de purgar `cachedPcmData` y ejecutar un glorioso pero retardado `vadWorker.terminate()` en el momento en el que el audio forense se oculta.

### 🎓 Lecciones Aprendidas
- **Destrucción diferida es más amable que Destrucción Reactiva**: Aunque iOS requiere la aniquilación de la sesión WebAssembly para limpiar la RAM, hacerlo de manera prematura por cada "Re-Scan" colapsa la inicialización de motores de Sandbox a alta velocidad. Es estrictamente mejor congelar el Worker vivo para reutilización inmediata y desintegrarlo de la memoria solamente cuando el usuario pulsa en la 'X' para marcharse y descartar la tarea modal subyacente.
