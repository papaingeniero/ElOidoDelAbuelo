## 🚀 Fix WebKit Jetsam Crash (VAD OOM) [v1.4.11] | 07/03/2026

**📜 El Problema:**
1. **Fugas de Memoria y Suicidio iOS:** Tras integrar la API real del `NonRealTimeVAD.new()` (v1.4.10), ejecutar el escáner sobre una grabación y luego intentar interactuar abriendo una segunda, el visor (modal) "explotaba" y devolvía al usuario al dashboard principal de manera brusca. Este no era un error de JS trivial; Safari iOS estaba forzando un reinicio reactivo (Jetsam Panic) para proteger la RAM física del iPhone.
2. Cada pulsación en el Analizador de IA recetaba la inicialización un nuevo hilo sinfín de `AudioContext` en el core Unix de Apple, y peor aún, volcaba ~300MB de memoria WebGL/WASM para inflar una nueva sesión abstracta del Tensor `onnxruntime` sin limpiar la del archivo anterior. 

**🛠️ La Solución:**
1. **Singleton de Tensor Global:** Se encapsuló la instancia pesada de la Red Neuronal tras la variable `globalVAD`. Si `globalVADThreshold` no varía en la UI, la aplicación ignora re-instanciar el constructor y recicla la sesión pre-calentada inyectándole el vector flotante en crudo del nuevo archivo (`globalVAD.run(pcmData)`), rebanando los recursos a cero.
2. **Sacrificio de AudioCore:** Introducida la directiva forzosa `await audioCtx.close()`. A diferencia de Chromium, el Recolector de Basura (Garbage Collector) de Safari es letárgicamente vago con los nodos de audio huérfanos. Cerrarlos tras usarlos para decodificar bytes `pcmData` detiene la fuga termodinámica de hardware concurrente. 

**🎓 Lecciones Aprendidas:**
- Cuando WebKit detona y "te manda de vueltas a la principal", 99 de 100 veces rompiste su techo de RAM. El lado oscuro del Edge AI en Javascript es que todo el peso computacional cruza de los fierros del Servidor Cloud hacia la memoria castigadísima de un teléfono móvil. Cuidar los punteros a nivel Bit en JS no es opcional, es dogma de supervivencia.
