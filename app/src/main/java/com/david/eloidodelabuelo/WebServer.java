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
                String range = session.getHeaders().get("range");
                long fileLen = audioFile.length();
                String mime = fileName.endsWith(".m4a") ? "audio/mp4" : "audio/wav";

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
            try {
                java.io.PipedInputStream pipedInputStream = new java.io.PipedInputStream(8192);
                java.io.PipedOutputStream pipedOutputStream = new java.io.PipedOutputStream(pipedInputStream);

                // Escribir cabecera WAV de longitud infinita (0xFFFFFFFF)
                byte[] header = new byte[44];
                long totalDataLen = 0xFFFFFFFFL; // inf
                long totalAudioLen = 0xFFFFFFFFL; // inf
                long longSampleRate = 16000;
                long byteRate = 16000 * 2;
                byte channels = 1;

                header[0] = 'R';
                header[1] = 'I';
                header[2] = 'F';
                header[3] = 'F';
                header[4] = (byte) (totalDataLen & 0xff);
                header[5] = (byte) ((totalDataLen >> 8) & 0xff);
                header[6] = (byte) ((totalDataLen >> 16) & 0xff);
                header[7] = (byte) ((totalDataLen >> 24) & 0xff);
                header[8] = 'W';
                header[9] = 'A';
                header[10] = 'V';
                header[11] = 'E';
                header[12] = 'f';
                header[13] = 'm';
                header[14] = 't';
                header[15] = ' ';
                header[16] = 16;
                header[17] = 0;
                header[18] = 0;
                header[19] = 0;
                header[20] = 1;
                header[21] = 0;
                header[22] = channels;
                header[23] = 0;
                header[24] = (byte) (longSampleRate & 0xff);
                header[25] = (byte) ((longSampleRate >> 8) & 0xff);
                header[26] = (byte) ((longSampleRate >> 16) & 0xff);
                header[27] = (byte) ((longSampleRate >> 24) & 0xff);
                header[28] = (byte) (byteRate & 0xff);
                header[29] = (byte) ((byteRate >> 8) & 0xff);
                header[30] = (byte) ((byteRate >> 16) & 0xff);
                header[31] = (byte) ((byteRate >> 24) & 0xff);
                header[32] = (byte) (2 * 16 / 8);
                header[33] = 0;
                header[34] = 16;
                header[35] = 0;
                header[36] = 'd';
                header[37] = 'a';
                header[38] = 't';
                header[39] = 'a';
                header[40] = (byte) (totalAudioLen & 0xff);
                header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
                header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
                header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

                pipedOutputStream.write(header, 0, 44);

                sentinel.addLiveListener(pipedOutputStream);

                return newChunkedResponse(Response.Status.OK, "audio/wav", pipedInputStream);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error de streaming");
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
