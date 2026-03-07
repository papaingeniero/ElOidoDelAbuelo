## 🚀 Hotfix OOM Safari Media Leak (VAD) [v1.4.12] | 07/03/2026

**📜 El Problema:**
1. **El Asesino Silencioso de WebKit:** A pesar del Singleton asíncrono para ONNX implantado en la versión anterior (1.4.11), investigar el segundo audio de Voice Activity seguía tumbando Safari. El problema residía en **el cementerio de variables no recolectadas de JavaScript**: retener un Array de Floats (`pcmData`) de cientos de megabytes en el Heap sin ponerlo a *null*, sumado al buffer nativo de la etiqueta `<audio>` de iOS que se negaba a vaciarse cuando cerrábamos la modal (poner `src = ""` no la mata en Apple).

**🛠️ La Solución:**
1. **Ejecución Forzada de Garbage Collector (`Nulling`):** Tras completar la digestión del Generator Asíncrono de la Red Neuronal y pintar las rayas paramétricas en rojo, se ordena la aniquilación de la matriz principal: `pcmData = null; arrayBuffer = null;`. Le robamos la variable a la máquina de JS para que el GC la recoja al toque.
2. **Purgado del Media Daemon:** Al pulsar "Cerrar" en la Modal Forense de un archivo, se exige al DOM ejecutar `forensicAudio.removeAttribute('src')` y acto seguido se llama al método `forensicAudio.load()`. Esto le dispara un comando de vaciado (flush) irrechazable al core de medios de Safari para ese tag específico, perdiendo instantáneamente los Megabytes de PCM descodificado antes de que abramos otro archivo diferente.

**🎓 Lecciones Aprendidas:**
- iOS Safari no sabe hacer "Garbage Collection" sobre medios (Audio/Video). Debes castigarlo explícitamente obligándole a recargar un reproductor vacío (`.load()`), o de lo contrario apilará 2, 3 o 4 canciones descodificadas enteramente en su RAM virtual antes de cometer un harakiri (Jetsam Crash). Trabajamos en Edge AI, cada megabyte es oro.
