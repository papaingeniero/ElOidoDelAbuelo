## 🚀 Invalidación de Caché VAD Dinámica v1.4.55 | 08/03/2026

### 📜 El Problema
Con la arquitectura del Zero-Shot VAD operando a pleno rendimiento, se descubrió un fallo en el flujo de Trabajo de UX: si un usuario abría un audio 100% analizado y decidía que el umbral de detección (threshold) no era el adecuado (demasiado sensible o muy restrictivo), y movía el control deslizante, el sistema de caché persistente de Android bloqueaba cualquier intento humano de análisis. Safari simplemente recargaba al instante el lienzo rojo antiguo cacheado de esa pista que se cargó con la orden `window.vadCheckpointData.complete`. El usuario quedaba imposibilitado de recalcular el audio.

### 🛠️ La Solución
Inyectado un mecanismo de purga nativa asociado al hardware de la interfaz web (`inpVadThresh`):
- Se ha encadenado un `addEventListener('input')` directamente al Slider de Umbral VAD.
- A la mínima detección de alteración en el input por parte del dedo del usuario, la lógica interviene aniquilando forzosamente el objeto `window.vadCheckpointData` en el Runtime de WebKit. 
- La matriz local de dibujos rojos (`vadSegments = []`) es purgada en memoria y el render del lienzo Canvas (`drawForensicWaveform`) es llamado al instante para resetear visualmente a la pista base gris. El botón transmuta visualmente del "Caché Instantánea" estático al texto vital "Re-Analizar (Nueva Sensibilidad)", quedando desbloqueado y re-habilitando el `runVADScanner` limpio para escupir un nuevo dataset.

### 🎓 Lecciones Aprendidas
- **Los Cachés Totales son Prisiones Inflexibles:** Evita bloquear las intenciones del panel de control si intervienen cachés predictivas. Si introduces una caché de autocompletado rígido (Zero-Shot), tu primera prioridad es dotar al humano de un botón o gatillo explícito para profanarlo e invalidarlo a voluntad. Un caché ciego es UX Hostil.
