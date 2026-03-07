## 🚀 AudioContext Strict Annihilation v1.4.18 | 07/03/2026
### 📜 El Problema
Los Jetsam (Out-Of-Memory crasheos) regresaron en iOS Safari debido a una contención microscópica. Aunque liberábamos el Worker de manera impecable, el cordón umbilical del procesamiento de ondas (`localCtx`) se anudaba a la memoria compartida impidiendo un barrido de recolección de basura eficiente, ahogando al dispositivo nativo en RAM retenida por buffers previos decodificados.

### 🛠️ La Solución
He aplicado la demolición exacta y cronometrada de todos los punteros multimedia:
1. **Asesinato Precursor**: La línea `await localCtx.close()` se ejecuta fulminantemente de inmediato tras expropiar un `.slice()` del array de canal, garantizando desconexión nativa.
2. **Transferencia Destructiva**: Se anula formalmente a los portadores (`arrayBuffer`, `tempAudioBuffer`, `localCtx`) estableciéndolos en `null` previamente de concebir al instanciador del Sandbox (`vadWorker`).

### 🎓 Lecciones Aprendidas
- **Desgarro Limpio en WebKit**: Extraer DataCruda mediante `.slice()` y apuñalar instantáneamente al `AudioContext` en vez de esperar al tramo de ejecución WebAssembly final reduce a un instante efímero la ventana de pico expansivo en RAM, cortando el apalancamiento que lleva a los fatídicos *Jetsam Panics* de iOS Safari al escanear audios encadenados.
