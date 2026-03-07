## 🚀 Worker Kamikaze v1.4.20 | 07/03/2026
### 📜 El Problema
Mantener el Web Worker (`vadWorker`) vivo en el estado global para agilizar subsecuentes ejecuciones estaba provocando un **"Memory Leak" Masivo de WASM (WASM Orphan)** en iOS Safari. Al deslizar repetidamente el control de umbral, el entorno de WebAssembly asimilaba las cargas pero fallaba al compactar la RAM entre ejecuciones retenidas, desencadenando invariablemente un *Jetsam Panic* y el crasheo letal de la vista WebKit.

### 🛠️ La Solución
Implementación de la Arquitectura **Worker Kamikaze**:
1. **Destrucción Síncrona Garantizada**: El Worker ha dejado de ser una variable global durmiente (`let vadWorker`). Ahora tiene ciclo vital de usar-y-tirar bloqueado en el Scope de `runVADScanner()` (`let tempWorker`).
2. **Kamikaze Flow**: 
    - a) *Nace*:  Creación del Worker vía URL temporal.
    - b) *Trabaja*:  En base al PCM guardado pacíficamente en caché (`cachedPcmData`).
    - c) *Se Autodestruye*: Un bloque `finally` blindado aniquila irrevocablemente el Worker (`tempWorker.terminate();`) y revoca su URL (`URL.revokeObjectURL()`).
3. La huella de memoria (RAM WASM) retorna exactamente a cero al acabar la función, cediendo todo el peso al `cachedPcmData` puro (que es ligero y seguro de manejar).

### 🎓 Lecciones Aprendidas
- **No todo Sandbox es de cristal**: A diferencia de los entornos de escritorio, los navegadores empaquetados en iOS penalizan brutalmente la persistencia de subprocesos y motores compilados (WASM / ONNXRuntime). Recrear la estructura neuronal en el Worker es más lento (~2s), pero es preferible a una aniquilación implacable de todo el tab del navegador por agotamiento de RAM. *La Estabilidad absoluta prima siempre sobre la latencia*.
