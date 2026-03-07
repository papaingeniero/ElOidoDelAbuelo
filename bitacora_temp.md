## 🚀 WASM WebWorker Sandbox (OOM Finale) [v1.4.14] | 07/03/2026

**📜 El Problema:**
1. **La Caja Negra de WebAssembly (WASM):** Tras parchar el fugaz motor WebAudio, descubrimos que tu iPhone 15 mataba la pestaña *exactamente* al terminar el segundo análisis. El diagnóstico subyacente: `@ricky0123/vad-web` carga el modelo ONNX de Silero dentro del motor WASM del navegador. Safari reserva decenas de megas de RAM pura (fuera del control del Heap normal de Javascript) para los vectores neuronales. 
2. Como se ejecutaba en un Singleton (`globalVAD`), cada nuevo archivo que analizábamos "crecía" el caché interno de WASM sin saber cómo vaciarlo. Para el hilo principal y gráfico del Safari, ese crecimiento rápido y opaco significa una sola cosa: *Jetsam Panic* instantáneo.

**🛠️ La Solución:**
1. **Aislamiento Biológico (Web Workers):** Se reconstruyó el motor de "Analizar IA" para que, bajo demanda, cree un Sub-Proceso aislado del navegador (Worker). Esto encapsula la descarga de la librería, el peso de ONNX y el buffer WASM lejos del hilo visual de UI.
2. **Transferencia Zero-Copy:** Cuando el `AudioContext` acaba de decodificar tu archivo forense, ya no se pasa la variable al modelo. Se ejecuta un `worker.postMessage(pcmData, [pcmData.buffer])`. En JS, enviar el `buffer` por la matriz de transferencia **le roba físicamente la memoria RAM a Safari** y se la regala al sub-proceso, bajando de golpe el consumo del hilo de la interfaz.
3. **El Verdugo Final (`terminate`):** Al cerrar la ventana modal del audio, la app invoca explícitamente `vadWorker.terminate()`. Esto es una orden nativa ineludible al núcleo del Sistema Operativo de Apple para que destruya el hilo de procesamiento con todo su WASM dentro, reciclando la RAM al 100% y dejando a WebKit totalmente prístino antes de abrir tu segunda grabación.

**🎓 Lecciones Aprendidas:**
- Cuando WebAssembly roza los límites de OOM (Out of Memory) en iOS, ni el Garbage Collector ni setear punteros a null te salvarán. Tienes que arrancar arquitecturas de Micro-Servicios en el Frontend usando Web Workers desechables. Levantas el worker, le transfieres la bomba térmica, te devuelve la respuesta matemática, y lo asesinas antes de que contamine el servidor principal. Puro Edge Computing.
