## 🚀 Protocolo Fénix (Auto-Resume UI) v1.4.53 | 08/03/2026

### 📜 El Problema
La arquitectura de Checkpointing salvaba los datos matemáticos a la perfección tras un Jetsam Crash de iOS Safari. Sin embargo, la Experiencia de Usuario (UX) quedaba herida: el usuario volvía a una página estática, perdía el scroll de la pista que estaba analizando y tenía que reabrir manualmente el cajón de gráficas para pulsar laboriosamente el botón "Continuar Análisis". 

### 🛠️ La Solución
Implementación formal del **"Protocolo Fénix"**:
1. **Intención de Escaneo (`localStorage`):** Al latir `runVADScanner()`, se graba en caché profunda el nombre del archivo activo bajo la llave `activeVadScan`. Esta llave actúa como un contrato de promesa de análisis. Se elimina sólo cuando la función cruza la línea final de éxito `vadSegments = segments;`.
2. **Auto-Navegación Táctil (`loadHistory`):** Tras reconstruir el DOM en un Reload, la lista lee la llave huérfana. Con un `setTimeout(..., 500)`, ejecuta un `scrollIntoView` certero que localiza la tarjeta magnética en la lista y lanza remotamente `openWaveform(activeScan)` replicando el toque del usuario humano.
3. **Auto-Ignición (Zero-Click Resumption):** El brazo asíncrono `fetch('/api/vad_load')` intercepta la carga de la pista fantasma. Si el protocolo Fénix chivata que ese archivo reventó en mitad del scan anterior, el botón cambia a *🔄 Autorecuperando...* y acciona el escote WASM automáticamente mediante `setTimeout(() => runVADScanner(), 800)`.

### 🎓 Lecciones Aprendidas
- **La Persistencia Frontend Completa el Backend:** De nada sirve que un Servidor Java retenga un archivo de Checkpointing si la UI obliga al usuario a operar manualmente el rescate tras una catástrofe cíclica de memoria. Un Auto-Resume transparente fusiona ambas caras del VAD inyectándole magia al proceso crudo.
