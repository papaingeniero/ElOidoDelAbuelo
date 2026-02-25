package com.david.eloidodelabuelo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.BatteryManager;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

import android.util.Log;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private final Context context;
    private final AudioSentinel sentinel;

    // Cache de telemetría (Eco-Mode V27)
    private long lastHardwarePollTime = 0;
    private static final long POLL_INTERVAL_MS = 60000; // 1 minuto
    private float cachedBatteryPct = -1;
    private boolean cachedIsCharging = false;
    private int cachedTempCelsiusFull = 0; // Guardado en décimas de grado (int)

    private void refreshHardwareTelemetry() {
        long now = System.currentTimeMillis();
        if (now - lastHardwarePollTime < POLL_INTERVAL_MS && lastHardwarePollTime != 0) {
            return; // Usar cache
        }

        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                cachedIsCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                        plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                        plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
                cachedBatteryPct = scale > 0 ? (level * 100 / (float) scale) : -1;
                cachedTempCelsiusFull = temp;
                lastHardwarePollTime = now;
                Log.d("WebServer", "Telemetría hardware refrescada: " + cachedBatteryPct + "%");
            }
        } catch (Exception e) {
            Log.e("WebServer", "Error refrescando telemetría", e);
        }
    }

    public WebServer(Context context, AudioSentinel sentinel) {
        super(8080);
        this.context = context.getApplicationContext();
        this.sentinel = sentinel;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if ("/api/status".equals(uri)) {
            try {
                JSONObject json = new JSONObject();
                json.put("currentAmplitude", sentinel.getCurrentAmplitude());
                json.put("isRecording", sentinel.isCurrentlyRecording());
                json.put("version", BuildConfig.VERSION_NAME);

                // Telemetría con Cache (Eco-Mode V27)
                refreshHardwareTelemetry();
                json.put("batteryPct", Math.round(cachedBatteryPct));
                json.put("isCharging", cachedIsCharging);
                json.put("tempCelsius", cachedTempCelsiusFull / 10.0f);

                SharedPreferences prefs = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE);
                json.put("micEnabled", prefs.getBoolean("MIC_ENABLED", true));
                json.put("autoDetectionEnabled", prefs.getBoolean("AUTO_DETECTION_ENABLED", true)); // Nuevo V38
                json.put("shieldEnabled", prefs.getBoolean("SHIELD_ENABLED", true));
                json.put("forceRecord", prefs.getBoolean("FORCE_RECORD", false));
                json.put("recordingStartTimestamp", sentinel.getRecordingStartTimestamp());

                json.put("SPIKE_THRESHOLD", prefs.getInt("SPIKE_THRESHOLD", 10000));
                json.put("REQUIRED_SPIKES", prefs.getInt("REQUIRED_SPIKES", 3));
                json.put("SHIELD_WINDOW_MS", prefs.getInt("SHIELD_WINDOW_MS", 500));
                json.put("RECORD_DURATION_MS", prefs.getInt("RECORD_DURATION_MS", 15000));

                Response r = newFixedLengthResponse(Response.Status.OK, "application/json", json.toString());
                r.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                r.addHeader("Pragma", "no-cache");
                return r;
            } catch (JSONException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error generando JSON");
            }
        }

        if ("/api/settings".equals(uri) && Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String postData = files.get("postData");

                if (postData != null) {
                    JSONObject json = new JSONObject(postData);
                    SharedPreferences.Editor editor = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE)
                            .edit();

                    if (json.has("micEnabled"))
                        editor.putBoolean("MIC_ENABLED", json.getBoolean("micEnabled"));
                    if (json.has("autoDetectionEnabled")) // Nuevo V38
                        editor.putBoolean("AUTO_DETECTION_ENABLED", json.getBoolean("autoDetectionEnabled"));
                    if (json.has("shieldEnabled"))
                        editor.putBoolean("SHIELD_ENABLED", json.getBoolean("shieldEnabled"));
                    if (json.has("forceRecord")) {
                        boolean bForce = json.getBoolean("forceRecord");
                        editor.putBoolean("FORCE_RECORD", bForce);
                        sentinel.updateForceRecordTimestamp(bForce);
                    }
                    if (json.has("abortRecording") && json.getBoolean("abortRecording")) {
                        sentinel.abortCurrentRecording();
                    }
                    if (json.has("SPIKE_THRESHOLD"))
                        editor.putInt("SPIKE_THRESHOLD", json.getInt("SPIKE_THRESHOLD"));
                    if (json.has("REQUIRED_SPIKES"))
                        editor.putInt("REQUIRED_SPIKES", json.getInt("REQUIRED_SPIKES"));
                    if (json.has("SHIELD_WINDOW_MS"))
                        editor.putInt("SHIELD_WINDOW_MS", json.getInt("SHIELD_WINDOW_MS"));
                    if (json.has("RECORD_DURATION_MS"))
                        editor.putInt("RECORD_DURATION_MS", json.getInt("RECORD_DURATION_MS"));

                    editor.apply();

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("status", "ok");
                    return newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString());
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                            "No postData found");
                }
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error parsing POST: " + e.getMessage());
            }
        }

        if ("/api/recordings".equals(uri)) {
            if (Method.DELETE.equals(session.getMethod())) {
                try {
                    File dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                    File[] files = dir != null ? dir.listFiles(
                            (dir1, name) -> name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".aac"))
                            : new File[0];
                    int deletedCount = 0;
                    if (files != null) {
                        for (File file : files) {
                            if (file.delete()) {
                                deletedCount++;
                            }
                        }
                    }
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("status", "ok");
                    responseJson.put("deleted_count", deletedCount);
                    return newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString());
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                            "Error durante la purga: " + e.getMessage());
                }
            }

            try {
                File dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                File[] files = dir != null
                        ? dir.listFiles(
                                (dir1, name) -> name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".aac"))
                        : new File[0];

                if (files == null)
                    files = new File[0];

                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    }
                });

                JSONArray jsonArray = new JSONArray();
                for (File file : files) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", file.getName());
                    obj.put("size", file.length());
                    obj.put("timestamp", file.lastModified());

                    // Extraer duración del audio (MediaMetadataRetriever)
                    long durationMs = 0;
                    JSONArray peaksArray = null;

                    try {
                        String jsonName = file.getName().replaceAll("\\.[^.]+$", ".json");
                        File jsonFile = new File(file.getParentFile(), jsonName);
                        if (jsonFile.exists()) {
                            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(jsonFile));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null)
                                sb.append(line);
                            br.close();

                            String jsonString = sb.toString();
                            if (jsonString.startsWith("{")) {
                                // Formato V54: Objeto con duración y picos
                                JSONObject jsonObj = new JSONObject(jsonString);
                                durationMs = jsonObj.optLong("durationMs", 0);
                                peaksArray = jsonObj.optJSONArray("peaks");
                            } else if (jsonString.startsWith("[")) {
                                // Formato antiguo: Solo array de picos
                                peaksArray = new JSONArray(jsonString);
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    // Se ha eliminado el fallback de MediaMetadataRetriever (V56)
                    // Confiamos exclusivamente en el JSON generado por AudioSentinel.

                    obj.put("durationMs", durationMs);
                    if (peaksArray != null)
                        obj.put("peaks", peaksArray);

                    jsonArray.put(obj);
                }
                Response r = newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray.toString());
                r.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                r.addHeader("Pragma", "no-cache");
                return r;
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error al leer historial");
            }
        }

        if ("/api/audio".equals(uri)) {
            Map<String, String> parms = session.getParms();
            String fileName = parms.get("file");

            if (fileName == null || fileName.contains("/") || fileName.contains("..")) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                        "Acceso denegado o archivo inválido");
            }

            File dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File audioFile = new File(dir, fileName);

            if (!audioFile.exists()) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                        "Archivo no encontrado");
            }

            try {
                String range = session.getHeaders().get("range");
                long fileLen = audioFile.length();
                String mime = "audio/wav";
                // Nuestros .m4a son ADTS-AAC crudo (v28), NO contenedores MP4.
                // Safari iOS rechaza audio/mp4 si el contenido es ADTS puro.
                if (fileName.endsWith(".m4a") || fileName.endsWith(".aac"))
                    mime = "audio/aac";

                if (range != null && range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    long start = 0;
                    long end = fileLen - 1;
                    if (minus > 0) {
                        try {
                            start = Long.parseLong(range.substring(0, minus));
                            if (minus < range.length() - 1) {
                                end = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (start >= fileLen) {
                        Response res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE,
                                NanoHTTPD.MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes */" + fileLen);
                        return res;
                    }

                    long dataLen = end - start + 1;
                    if (dataLen < 0)
                        dataLen = 0;

                    FileInputStream fis = new FileInputStream(audioFile);
                    fis.skip(start);

                    Response res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, dataLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", String.valueOf(dataLen));
                    res.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLen);
                    return res;
                } else {
                    FileInputStream fis = new FileInputStream(audioFile);
                    Response res = newFixedLengthResponse(Response.Status.OK, mime, fis, fileLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", String.valueOf(fileLen));
                    return res;
                }
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error reproduciendo audio: " + e.getMessage());
            }
        }

        if ("/api/stream".equals(uri)) {
            // Streaming en Vivo ADTS-AAC Directo (V28)
            java.io.PipedInputStream pipedInputStream = new java.io.PipedInputStream(16384);
            java.io.PipedOutputStream pipedOutputStream = new java.io.PipedOutputStream();
            try {
                pipedInputStream.connect(pipedOutputStream);
                sentinel.addLiveListener(pipedOutputStream);
                // No necesitamos escribir cabeceras simuladas WAV, el AAC-ADTS es
                // auto-descriptivo
                Response r = newChunkedResponse(Response.Status.OK, "audio/aac", pipedInputStream);
                r.addHeader("Connection", "keep-alive");
                r.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                r.addHeader("Access-Control-Allow-Origin", "*");

                // Muerte absoluta al GZIP mediante Reflexión (Bypass NanoHTTPD 2.3.1 interno)
                try {
                    java.lang.reflect.Field encodeAsGzipField = r.getClass().getDeclaredField("encodeAsGzip");
                    encodeAsGzipField.setAccessible(true);
                    encodeAsGzipField.setBoolean(r, false);
                } catch (Exception e) {
                    Log.e("WebServer", "Imposible anular GZIP por reflexión", e);
                }

                return r;
            } catch (java.io.IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error interno");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error de streaming AAC");
            }
        }

        if ("/".equals(uri)) {
            try {
                InputStream is = context.getAssets().open("web/index.html");
                Response r = newChunkedResponse(Response.Status.OK, "text/html", is);
                r.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                r.addHeader("Pragma", "no-cache");
                return r;
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "No se pudo cargar el Dashboard: " + e.getMessage());
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "404 Not Found");
    }
}
