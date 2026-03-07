## 🚀 Corrección Quirúrgica Anti-Neutered WebKit v1.4.29 | 07/03/2026

### 📜 El Problema
iOS Safari explota en la segunda invocación del análisis VAD tras ajustar la sensibilidad. El diagnóstico revela una colisión fundamental en el mecanismo de **Transferable Objects** específico de WebKit:
1. **ArrayBuffer "Neutered" Zombie**: Cuando el Worker devuelve los datos PCM al hilo principal usando `postMessage({..., pcmData}, [pcmData.buffer])` (Transferable), seguido inmediatamente por `worker.terminate()`, WebKit ejecuta la terminación **síncronamente**, cortando la transferencia a medio vuelo. El `cachedPcmData` resultante queda con un `buffer.byteLength === 0` ("neutered"), provocando que la 2ª invocación intente transferir un ArrayBuffer vacío al Worker, lo que colapsa ONNX/WASM → Jetsam.
2. **AVPlayer Zombie Concurrente**: El `forensicAudio` que creó el usuario al hacer Play/Pause retiene ~30-80MB de buffers nativos AVFoundation que compiten con el pico de WASM.

### 🛠️ La Solución (Fusión de 3 Correcciones Quirúrgicas)
1. **Viaje Asimétrico de Datos (Ida ≠ Vuelta)**: Main→Worker usa Transferable Objects (`[cachedPcmData.buffer]`) para vaciar el hilo principal durante la inferencia WASM. Pero Worker→Main usa Structured Clone plano (`postMessage({..., pcmData})` SIN segundo argumento), garantizando que la copia llegue íntegra sin importar la carrera de terminación.
2. **Rehidratación del Float32Array**: Al recibir los datos del Worker, se construye un array completamente nuevo (`cachedPcmData = new Float32Array(e.data.pcmData)`) con un ArrayBuffer virgen y propio, eliminando cualquier referencia transversal al Worker destruido.
3. **Micro-Delay en terminate() (100ms)**: Se desacopla la ejecución de `terminate()` del flujo síncrono mediante `setTimeout(() => workerToKill.terminate(), 100)`, dando margen a Safari para finalizar internamente sus colas de mensajes pendientes antes de la aniquilación.
4. **FREEZE de AVFoundation Preservado**: Se mantiene la destrucción preventiva del `<audio>` con respiro de 800ms para el GC de iOS, evitando el solapamiento crítico entre AVFoundation y WASM.

### 🎓 Lecciones Aprendidas
- **Transferable Objects son Asimétricos por Naturaleza en Safari**: La ida (Main→Worker) es segura porque el Worker está vivo y esperando. La vuelta (Worker→Main) es peligrosa si `terminate()` se ejecuta síncronamente antes de que el Engine complete la transferencia interna. Chrome es permisivo; Safari no perdona.
- **El Segundo Viaje Siempre Debe Ser Structured Clone**: En entornos WebKit móviles donde `terminate()` es imprevisible, el coste de una copia estructurada (~50ms en un array de 50MB) es infinitamente preferible al riesgo de un ArrayBuffer neutered que provoca un crash OOM catastrófico.
