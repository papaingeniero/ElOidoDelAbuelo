## 🚀 Legibilidad Forense y HUD Bi-Línea [v1.4.2] | 07/03/2026

**📜 El Problema:**
1. **Contraste de la Regla Temporal**: Tras la implementación del Eje X de Tiempo Absoluto (v1.4.1), los usuarios reportaron que el texto proyectado en el Canvas perdía viabilidad óptica. El color predeterminado estaba programado con una opacidad reducida del 40% (`rgba(255, 255, 255, 0.4)`), lo que provocaba que frente a grabaciones muy densas (barras apretadas del espectrograma), el texto de los minutos y segundos quedara enterrado e ilegible.
2. **Exceso de Información en Línea**: El contador de tiempo de reproducción posicionado debajo del visualizador Canvas (`waveTimeDisplay`) encadenaba el Tiempo Transcurrido y la Duración Total en un mismo string amorfo de texto (`00:00 / 03:00`). Era funcional, pero matemáticamente inconexo con el recién introducido Eje X de Tiempo Absoluto del Canvas superior.

**🛠️ La Solución:**
1. **Contraste Quirúrgico (Canvas)**: Se ha sobreescrito la propiedad `ctx.fillStyle` del bucle generador de etiquetas temporales en el entorno JavaScript (`drawForensicWaveform`) elevando su canal Alfa al 100% de solidez. Ahora las marcas temporales se trazan en Blanco Puro (`rgba(255, 255, 255, 1.0)`), rasgando visualmente la amalgama de picos sonoros.
2. **Micro-arquitectura HTML Dual**: La función `updateWaveTimeDisplay()` fue rediseñada para expulsar un bloque HTML formateado de dos niveles en lugar de `innerText`. En el fotograma superior y teñido de un tono Ámbar (`#f5a623`) de alto contraste, se ha conectado el puntero temporal del `forensicAudio.currentTime` re-encapsulándolo contra el `waveBaseTimestamp` global. Esto produce un cronómetro HUD nativo que informa de la **Hora Exacta del Día (HH:MM:SS)** de lo que el analista está escuchando, relegando el progreso relativo del archivo a una línea subatómica inferior `00:00 / 03:00` difuminada al 80%.

**🎓 Lecciones Aprendidas:**
- Cuando elevas un sistema analítico desde relacional (empezar en cero) a absoluto (horas del mundo real), toda la Interfaz de Usuario aledaña sufre disonancia cognitiva si no sube en la misma "gravedad semántica". Unificar la matemática del visor de horas superior con las variables de base del componente Canvas ha integrado herméticamente la experiencia de rastreo forense.
