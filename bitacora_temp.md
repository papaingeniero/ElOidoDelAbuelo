## 🚀 Ajuste de Responsividad (Botonera) [v1.4.4] | 07/03/2026

**📜 El Problema:**
1. **Desbordamiento Flexbox en iOS:** Al revisar la nueva matriz táctil de saltos de tiempo prolongados (v1.4.3), Safari Mobile en pantallas contenidas (ej. iPhone 15) renderizaba al menos los dos últimos botones (`+20s` y `+30s`) en una segunda línea, reventando el *layout* vertical del reproductor y perdiendo la estética de "panel de control único". El atributo original `flex-wrap: wrap` junto con anchos de *padding* muy altos (12px) provocaban este desbordamiento.

**🛠️ La Solución:**
1. **Contención Horizontal Rigurosa:** Se reescribió la estructura `.css` integrada de la botonera inferior de Scrubbing. Se reemplazó el `flex-wrap: wrap` por `white-space: nowrap`, se constriñó el botón con propiedes `flex: 1` y `max-width: 55px`, y se comprimieron dramáticamente sus rellenos (`padding: 8px 4px`). Esta geometría obliga al motor WebKit de Safari a agrupar con consistencia perfecta los seis botones en una única línea transversal, maximizando el espacio útil del teléfono del investigador.

**🎓 Lecciones Aprendidas:**
- Lo que cabe de sobra en el renderizador de sobremesa y en los densos paneles de Android a veces choca frontalmente con los densos píxeles independientes de Apple (`pt`). Nunca hay que dar la responsividad por garantizada a base de magia flex sin forzar límites nativos (`max-width`) y empaquetados forzosos o un test físico en el navegador safari móvil final.
