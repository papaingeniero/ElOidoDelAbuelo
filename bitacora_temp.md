## 🚀 Arquitectura Chunk Streaming (Anti-OOM Definitiva) v1.4.30 | 08/03/2026

### 📜 El Problema
Tras numerosas iteraciones, todas las arquitecturas anteriores compartían un defecto de diseño fundamental: **enviaban el Float32Array entero al Worker de golpe**, traspasando cientos de megabytes por la frontera del `postMessage`. Esto forzaba a elegir entre dos caminos perdedores:
- **Transferable Objects** (`[buffer]`): Neutra el ArrayBuffer en el Main Thread. El Worker lo devuelve, pero `terminate()` en Safari corta la transferencia de vuelta → ArrayBuffer corrupto ("neutered zombie") → crash en la 2ª invocación.
- **Structured Clone** (copiar): Duplica temporalmente el array entero en memoria (Main Thread + Worker). Para un archivo de 1 hora (~230MB), eso son ~460MB solo de PCM, más ~200MB de WASM = **660MB** → Jetsam inmediato en iOS.

### 🛠️ La Solución: Chunk Streaming (Fragmentación en el Borde del Mensaje)
Se ha rediseñado por completo la comunicación Main Thread ↔ Worker:
1. **`cachedPcmData` NUNCA cruza la frontera**: El Float32Array decodificado vive exclusivamente en el Main Thread. Jamás se transfiere ni se clona entero al Worker.
2. **Protocolo de 2 Fases en el Worker**:
   - `init`: El Worker descarga ONNX + Silero VAD y las compila en WASM **una sola vez**. Responde `ready`.
   - `chunk`: El Main Thread extrae cortes de 15 segundos (`cachedPcmData.slice(offset, end)` ≈ 960KB) y los envía **uno a uno**. El Worker procesa cada micro-fragmento y devuelve solamente los segmentos detectados (pocos bytes).
3. **Memoria Pico**: cachedPcmData (230MB) + Worker WASM (200MB) + 1 chunk en tránsito (1MB) = **~431MB**. Estable e idéntico en la 1ª, 2ª, o enésima invocación.
4. **Terminación Segura**: `terminate()` se ejecuta inmediatamente sin micro-delay. No hay datos PCM en tránsito entre Worker y Main Thread que puedan corromperse.

### 🎓 Lecciones Aprendidas
- **El Borde del Mensaje ES la Frontera de Memoria**: El verdadero enemigo nunca fue el Garbage Collector ni los Transferable Objects. Era **el tamaño del payload que cruzaba `postMessage`**. Fragmentar el dato ANTES de enviarlo (en vez de fragmentarlo dentro del Worker después de recibirlo entero) reduce el pico de memoria de `O(2N)` a `O(N + chunk)`.
- **Un Archivo de 1 Hora Sobrevive**: Con un chunk de 15s, un audio de 60 minutos genera 240 micro-envíos de ~960KB. La huella de memoria pico permanece estable en ~431MB independientemente de la duración del archivo.
