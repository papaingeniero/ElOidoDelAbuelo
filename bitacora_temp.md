## 🚀 v1.3.0 Grabación Programada y Escalas Analíticas | 05/03/2026

**📜 El Problema (Contexto Histórico):**
1. **Delegación Temporal:** El usuario necesitaba poder indicar al dispositivo que grabase el audio ambiente en un lapso futuro sin estar presente para pulsar el botón manualmente. Las grabaciones automáticas por decibelios (Spike) no bastaban si el ruido a vigilar era bajo pero sostenido.
2. **Despliegue UI de Estado Hostil:** Al forzar el reloj interno del móvil a grabar, el backend devolvía al frontend un "Grabar = True", colisionando gráficamente en un cartel rojo de `GRABACIÓN MANUAL FORZADA` que generaba gran confusión.
3. **Escala Analítica Muerta:** En la validación forense de pistas previas, las ondas se dibujaban aisladas. La mente humana no lograba decodificar la magnitud acústica de los valles sin una cuadrícula matemática que apoyase la visión.

**🛠️ La Solución:**
1. **Modal de Programación:** Se diseñó un temporizador (Hora de Inicio y HH:MM de duración) enviando Intents al `AlarmManager` para despertarse matemáticamente vía PendingIntent en background.
2. **Subyugación de Threads:** Se estableció un orden jerárquico. Las grabaciones automáticas son abortadas y sobreescritas si chocan con una grabación programada. Las programadas son ignoradas si el usuario ya está grabando manualmente en primer plano.
3. **Segmentación y UI:** El backend emite ahora un flag `isScheduledRecording=true`. El Dashboard lo intercepta y pinta un amistoso escudo ambarino `🟡 ESTADO: GRABACIÓN PROGRAMADA ACTIVA` para diferenciar a las máquinas del humano.
4. **Onda Cuartil Estética (El Eje Y):** La forma de onda del Canvas se hace zoom al 100% de alto y superpone 3 líneas base horizontales al 25%, 50% y 75% del volumen del pico máximo del audio en curso, rotulando los márgenes para calibrar cada evento auditivo a una escala de bolsillo propia.

**🎓 Lecciones Aprendidas:**
- Renuncia al `<input type="time">` de HTML para "Duraciones". Los navegadores se empeñan en convertirlos en "Horas del Día" (AM/PM) inyectando selectores inusables.
- Visualización de Datos Cíclica: El Zoom siempre debe prevalecer sobre la escala. El Eje Y debe doblarse siempre sobre sí mismo para que una onda ínfima se dibuje enorme y llene el frame, pintando sus 3 marcas de nivel encima de su pico, no debajo de barreras abstractas vacías.
