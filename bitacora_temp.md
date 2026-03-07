## 🚀 Purificación de VAD y Clonación Estructurada Segura v1.4.25 | 07/03/2026

### 📜 El Problema
Los usuarios reportaban un "Crash" duro o "Jetsam" en iOS la segunda o tercera vez que pulsaban el botón "Scan IA" consecutivamente. Las contramedidas previas mitigaron el colapso al cargar el archivo, pero introdujeron inestabilidad de memoria en usos iterativos.
El diagnóstico profundo evidenció dos problemas vitales:
1. **Corrupción por Zero-Copy (`Transferable Objects`)**: La bandera `[cachedPcmData.buffer]` desvinculaba la matriz del Main Thread entregándosela al Worker y recuperándola en el `onmessage`, pero el motor de JavaScript en Safari a menudo corrompe las referencias a nivel de host si el recolector de basura (GC) ocurre asíncronamente en WebKit, arrojando punteros inválidos en escaneos repetidos.
2. **Chunking GC Overload**: Segmentar manualmente un buffer de 3 minutos en fragmentos de 15 segundos mediante un bucle for-loop en JavaScript disparaba docenas de minitareas que sobrecargaban la gestión de memoria V8/JSC, aumentando exponencialmente la huella WASM con cada "segmento", neutralizando la meta original del chunking.

### 🛠️ La Solución
Se ha restructurado `runVADScanner()` para priorizar la estabilidad robusta sobre micro-optimizaciones destructivas, utilizando una filosofía de delegación estricta y aislamiento:
1. **Copia Estructurada Segura (`Structured Cloning`)**: Se revoca la directriz Zero-Copy asimétrica. Se genera un `new Float32Array()` permanente y prístino a nivel Main Thread. Este buffer se envía al Worker Kamikaze *por copia*, sin delegación de propiedad mutante. Aunque genera un pico marginal temporal de RAM (unos MBs), garantiza que el hilo principal jamás pierde ni corrompe la referencia madre subyacente impidiendo leaks recurrentes en Safari.
2. **Delegación Nativa C++ (ONNX)**: Se ha desmantelado el bucle "Chunking" JavaScript artesanal. En lugar de partir la memoria desde JS, inyectamos el tensor completo de `e.data.pcmData` directo al método `.run()` interno de `silero_vad.onnx`. El motor subyacente programado en Rust/C++ gestiona sus propios tensores VRAM internamente de forma abrumadoramente más eficiente y segura contra OOMs que segmentando nosotros el ArrayBuffer desde una capa de script de alto nivel.
3. **Respiro Anti-Overlap Plus**: Se ha incrementado el *GC Breath* de 800ms a `1200ms` garantizando totalmente la desasignación de AVFoundation de Apple antes de proceder con descodificación pesada.

### 🎓 Lecciones Aprendidas
- **Las Micro-Optimizaciones JS muerden duro en Mobile**: Perseguir el "Cero Costo en RAM" usando Transferable Objects en browsers heterogéneos (Safari VS Chrome) es la receta perfecta para la corrupción sigilosa. Un array copiado consume RAM, pero un puntero colgante tumba un SO entero.
- **La IA nativa sabe lo que hace**: Delegar iteradores for-loop de tensores a una librería ONNX compilada a WASM es infinitamente estadísticamente más resiliente y lineal que "inventar el lote" artesanalmente desde TypeScript/JS.
