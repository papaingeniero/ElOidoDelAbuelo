## 🚀 OOM Fixes & WASM Sandbox v1.4.15 | 07/03/2026
### 📜 El Problema
La última implementación de `runVADScanner()` provocaba eventos "Jetsam" (Out-Of-Memory) en iOS Safari, obligando a la recarga agresiva de la página. Había problemas graves de retención en `AudioContext`, un mal manejo en la clonación de la onda (`Float32Array`) sin Zero-Copy real, uso de APIs de VAD inexistentes (`vad.NonRealTimeVAD.new()`) y un diseño inseguro en el `Web Worker`.

### 🛠️ La Solución
Se han aplicado directamente 4 correcciones críticas de Meta-Ingeniería en `index.html`:
1. **Fuga de Memoria WebKit**: El contexto de audio `localCtx` es forzado a cerrar (`await localCtx.close(); localCtx = null`) tras la decodificación.
2. **Transferencia Zero-Copy**: Extraemos el buffer con `tempAudioBuffer.getChannelData(0).slice()` anulando cualquier referencia al array subyacente que iOS amarrara a la RAM.
3. **Purificación de la API**: Uso directo de `vad.utils.processAudio()` desde el bundle real en vez de instanciar clases inexistentes.
4. **Re-Estructura del Worker**: Código JavaScript saneado y parametrizado correctamente inidicando a ONNX un hilo único (`ort.env.wasm.numThreads = 1;`).

### 🎓 Lecciones Aprendidas
- **Garbage Collection (Safari)**: Safari jamás soltará un `AudioContext` a menos que su destrucción se ordene implícitamente, costando docenas de Megabytes ciegos de RAM y originando un eventual asfixio sistémico (Jetsam event).
- **APIs Fantasma**: Asumir versiones antiguas o emuladas de ONNX/VAD cuesta horas de depuración. Leer el `.d.ts` o los bundles ofuscados suele ser el único medio de certificar las funciones que realmente exportan a un Web Worker.
