## 🚀 Eje X Absoluto y Renderizado Adaptativo en Canvas [v1.4.1] | 07/03/2026

**📜 El Problema:**
1. **Pérdida de Contexto Temporal**: El análisis forense se volvía confuso porque el visualizador dibujaba la Regla de Tiempo en segundos relativos `(00:00)` partiendo del inicio del archivo, forzando al analista a hacer cálculos mentales constantes para determinar en qué momento de la vida real ocurrió un ruido detectado en un archivo (e.g. `Oido_20260306_112838.m4a`).
2. **Colisiones Tipográficas**: Cuando se realizaba un nivel de Zoom considerable o se comprimía la duración de vista, las etiquetas de los segundos en el Canvas colisionaban entre sí provocando un barullo ilegible de textos estáticos e impidiendo su utilidad práctica.

**🛠️ La Solución:**
1. **Regla de Tiempo Real (Absoluta):** Nueva variable global `waveBaseTimestamp` definida en el intérprete JavaScript. Al invocar `openWaveform`, un subsistema aplica una expresión regular (Regex) al título del archivo buscando rigurosamente la firma nativa temporal `YYYYMMdd_HHmmss`. En base a este Epoch de milisegundos base extraído, el dibujador de Ticks inyecta la hora exacta `HH:MM:SS` (time-travel) donde pertenece cada píxel del canvas, alineando las reproducciones largas o grabaciones programadas sin error humano.
2. **Motor de Etiquetas Adaptativo:** Se demolió el antiguo conector de 3 condicionales estáticos (`tickInterval`) por un algoritmo de prevención de colisiones `maxLabels = width / 65`. Ahora, un Array de escalones limpios o *Nice Steps* `[1, 2, 5, 10, 15, 30, 60...]` es barrido iterativamente sobre el ancho de la pantalla de cristal para determinar la frecuencia de pintado perfecta para las métricas de la onda interactiva asegurando que el string de tiempo jamás se sobreponga consiga mismo.

**🎓 Lecciones Aprendidas:**
- Jamás subestimar la deuda técnica semántica de los ejes cartesianos interactivos creados puramente en Canvas 2D sin apoyo de un SVG o librería. Las operaciones de `ctx.fillText` por cada frame deben limitarse drásticamente para mantener los ansiados 60 FPS en hardware de gama baja como en nuestro Xiaomi destino.
- El fallback condicional defiende el ciclo de vida del WebView de corromperse en iteraciones extrañas y los JSONs legados (si falla nuestra regex) garantizando un downgrade elegante a `Date.now()`.
