## 🚀 Arquitectura Stateless y Amnesia Total VAD v1.4.26 | 07/03/2026

### 📜 El Problema
La estabilización de Safari iOS lograda en v1.4.25 (mediante _Structured Cloning_) mitigó la corrupción de memoria generada por _Zero-Copy_, pero expuso un techo de cristal físico subyacente: el "Acumulamiento Inter-Escaneo".
Al conservar una variable global `cachedPcmData` con todo el Float32Array del audio decodificado, estábamos secuestrando permanentemente entre 50MB y 150MB de memoria RAM principal sin posibilidad de intervención del Garbage Collector. Si a este bloque muerto le sumamos el pico de VRAM de AVFoundation al reproducir el audio y el arranque dinámico del motor ONNX para el Web Worker, el límite de 1GB por Tab impuesto rutinariamente por el sistema operativo _Jetsam_ de Apple en dispositivos móviles colapsaba bajo el peso estático, matando la App en el segundo o tercer escaneo.

### 🛠️ La Solución
Se ha demolido el paradigma de "Caché de Rendimiento" cambiándolo por una filosofía de "Amnesia de Memoria" (Stateless VAD Architecture):
1. **Extracción Efímera**: WebKit ya no almacena ninguna copia global del audio PCM. Decodifica el archivo entero bajo demanda en un `AudioContext` aislado, que ahora se cierra y se aniquila instantáneamente (`await localCtx.close(); localCtx = null`).
2. **Transferencia Destructiva (Verdadero Zero-Copy)**: Tras decodificar y aniquilar el origen, el array transitorio restante se inyecta al Worker de ONNX *transfiriendo su control* (`[pcmData.buffer]`), vaciando el contexto local. El Worker consume el array y este muere orgánicamente junto con la hebra del Web Worker.
3. **Worker Kamikaze Perfecto**: Ya no intentamos "devolver" ni reutilizar la RAM. Al cerrarse el Worker (`tempWorker.terminate()`), su bloque entero de memoria aislada vuela por los aires, restableciendo la RAM del Safari a casi 0MB de sobrecarga en cada repetición.

### 🎓 Lecciones Aprendidas
- **La Memoria en Mobile dicta el Rendimiento, no la CPU**: En aplicaciones empotradas de IA en navegadores (Especialmente móviles), priorizar el _Caching_ para ahorrar ciclos de CPU es un error estratégico fatal. "Decodificar de Cero" toma 1.5 segundos extras al usuario móvil, pero "Guardarlo en Caché" causa un _Jetsam OOM Crash_ definitivo. La supervivencia a largo plazo reside siempre en la *Amnesia Estricta*.
