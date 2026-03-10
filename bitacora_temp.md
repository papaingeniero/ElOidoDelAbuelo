## 🚀 ADB Watchdog: Telemetría & Optimización (5 Minutos) v1.4.59 | 11/03/2026

### 📜 El Problema
Tras desplegar el "Deep Ping" TCP en el puerto 5555 para engañar a ADB, surgieron dos dudas lógicas de Arquitectura:
1. **Telemetría Ciega**: Al atrapar las excepciones (`catch (Exception e)`) silenciosamente y no llevar la cuenta de los pings, la aplicación sufría de "Caja Negra". Si MIUI mataba o cancelaba el servicio ADB de fondo, o los sockets se corrompían, la app jamás alertaría en el Panel de Control Web de que nos habíamos quedado sin defensa.
2. **Desgaste Térmico Abrasivo**: Golpear a ADB con un socket TCP cada 60 segundos es una cadencia violenta que ciertamente detiene el Doze Mode, pero somete a la radio y a la CPU de Android a una fricción eléctrica constante, drenando batería en un servicio de vigilancia 24/7.

### 🛠️ La Solución
Se ha refactorizado la cadencia térmica y el chivato del `adbWatchdogThread` en el `OidoService`.
- **Intervalo Táctico de 5 Minutos**: El hilo ahora duerme en `Thread.sleep(300000);`. El límite del algoritmo de congelamiento de Doze es lo suficientemente holgado para permitir "latidos" cada 5 minutos, reteniendo a ADB en memoria caché sin fundir la batería por micro-despertares por minuto.
- **Contador de Salud Intravenoso (`pingCount`)**: Se agregó un contabilizador local en la iteración `while` del servicio. Si el ping logra enviarse, suma un latido. Para no inundar (`spam`) la bitácora del panel web `logToWeb`, se reporta por radio:
  - El **1º Ping** que sella el arranque.
  - Luego de manera telepática **cada hora** (`pingCount % 12 == 0`, es decir, 12 pings de 5 minutos = 60 mins).
- **Extracción de Excepciones**: En lugar de devorar los errores (`// Fallo silencioso`), el hilo expulsa ahora un `LogToWeb` inmediato si ocurre un `Connection Refused` en el puerto 5555, visibilizando mortalmente ataques Doze o desconexiones de cables en el panel Web.

### 🎓 Lecciones Aprendidas
- **El Silencio No es Seguro:** Un guardián debe gritar si muere, no desvanecerse en un catch vacío. Dotar a los subsistemas críticos o "Watchdogs" de telemetría racionada (Ej: 1 ping visible cada hora) permite a un Ingeniero confirmar la estabilidad del sistema 6 horas después sin tener que empalmar ADB por cable y revisar logs crudos de memoria asíncrona.
