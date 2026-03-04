package com.david.eloidodelabuelo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

public class ScheduleReceiver extends BroadcastReceiver {
    private static final String TAG = "ScheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long durationMs = intent.getLongExtra("durationMs", 0);
        Log.i(TAG, "⏰ ¡Alarma Programada Despertada! Ejecutando OidoService por " + durationMs + "ms");

        // Al sonar la alarma, limpiamos las preferencias para que no vuelva a sonar o
        // aparecer en el dashboard como "futura"
        SharedPreferences.Editor editor = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE).edit();
        editor.remove("SCHEDULE_AT_MS");
        editor.remove("SCHEDULE_DUR_MS");
        editor.apply();

        // Lanzar AudioSentinel en OidoService (usamos WakefulBroadcastReceiver o
        // ForegroundService)
        Intent serviceIntent = new Intent(context, OidoService.class);
        serviceIntent.setAction("EXACT_SCHEDULE_FIRED");
        serviceIntent.putExtra("durationMs", durationMs);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
