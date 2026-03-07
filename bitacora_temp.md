## 🚀 Hotfix VAD Web API (Silero) [v1.4.10] | 07/03/2026

**📜 El Problema:**
1. **Colapso del Motor (API Obsoleta):** Al pulsar el botón "Analizar Voces (IA)", el navegador arrojaba un "Error al ejecutar VAD". Detrás de esta alerta, la realidad era que el objeto `vad.utils.processAudio` no existía en la capa de FrontEnd del CDN (v0.0.19) debido a cambios arquitecturales de WebAssembly. Además, el Runtime de ONNX (`ort-wasm.wasm`) intentaba descargarse desde el NanoHTTPD local (`/ort-wasm.wasm`), detonando un 404 estrepitoso por parte de nuestro servidor perimetral Xiaomi, ya que ese archivo no exite en los Assets. 

**🛠️ La Solución:**
1. **Arqueología NPM Táctica:** Bajamos directamente de NPM el tarball de la versión `0.0.19` y leímos sus cabeceras TypeScript (`non-real-time-vad.d.ts`). Sustituimos el inyector imaginario por el correcto instanciador `vad.NonRealTimeVAD.new()`, configurando un Bucle Generador Asíncrono (`AsyncGenerator`) que escupe los fotogramas de habla.
2. **Override del Entorno Wasm (ONNX):** Interceptamos la variable global de Microsoft `window.ort.env.wasm.wasmPaths` e inyectamos a fuego el prefijo HTTPS del CDN JSdelivr, engañando al motor para que busque sus pesados binarios de red neuronal en la nube de alta disponibilidad y no en nuestro frágil Android Centinela.
3. **Manejo Dinámico de Tiempo:** Asegurada conversión de milisegundos nativos del VAD-Web a segundos fraccionales requeridos por la regla de medición de nuestro Canvas Táctico.

**🎓 Lecciones Aprendidas:**
- Cuando desarrollas IA Frontend contra CDNs en dispositivos Edge sin npm/node local, nunca asumas que la librería sigue tu lógica deductiva. Descargar el empaquetado directo y leer el `index.d.ts` sobre la marcha ahorra horas de frustración contra cajas negras.
