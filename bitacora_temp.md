## 🚀 Extreme Safari OOM Patch [v1.4.13] | 07/03/2026

**📜 El Problema:**
1. **El Gusano de WebKit:** A pesar de haber matado el Daemon de Medios (`<audio>.load()`), abrir un segundo archivo y pulsar "Analizar IA" volvía a detonar un Jetsam Panic. Tras una investigación táctica en los motores de Apple, descubrimos dos fugas crónicas de memoria interna en Safari originadas por la API de `AudioContext`:
   - Instanciar y cerrar múltiples `AudioContext` en una misma página en Safari deja "colas fantasma" en la RAM.
   - Guardar el resultado `audioBuffer.getChannelData(0)` engancha estructuralmente la memoria a todo el envoltorio del mega-buffer de WebKit. Un audio de 5 minutos ocupa 20MB de array, pero WebKit atrapa 100MB de buffer padre que no se pueden borrar porque el array hijo (`pcmData`) sigue vivo siendo masticado por la IA.

**🛠️ La Solución:**
1. **Singleton de Audio Global:** El contexto de descodificación nativo `audioCtx` ahora sobrevive en el scope global (`globalAudioCtx`). Reutilizamos siempre las mismas entrañas de WebKit para todos los archivos, evitando instanciar y detonar daemons innecesarios.
2. **Clonación Forense de Matrices (Deep Clone):** Al obtener los bytes para la IA (`getChannelData`), ya no hacemos una referencia cruzada. He forzado la clonación dura: `let pcmData = new Float32Array(tempAudioBuffer.getChannelData(0))`. Esto empaca los datos al vacío y nos permite asesinar explícitamente el gargantuesco padre (`tempAudioBuffer = null`). WebKit lo entiende y purga la RAM antes de que ONNX siquiera empiece a calentarse.

**🎓 Lecciones Aprendidas:**
- Cuando luchas contra WebKit Edge-AI en dispositivos móviles, un simple signo de igual (`=`) entre variables no copia información, transfiere un anclaje mortal de memoria compartida (Shallow Copy). Romper las referencias copiando bit a bit los Arrays es imperativo para evitar que las optimizaciones fantasma del Safari maten tu aplicación por castigo de la RAM (OOM).
