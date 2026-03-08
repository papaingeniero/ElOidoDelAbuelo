## 🚀 VAD Checkpointing Fetch Override & Pre-slice RAM Fix v1.4.52 | 08/03/2026

### 📜 El Problema
Los logs revelaron que tras implementar Checkpointing persistente, NanoHTTPD estaba retornando `null` para la lectura en memoria de los archivos grabados:
1. **El Bug de SendBeacon:** `navigator.sendBeacon` por diseño transmite diccionarios `URLSearchParams` codificados en formato MIME `application/x-www-form-urlencoded`. En el lado del servidor Java, la lectura `filesMap.get("postData")` perdía sistemáticamente los fragmentos del JSON, lo que provocaba que el archivo `.vad.json` jamás llegara a materializarse físicamente en disco. Todo se quedaba flotando en un pseudo-caché en vivo.
2. **Saturación Crítica en Pre-Slice:** La "Fase 1" pre-cortaba de manera imperativa cada bloque de 15 segundos del `channelData` de todo el audio, copiándolo a memoria, incluso si el "Resume" indicaba que el audio ya había sido completado al 60%. Operar el `slice()` al 100% de la pista multiplicaba innecesariamente la ocupación temporal en RAM, desafiando innecesariamente el frágil límite de Safari iOS.

### 🛠️ La Solución
Sustitución completa del corazón de enrutado asíncrono `_executeVadScan`:
1. **API Fetch EndPoint JSON (`application/json`):** Fue amputado el `sendBeacon` url-encoded para los debounces e inyectada una directiva nativa `fetch /api/vad_save` configurada explícitamente por cabecera y cuerpo `JSON.stringify()`. Por vez primera las tramas Checkpoint llegan a Java con estructura, asegurando una escritura blindada del texto en eMMC.
2. **Pre-Slice Nulo:** Implementado un ahorro brutal en recursos con un salto matricial `if (i >= startChunk)`. El array de Float32s que empujan al WASM se niega ahora a llenar la RAM con las secciones ya analizadas en el pasado, depositando directamente `null` (coste: 0 bytes) como *placeholder* de las viejas matrices y resbalando limpiamente sin desbordar el Scope temporal del bloque de Decode.

### 🎓 Lecciones Aprendidas
- **Desprecio Oculto al Payload Form:** `sendBeacon` es implacable mutando Cargas Útiles a URL Paramétrica, desconfigurando JSON Stringifieds nativos si el servidor Java espera un String crudo en un mapa Multipart. Usa siempre Fetch POST `application/json` si el otro extremo de la tubería requiere serialización pura y dependes de `Object/JSON.parse()`.
