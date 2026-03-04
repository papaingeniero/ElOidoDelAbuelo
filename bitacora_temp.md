## 🚀 Mantenimiento y Saneamiento (v1.1.1) | 04-Mar-2026
### 📜 El Problema
Tras la liberación oficial de la v1.1.0, procedimos con una auditoría de rendimiento y código:
1. Se detectó actividad innecesaria de recolección de basura (GC Thrashing) en el ciclo vital de `AudioSentinel.java`. En cada vuelta del bucle se re-declaraban arrays de bytes `new byte[]`, castigando al procesador y la batería en el largo plazo.
2. Deuda Técnica en `OidoService.java`: El `PendingIntent` de la notificación no declaraba correctamente los flags de inmutabilidad, corriendo el riesgo de crashear el servicio vital si subimos el Target SDK a Android 12 (API 31).

### 🛠️ La Solución
- **Anti GC-Thrashing**: En el `AudioSentinel.java` se optó por instanciar un único array `reusableByteBufferOut` a nivel pre-bucle. Ahora sus índices de memoria son machacados y reciclados de forma atómica (*in-place*), neutralizando las llamadas extra al Garbage Collector.
- **PendingIntent Strictness**: Se inyectó estrictamente `PendingIntent.FLAG_IMMUTABLE` en el código de notificación de `OidoService.java`.

### 🎓 Lecciones Aprendidas
- **Optimización de Recursos**: A la hora de construir bucles asíncronos en Java que se ejecutan infinitamente buscando la máxima retención de batería, es un dogma reservar toda la memoria posible *out-of-the-loop* y limitarse a sobreescribir direcciones de memoria internamente.
