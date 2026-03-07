package com.david.eloidodelabuelo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class RevivalReceiver extends BroadcastReceiver {

    private static final String TAG = "RevivalReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        WebServer.logToWeb(TAG, "⚡ ¡Desfibrilador Activado! Comprobando constantes vitales del OidoService...");

        if (!OidoService.isServiceRunning) {
            WebServer.logToWeb(TAG, "💀 OidoService detectado como CAÍDO. Iniciando protocolo de resurrección de emergencia (Operación Lázaro)...");
            
            Intent serviceIntent = new Intent(context, OidoService.class);
            try {
                ContextCompat.startForegroundService(context, serviceIntent);
                WebServer.logToWeb(TAG, "🚀 startForegroundService invocado explícitamente por el RevivalReceiver.");
            } catch (Exception e) {
                WebServer.logToWeb(TAG, "❌ Error crítico intentando resucitar el servicio", e);
            }
        } else {
            WebServer.logToWeb(TAG, "✅ OidoService está vivo y respirando. Falsa alarma.");
        }

        // 🔥 LA MAGIA: Volver a cargar el desfibrilador para dentro de 15 minutos
        OidoService.scheduleRevivalAlarm(context);
    }
}
