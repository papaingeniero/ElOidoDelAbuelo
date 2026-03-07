## 🚀 Restauración Arquitectura Ping-Pong Zero-Copy v1.4.28 | 07/03/2026

### 📜 El Problema
La arquitectura de "Amnesia Total" implementada preventivamente resultó ser demasiado punitiva en iOS. Al destruir por completo el contexto de audio y la caché PCM en cada pasada, la aplicación obligaba al usuario a **reedecodificar** el archivo entero cada vez que se ajustaba la sensibilidad de la IA. Este proceso saturaba al decodificador nativo de Apple (AVFoundation) en la segunda o tercera llamada, provocando un OOM inmediato por superposición de decodificadores latentes.

### 🛠️ La Solución
Se ha revertido a la arquitectura quirúrgica que ya probó estabilizar el entorno WASM pero ahora acoplada con los limpiadores de VRAM y descargas globales introducidas en la v1.4.27.
1. **Recuperación de `cachedPcmData`**: Ya no decodificamos el disco en cada pasada. El Float32Array decodificado se retiene en el hilo principal como caché.
2. **Ping-Pong Zero-Copy Perfecto (`[cachedPcmData.buffer]`)**: La caché no se copia, **se transfiere** al Worker (reduciendo la RAM del Main Thread a 0 mientras escanea). Al terminar, el Worker **devuelve la posesión** del array (`postMessage(..., [e.data.pcmData.buffer])`) al hilo principal. Esta danza mantiene la huella de memoria global estable sin forzar al Garbage Collector a destruir buffers de 100MB iterativamente.
3. **Chunking Anti-WASM Leak**: Dentro del Web Worker, el tensor no se evalúa de golpe, sino que se inyecta en "lonchas" de 15 segundos (`chunkSize = sampleRate * 15`). Esto aplana la curva de memoria lineal de WebAssembly en iOS Safari, impidiendo que el motor C++ interno exceda su propia cuota virtual.

### 🎓 Lecciones Aprendidas
- **Stateless no siempre es mejor**: La recolección de basura iterativa en bloques de ArrayBuffer grandes (Decodificar -> Destruir -> Repetir) estresa profundamente a los motores del browser móvil. El préstamo seguro (Ping-Pong) es superior energéticamente y operativamente a la aniquilación continua de memoria.
