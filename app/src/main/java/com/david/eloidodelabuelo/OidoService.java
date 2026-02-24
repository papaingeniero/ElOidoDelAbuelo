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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class OidoService extends Service {

    private static final String TAG = "OidoService";
    private static final String CHANNEL_ID = "SentinelChannel";
    private static final int NOTIFICATION_ID = 1;
    // Sustituye <TU_TOKEN> por el tuyo en el siguiente comando de ProcessBuilder
    private static final String CLOUDFLARED_TOKEN = "eyJhIjoiOTUzYjYyNTI4ZjU4NWNiNzc3MDNkYTg0MjgxMWJlNDUiLCJ0IjoiNzQwZTlmOGUtNjNkMy00Y2NkLTk3ZWMtZDI4M2M1ZWQ5MjFmIiwicyI6Ik1USmpOV0ZsTWpNdE9UZzNaaTAwTkRaakxUZ3pObU10WVdRMFl6aGhZamd3TldabCJ9";

    private AudioSentinel audioSentinel;
    private WebServer webServer;
    private Process cloudflaredProcess;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Iniciando servicio");
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

        startCloudflaredTunnel();
    }

    private void startCloudflaredTunnel() {
        new Thread(() -> {
            try {
                File cloudflaredFile = new File(getFilesDir(), "cloudflared");

                // Siempre extraemos/sobreescribimos para asegurar que tenemos la versión
                // correcta del APK
                try (InputStream is = getAssets().open("cloudflared");
                        OutputStream os = new FileOutputStream(cloudflaredFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    Log.d(TAG, "Binario cloudflared extraído a disco");
                }

                // Dar permisos de ejecución
                cloudflaredFile.setExecutable(true);

                // Levantar el proceso en background
                Log.d(TAG, "Iniciando túnel Cloudflare Access...");
                ProcessBuilder pb = new ProcessBuilder(
                        cloudflaredFile.getAbsolutePath(),
                        "tunnel",
                        "--no-autoupdate",
                        "run",
                        "--token",
                        CLOUDFLARED_TOKEN);

                // Redirigir la salida del error standard porsi falla al iniciar
                pb.redirectErrorStream(true);

                cloudflaredProcess = pb.start();
                Log.d(TAG, "Proceso Cloudflared en ejecución (PID oculto)");

            } catch (Exception e) {
                Log.e(TAG, "Fallo crítico al iniciar Cloudflared", e);
            }
        }).start();
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
        if (webServer != null) {
            webServer.stop();
        }
        if (audioSentinel != null) {
            audioSentinel.stop();
        }
        if (cloudflaredProcess != null) {
            cloudflaredProcess.destroy();
            Log.d(TAG, "Subproceso Cloudflared aniquilado");
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
