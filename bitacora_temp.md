## 🚀 Desbloqueo de Scroll en iOS (Consola) [v1.4.8] | 07/03/2026

**📜 El Problema:**
1. **Trampas de Desplazamiento (Scroll Traps) en WebKit:** Inmediatamente después del lanzamiento de la Consola Web de Logs (v1.4.7), las interfaces táctiles en iPhones (Safari iOS) reportaron fallos críticos de interacción. Al tocar el bloque `<pre>` que renderiza el historial del sistema, la pantalla no bajaba. Esto sucedía porque Safari entra en pánico funcional cuando dos contenedores FlexBox anidados compiten con las directivas de `overflow-y: auto`.

**🛠️ La Solución:**
1. **Aislamiento de Cajas Negras CSS:** Intervención directa en el DOM nativo (`index.html`). Primero se equipó el contenedor interior (`<pre id="logsContainer">`) con aceleración táctil de hardware propietaria de Apple (`-webkit-overflow-scrolling: touch`) y confinamiento de rebote (`overscroll-behavior: contain`).
2. **Mutilación de Herencia en el Padre:** El `.modal-content` estructural de la ventana global tenía un comportamiento de scroll heredado implícito. Se le forzó un `overflow: hidden;` en línea. Ahora, Safari entiende orgánicamente que el desplazamiento "pertenece" legal y exclusivamente al bloque de texto verde oscuro y transfiere la fricción del dedo perfectamente.

**🎓 Lecciones Aprendidas:**
- Cuando desarrolles en el stack tecnológico moderno, las reglas de FlexBox en Google Chrome no aplican en Apple Safari bajo los mismos axiomas mecánicos. Las aplicaciones Web-Móviles híbridas exigen un cuidado enfermizo con los `overflow`, donde "congelar a los abuelos para permitir que los nietos bailen" es la única topología CSS táctil robusta.
