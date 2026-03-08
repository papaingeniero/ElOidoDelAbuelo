## 🚀 VAD Checkpointing Resume & OOM Fix v1.4.49 | 08/03/2026

### 📜 El Problema
Tras la implementación original del Checkpointing, se descubrieron dos bugs críticos que impedían un análisis pesado (21+ minutos) ininterrumpido:
1. **Crash de Reanudación:** Si el sistema caía, el frontend enviaba un chunk de reinicio incorrecto y comenzaba a reevaluar todo el audio desde 0% en vez de "saltarse" lo completado, quemando recursos valiosos.
2. **Jetsam por Retención de Referencias Ocultas:** Aunque WebAssembly y AudioContext fuesen reseteados puramente, el gran `Float32Array` de 80MB llamado `channelData` no se destruía antes de reanudar el evento gráfico. Al terminar el chunking, este gigantesco buffer residía congelado referenciado por la lógica `_executeVadScan`, reventando los hilos cuando el usuario pulsaba en *reproducir* (Play audio) mezclando memoria nativa `AVFoundation` mas VAD buffer retenido.

### 🛠️ La Solución
Se ha purgado agresivamente el código del `index.html` sin contemplación implementado las órdenes precisas:
1. **Precisión Quirúrgica de Variables:** Identificado y corregido que el `openWaveform` pasara correctamente la lectura del `GET /api/vad_load` del JSON incompleto al valor local estricto de `window.vadCheckpointData`.
2. **Resume por Salto Seguro:** Modificada en raíz la función `_executeVadScan` forzando un `let startChunk = 0`, luego leyendo `window.vadCheckpointData.lastChunkProcessed`, validando que exista y ordenando al bucle de WASM (`for(let i = startChunk...)`) saltarse las pasadas inútiles.
3. **Erradicación de la Referencia ChannelData (Corte en Frío):** Se ha ejecutado el plan táctico "B". Segundos antes del cierre incondicional (`return results;`), el hilo principal purga explícitamente sus variables (`if (typeof channelData !== 'undefined') channelData = null;`) aniquilando al clon zombie y dejando espacio en VRAM y RAM para AVFoundation y Canvas.
