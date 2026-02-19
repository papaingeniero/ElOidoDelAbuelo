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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class OidoService extends Service {

    private static final String TAG = "OidoService";
    private static final String CHANNEL_ID = "SentinelChannel";
    private static final int NOTIFICATION_ID = 1;

    private AudioSentinel audioSentinel;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Iniciando servicio");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        audioSentinel = new AudioSentinel(this);
        audioSentinel.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Si el sistema mata el servicio, intentar recrearlo
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Deteniendo servicio");
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
}
