## 🚀 ADB Watchdog: Deep Ping (Data Spoofing) v1.4.58 | 11/03/2026

### 📜 El Problema
El "Doze Mode" de MIUI en Xiaomi es legendariamente agresivo. Se ha descubierto que el demonio `adbd` (ADB Daemon) de Android es lo suficientemente astuto (o vago) como para ignorar conexiones TCP vacías (`Socket.connect` sin datos). Un mero "TCP Handshake" al puerto 5555 no obliga al demonio a consumir tiempo de CPU procesando el buffer de entrada, permitiendo con el tiempo que las optimizaciones de batería de Xiaomi congelen el proceso por inactividad.

### 🛠️ La Solución
Implementación de un **Auto-Desfibrilador Local Profundo (Data Spoofing)** en el corazón de `OidoService`.
- Se ha inyectado un hilo de muy baja prioridad `adbWatchdogThread` directamente en el latido del servicio.
- Cada 60 segundos, el hilo abre un socket hacia `127.0.0.1:5555`.
- **El Cubo de Agua Fría**: Inmediatamente tras conectar, escribe por el `OutputStream` los bytes basura `"HELO".getBytes("UTF-8")` y ejecuta un `flush()`.
- Se mantiene la conexión abierta artificialmente durante `500ms` antes de cerrarla. Esta retención combinada con el volcado de datos falsos de protocolo ("spoofing") fuerza físicamente al demonio ADB a despertar, asignar CPU, tragarse la trama, darse cuenta de que no es un paquete ADB válido y rechazarla. 
- Este esfuerzo termodinámico inútil por parte de `adbd` garantiza que el sistema operativo registre picos de consumo de CPU por parte de un proceso del sistema (ADB), rompiendo microscópicamente el Deep Sleep del Doze Mode cada minuto y manteniendo vivo indirectamente al `OidoService`.

### 🎓 Lecciones Aprendidas
- **Un toque en la puerta no basta si no gritas:** Para despertar a un demonio de sistema (como ADB) desde un servicio local y evadir los asesinos de RAM, no basta con hacer ping a su puerto TCP. Debes forzarlo a procesar información corrupta ("Data Spoofing") reteniendo el socket. El coste de CPU de parsear el error es exactamente la chispa eléctrica que necesitamos para mantener los corazones latiendo en Android 10.
