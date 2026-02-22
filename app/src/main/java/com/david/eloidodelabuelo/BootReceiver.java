package com.david.eloidodelabuelo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;

/**
 * Escucha el evento de arranque completado del Dispositivo (Autostart).
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "¡Sistema arrancado! Iniciando El Oído del Abuelo en Segundo Plano...");

            Intent serviceIntent = new Intent(context, OidoService.class);

            // Requisito de Android 8+: Usar Foreground Service desde Background Receivers
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
