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

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
        Log.d(TAG, "onCreate: Iniciando servicio");

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
            Log.d(TAG, "WebServer iniciado en el puerto 8080");
        } catch (IOException e) {
            Log.e(TAG, "Error iniciando WebServer", e);
        }

        // Inicializar FRP
        frpManager = new FrpManager(this);
        frpManager.start();

        scheduleRevivalAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Si el sistema mata el servicio, intentar recrearlo
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        cancelRevivalAlarm();
        Log.d(TAG, "onDestroy: Deteniendo servicio");

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
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("El Oído del Abuelo")
                .setContentText("Escuchando activamente...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Icono de sistema garantizado
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void scheduleRevivalAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, RevivalReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
            );

            // MIUI es agresivo, usamos setExactAndAllowWhileIdle si es posible, repitiendo cada 15 min aprox.
            long triggerAtMillis = SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.d(TAG, "⏰ Desfibrilador programado para dentro de 15 minutos exactos.");
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
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE : PendingIntent.FLAG_NO_CREATE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "🛑 Desfibrilador (AlarmManager) cancelado por muerte voluntaria.");
            }
        }
    }
}

