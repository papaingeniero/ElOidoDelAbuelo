## 🚀 Memory Leak Fix: Caché Huérfano en Purgado v1.4.54 | 08/03/2026

### 📜 El Problema
El sistema de purgado de historial ubicado en NanoHTTPD (`DELETE /api/recordings`) estaba configurado originalmente para destruir puramente contenedores de audio base (`.wav`, `.m4a`, `.aac`). Tras la reciente integración del sistema de Caché VAD Zero-Shot y del autogenerador de picos de audio, se introdujo en la arquitectura un subsistema intensivo de metadatos guardados como extensiones adyacentes de disco (`.json`, `.vad.json`).
Un borrado masivo invocaba la aniquilación de los audiorrompecabezas madre, pero **no listaba ni purgaba los archvos de telemetría IA**, creando un inmenso agujero negro en la memoria caché del dispositivo con miles de archivos JSON huérfanos eternos que colapsaban silenciosamente los recursos del explorador de Archivos eMMC del Xiaomi.

### 🛠️ La Solución
Corrección matemática literal a nivel de directiva sobre EndPoint Native Java:
- El filtro condicional asíncrono `.listFiles()` ha sido complementado añadiendo un bloque lógico de tipo inclusivo `|| name.endsWith(".json")`. Ahora el proceso recursivo identificará implícita y expresamente todos los artefactos de telemetría de Picos (.json) y Caché Resumption VAD (.vad.json) como cómplices mortales cuando el frontend exige la supresión de la base de datos matriz.

### 🎓 Lecciones Aprendidas
- **Desincronización Arquitectural Backend-Frontend:** Las innovaciones visuales complejas que escalen el disco en fronted (como el VAD) y requieran almacenamiento estructurado en backend deben auditar sistemáticamente sus rutinas de Destrucción de Memoria (`DELETE / Purgado`). En sistemas integrados hostiles perennemente expuestos como este Xiaomi, dejar un archivo flotando equivale a devorar la estabilidad general.
