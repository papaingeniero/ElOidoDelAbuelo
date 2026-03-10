## 🚀 Detección Inteligente: Aislamiento Chrome iOS (CriOS) v1.4.57 | 10/03/2026

### 📜 El Problema
El "Patrón de Suicidio Controlado" (recarga dura de WebKit cada 20 Chunks) fue diseñado drásticamente para doblegar al guardián de Memoria de Safari (Jetsam). Sin embargo, pruebas de estrés empíricas sobre el terreno demostraron que Google Chrome para iOS (`CriOS`), a pesar de utilizar por debajo el mismo motor WebKit obligado por Apple, gestiona sus rutinas de Garbage Collection o buffers internos de manera diferente, logrando procesar archivos inmensos sin asfixiarse y sin requerir amputaciones tácticas de recarga forzada en la UX del usuario.

### 🛠️ La Solución
Se ha bifurcado sutilmente la arquitectura de evasión táctica dentro del core `_executeVadScan`:
- Se introdujo una `DETECCIÓN INTELIGENTE AVANZADA` basada en análisis cruzado de `navigator.userAgent`.
- Si el cliente reporta ser la plataforma genérica `isIOS` pero simultáneamente incluye la rúbrica `CriOS` de Google, el sistema le concede libertad absoluta.
- La matemática dictamina: `const WASM_ROTATION_LIMIT = (isIOS && !isChromeIOS) ? 20 : Infinity;`.
- De este modo, Safari nativo se sigue sometiendo al reseteo y "Suicidio" al chunk 20, mientras que el usuario avanzado que accede vía Google Chrome en iPhone/iPad no sufrirá parpadeos visuales ni auto-recargas asíncronas, procesando todo del tirón hasta el final del audio.

### 🎓 Lecciones Aprendidas
- **WebKit no es Universal bajo iOS:** Un viejo dogma asume que "Todo navegador en iOS es un clon exacto atado de manos de Safari". Aunque Apple imponga su motor de renderizado, las capas superiores de Google (o Firefox) introducen gestores de recursos independientes o flags que mutan sustancialmente el límite de tolerancia de la App. Mide en campo el umbral de cada actor individual antes de aplicar medidas de contención suicida a gran escala.
