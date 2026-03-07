## 🚀 Fusión Maestra: Chunking + GC Breath + ZeroCopy v1.4.22 | 07/03/2026

### 📜 El Problema
Durante la implementación del Chunking en la `v1.4.22-dev.1`, se sobrescribió accidentalmente la lógica vital del `"GC Breath"` (la pausa para destruir AVFoundation) y la caché `Zero-Copy` que evitaba duplicar la RAM. Esta regresión provocó que, aunque el Chunking funcionaba aislando la RAM de WASM, la RAM total del tab en Safari iOS explotara nuevamente (`Jetsam OOM`) por el solapamiento del búfer multimedia nativo activo con el proceso de Web Worker. Además, al destruir silenciosamente la etiqueta `<audio>` para purgar RAM, el cabezal de reproducción desaparecía temporalmente de la UI.

### 🛠️ La Solución
Se ha implementado una arquitectura de "Fusión Maestra" en el `runVADScanner` que une las tres principales contramedidas Anti-OOM:
1. **GC Breath (Respiración de Purga)**: Se destruye el objeto `<audio>` y se inyecta una pausa explícita (`await new Promise(resolve => setTimeout(resolve, 800))`) dándole tiempo al Garbage Collector del sistema base de iOS (y a WebKit) para asimilar la liberación masiva de RAM del motor AVFoundation antes de despertar al gigante de WebAssembly.
2. **Caché Zero-Copy Intacta**: El PCM extraído se desvincula desde el tab principal mandándose como *Transferable Object* hacia el Web Worker (`[cachedPcmData.buffer]`). Una vez que el motor ONNX termina la inferencia, se vuelve a transferir *íntegro* al Worker principal para una reutilización instantánea sin duplicar el peso en la memoria del navegador.
3. **Chunking Constante**: Manteniendo la carga lineal de RAM al procesar fragmentos de 15 segundos dentro de la inferencia IA.
4. **Preservación Visual (Cabezal Fantasma)**: Se ha modificado `drawForensicWaveform` para que, a pesar de que la fuente real nativa (`forensicAudio`) sea destruida temporalmente, el motor de Canvas lea el offset almacenado (`window.vadSavedTime`) y lo dibuje permanentemente para que el usuario jamás perciba la manipulación de recarga táctica.

### 🎓 Lecciones Aprendidas
- **Las contramedidas no son excluyentes, son acumulativas**: Resolver un problema en WASM (Chunking) no permite olvidar los problemas estructurales del host (AVFoundation Overlap). En plataformas empobrecidas como WebKit iOS, un `run()` pesado requiere silenciar el ecosistema circundante entero. Y darle tiempo (`setTimeout`) para que la RAM física respire.
