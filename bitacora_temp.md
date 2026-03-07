## 🚀 Extreme WASM Sandbox & Termination v1.4.16 | 07/03/2026
### 📜 El Problema
A pesar de las mejoras previas (v1.4.15), el "Jetsam Panic" (Out-Of-Memory) persistía en iOS Safari al terminar de escanear audios sucesivos con el analizador VAD. El origen radicaba en que el Web Worker y su motor interno WASM (ONNX Runtime) no liberaban su gigantesco *heap* de memoria tras enviar el mensaje de `done`, ni tampoco era suficiente rebotar un nuevo stream `pcmData`. 

### 🛠️ La Solución
Se ha insertado la solución arquitectónica quirúrgica final en el entorno del `Web Worker` de `index.html`:
1. **Destrucción Incondicional**: Tras concluir el análisis, y dentro del bloque `finally` de `runVADScanner`, la instrucción `vadWorker.terminate()` se dispara ciegamente. 
2. **Purgado Radical de Objetos URL**: Liberación con `URL.revokeObjectURL(workerUrl)` impidiendo que iOS retenga los Blobs en caché, forzando la muerte del subproceso completo y su respectiva carga WebAssembly.
3. **Restauración VAD API**: Regreso al flujo de inicialización correcto `vad.NonRealTimeVAD.new()` dentro de la estructura Web Worker.

### 🎓 Lecciones Aprendidas
- **WebKit Death Trap**: El navegador nativo de Apple requiere que se le mate el Sub-Proceso Worker sin piedad al acabar la tarea intensa. Las almas de los Workers (y las sesiones de red neuronal ONNX que habitan en ellos) nunca ceden voluntariamente la RAM al hilo principal por más `nulls` que se apliquen internamente.
