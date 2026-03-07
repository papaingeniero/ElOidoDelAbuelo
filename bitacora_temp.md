## 🚀 Kamikaze Worker + Respiro WASM Agresivo v1.4.32 | 08/03/2026

### 📜 El Problema
Con archivos de 21 minutos (84 chunks), la memoria lineal WASM del Worker **crece progresivamente** durante la inferencia ONNX y **jamás decrece** (limitación inherente de WebAssembly 1.0). Al terminar el scan, el Worker Persistente retiene toda esa memoria inflada. El simple hecho de redibujar la forma de onda tras el scan empuja la memoria total por encima del umbral Jetsam de iOS → crash inmediato, sin necesidad de pulsar PLAY ni hacer un 2º escaneo.

### 🛠️ La Solución
1. **Worker Kamikaze (Vida = 1 Scan)**: El Worker se crea, procesa todos los chunks, y se destruye INMEDIATAMENTE al terminar. No persiste entre escaneos.
2. **Respiro Agresivo (1.5 segundos)**: Tras `terminate()`, se espera 1.5 segundos antes de redibujar la forma de onda, dando a iOS tiempo real para liberar la memoria lineal WASM del Worker destruido.
3. **Zero Cache de PCM**: El audio se decodifica fresco en cada invocación y se libera al terminar. No compite con PLAY ni con futuros scans.
4. **Limpieza de Workers Anteriores**: Al inicio de cada scan, se destruye cualquier Worker huérfano de sesiones anteriores.

### 🎓 Lecciones Aprendidas
- **WASM Linear Memory Solo Crece**: La especificación WebAssembly 1.0 no permite reducir la memoria. Cada inferencia ONNX dentro del Worker puede solicitar más páginas WASM vía `memory.grow()`, pero nunca las devuelve. Para archivos largos, esto es una bomba de relojería.
- **`terminate()` Es Necesario Pero Insuficiente**: Matar el Worker libera la memoria WASM eventualmente, pero iOS necesita un respiro real (1.5s) antes de que el Main Thread intente cualquier operación que aloque memoria (como redibujar el canvas).
