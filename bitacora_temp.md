## 🚀 Blob Worker URL Fix v1.4.17 | 07/03/2026
### 📜 El Problema
Tras la monumental hazaña de contener a Safari encapsulando todo el Motor VAD en un Sandbox Web Worker (v1.4.16), nos enfrentamos a las consecuencias de haber instanciado ese código nativo desde el vacío de un Objeto de Dominio Local (`new Blob`). Este objeto efímero no cuenta con una "Carpeta Raíz" (Base URL); por lo tanto, cuando el núcleo neuronal precompilado de `@ricky0123/vad-web` pretendía descargar su pesada matriz tensorial (`silero_vad.onnx`) llamando a `/` (su padre absoluto), colapsaba exclamando categóricamente `URL is not valid`.

### 🛠️ La Solución
Intervención estructural y forzado directo de rutas. Se han inyectado en la API del Worker parámetros rigurosamente acotados dentro del generador principal de instanciación VAD:
1. `modelURL` establecido apuntando tajantemente al dominio principal HTTPS de `jsdelivr`.
2. `workletURL` redirigido para blindar posibles fugas al importar submódulos paralelos.

### 🎓 Lecciones Aprendidas
- **El Vacío Terapéutico del Blob**: Construir un Worker inyectándole Strings desde JavaScript salva vidas en entornos sin empaquetadores como WebPack o Node JS. Sin embargo, roba a las herramientas foráneas de todo rastro contextual. Proporcionar "Coordenadas UTM" (`modelURL`) es un deber inapelable al tratar con bibliotecas remotas estáticas a nivel Frontend.
