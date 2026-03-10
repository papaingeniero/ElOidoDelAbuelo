package com.david.eloidodelabuelo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;

public class OidoService extends Service {

    public static volatile boolean isServiceRunning = false;

    private static final String TAG = "OidoService";
    private static final String CHANNEL_ID = "SentinelChannel";
    private static final int NOTIFICATION_ID = 1;

    private AudioSentinel audioSentinel;
    private WebServer webServer;
    private FrpManager frpManager;
    private android.os.PowerManager.WakeLock wakeLock;
    private android.net.wifi.WifiManager.WifiLock wifiLock;
    private Thread adbWatchdogThread;

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
        WebServer.logToWeb(TAG, "onCreate: Iniciando servicio");

        // 🛡️ BLINDAJE ANTI-DEEP SLEEP (CPU + 4G)
        android.os.PowerManager powerManager = (android.os.PowerManager) getSystemService(
                android.content.Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "ElOido::CpuWakeLock");
            wakeLock.acquire();
        }

        // 🛡️ BLINDAJE ANTI-NARCOLEPSIA (Wi-Fi)
        android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) getApplicationContext()
                .getSystemService(android.content.Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiLock = wifiManager.createWifiLock(android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    "ElOido::WifiLock");
            wifiLock.acquire();
        }
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        audioSentinel = new AudioSentinel(this);
        audioSentinel.start();

        webServer = new WebServer(this, audioSentinel);
        try {
            webServer.start();
            WebServer.logToWeb(TAG, "WebServer iniciado en el puerto 8080");
        } catch (IOException e) {
            WebServer.logToWeb(TAG, "Error iniciando WebServer", e);
        }

        // Inicializar FRP
        frpManager = new FrpManager(this);
        frpManager.start();

        scheduleRevivalAlarm(this);
        startAdbWatchdog();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("RESTART_FRP".equals(action)) {
                Log.w(TAG, "♻️ Recibida orden HTTP de RESTART_FRP. Reiniciando túnel...");
                if (frpManager != null) {
                    frpManager.stop();
                    frpManager.start();
                }
            } else if ("PROGRAM_SCHEDULE_REC".equals(action)) {
                long trigger = intent.getLongExtra("triggerAtMillis", 0);
                long dur = intent.getLongExtra("durationMs", 0);
                programarGrabacionExacta(trigger, dur);
            } else if ("CANCEL_SCHEDULE_REC".equals(action)) {
                cancelarGrabacionExacta();
            } else if ("EXACT_SCHEDULE_FIRED".equals(action)) {
                long durMs = intent.getLongExtra("durationMs", 0);
                Log.w(TAG, "🔥 ALARMA DETONADA. Forzando grabación por " + durMs + "ms");
                if (audioSentinel != null) {
                    audioSentinel.startScheduledRecording(durMs);
                }
            }
        }
        // Si el sistema mata el servicio, intentar recrearlo
        return START_STICKY;
    }

    private void programarGrabacionExacta(long triggerAtMillis, long durationMs) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ScheduleReceiver.class);
        intent.putExtra("durationMs", durationMs);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 999, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            // Usar reloj RTC_WAKEUP exacto para saltar la hora con Doze mode override
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            WebServer.logToWeb(TAG, "📅 Grabacion Programada Activada en AlarmManager para el Timestamp: " + triggerAtMillis);
        }
    }

    private void cancelarGrabacionExacta() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ScheduleReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 999, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            WebServer.logToWeb(TAG, "🔕 Grabacion Programada Cancelada en AlarmManager");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        cancelRevivalAlarm();
        WebServer.logToWeb(TAG, "onDestroy: Deteniendo servicio");

        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
        if (wifiLock != null && wifiLock.isHeld())
            wifiLock.release();
        if (webServer != null) {
            webServer.stop();
        }
        if (frpManager != null) {
            frpManager.stop();
        }
        if (audioSentinel != null) {
            audioSentinel.stop();
        }
        if (adbWatchdogThread != null) {
            adbWatchdogThread.interrupt();
            adbWatchdogThread = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal Centinela El Oído del Abuelo",
                    NotificationManager.IMPORTANCE_LOW);
            // IMPORTANCE_LOW: soundless, min visual intrusion

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("El Oído del Abuelo")
                .setContentText("Escuchando activamente...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Icono de sistema garantizado
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public static void scheduleRevivalAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, RevivalReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                            : PendingIntent.FLAG_UPDATE_CURRENT);

            // MIUI es agresivo, usamos setExactAndAllowWhileIdle si es posible, repitiendo
            // cada 15 min aprox.
            long triggerAtMillis = SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis,
                        pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            }
            WebServer.logToWeb(TAG, "⏰ Desfibrilador programado para dentro de 15 minutos exactos.");
        }
    }

    private void cancelRevivalAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, RevivalReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
                            : PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                WebServer.logToWeb(TAG, "🛑 Desfibrilador (AlarmManager) cancelado por muerte voluntaria.");
            }
        }
    }

    private void startAdbWatchdog() {
        if (adbWatchdogThread != null) return;
        adbWatchdogThread = new Thread(() -> {
            WebServer.logToWeb("ADB-Watchdog", "🩺 Iniciando Auto-Desfibrilador local PROFUNDO (Puerto 5555)...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 1. Conexión TCP
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress("127.0.0.1", 5555), 2000);
                    
                    // 2. 🔥 EL CUBO DE AGUA FRÍA: Enviar payload para obligar al demonio ADB a procesar y rechazar la trama
                    java.io.OutputStream os = socket.getOutputStream();
                    os.write("HELO".getBytes("UTF-8"));
                    os.flush();
                    
                    // 3. Dejar la conexión abierta medio segundo para que ADB lo mastique
                    Thread.sleep(500);
                    socket.close();
                } catch (Exception e) {
                    // Fallo silencioso si ADB está realmente muerto
                }
                try {
                    Thread.sleep(60000); // Latido cada 60 segundos
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        adbWatchdogThread.setPriority(Thread.MIN_PRIORITY);
        adbWatchdogThread.start();
    }
}
