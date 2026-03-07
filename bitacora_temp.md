## 🚀 Strict Lazy Load y Prevención OOM v1.4.23 | 07/03/2026

### 📜 El Problema
La "Fusión Maestra" en la versión `v1.4.22` introdujo un defecto sutil de concurrencia de memoria: la función pre-instanciaba automáticamente la etiqueta `<audio>` de AVFoundation (`initForensicAudio()`) desde el bloque `finally` justo después de ordenar la terminación del Web Worker. Esto creaba una carrera de destrucción/creación donde Safari iOS requería alojar de nuevo la memoria RAM nativa *mientras* WebAssembly aún estaba asimilando su propio Garbage Collection de terminación, desembocando en otro pico de Jetsam OOM.

### 🛠️ La Solución
Implementación de la política de Carga Perezosa Estricta (Strict Lazy Load):
1. **Delegación de Instanciación**: Se ha prohibido rotundamente "auto-resucitar" la etiqueta `<audio>` al finalizar la inferencia de la IA. El flujo visual de la onda dependerá únicamente de los offsets (`window.vadSavedTime`) para sostener un cabezal virtual.
2. **Lazy Initialization al Play**: El reproductor nativo de audio sólo se cargará e instanciará en RAM física cuando el usuario explícitamente vuelva a pulsar el botón manual de `PLAY`, garantizando que para entonces el subsistema WebAssembly ya ha liberado totalmente su huella de memoria.
3. **Restauración con Delay Analógico**: El evento `onloadedmetadata` de la nueva fuente de audio asume ahora la responsabilidad de restaurar `window.vadSavedTime` y llevar el cabezal a la posición pausada en el instante mismo de la petición de *Play*.

### 🎓 Lecciones Aprendidas
- **El Garbage Collection no es síncrono**: Invocar `worker.terminate()` flaggea los bloques de memoria, pero el recolector interno puede tardar varios frames en recuperarla del todo. Reservar RAM crítica (`new Audio()`) justo en la línea siguiente es invitar a un OOM por solapamiento de umbrales. En un móvil empobrecido, las reservas costosas deben someterse siempre a una iteración basada en el "Gesto del Usuario" diferido.
