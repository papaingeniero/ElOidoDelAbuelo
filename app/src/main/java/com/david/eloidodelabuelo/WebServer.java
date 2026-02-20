package com.david.eloidodelabuelo;

import android.content.Context;
import android.content.SharedPreferences;

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

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private final Context context;
    private final AudioSentinel sentinel;

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

                SharedPreferences prefs = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE);
                boolean detectionEnabled = prefs.getBoolean("DETECTION_ENABLED", true);
                json.put("DETECTION_ENABLED", detectionEnabled);
                json.put("SHIELD_ENABLED", prefs.getBoolean("SHIELD_ENABLED", true));
                json.put("SPIKE_THRESHOLD", prefs.getInt("SPIKE_THRESHOLD", 10000));
                json.put("REQUIRED_SPIKES", prefs.getInt("REQUIRED_SPIKES", 3));
                json.put("SHIELD_WINDOW_MS", prefs.getInt("SHIELD_WINDOW_MS", 500));
                json.put("RECORD_DURATION_MS", prefs.getInt("RECORD_DURATION_MS", 15000));

                return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString());
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

                    if (json.has("DETECTION_ENABLED"))
                        editor.putBoolean("DETECTION_ENABLED", json.getBoolean("DETECTION_ENABLED"));
                    if (json.has("SHIELD_ENABLED"))
                        editor.putBoolean("SHIELD_ENABLED", json.getBoolean("SHIELD_ENABLED"));
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
            try {
                File dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                File[] files = dir != null ? dir.listFiles((dir1, name) -> name.endsWith(".wav")) : new File[0];

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
                    jsonArray.put(obj);
                }

                return newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray.toString());
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
                FileInputStream fis = new FileInputStream(audioFile);
                return newChunkedResponse(Response.Status.OK, "audio/wav", fis);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error reproduciendo audio");
            }
        }

        if ("/".equals(uri)) {
            try {
                InputStream is = context.getAssets().open("web/index.html");
                return newChunkedResponse(Response.Status.OK, "text/html", is);
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "No se pudo cargar el Dashboard: " + e.getMessage());
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "404 Not Found");
    }
}
