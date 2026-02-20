package com.david.eloidodelabuelo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;

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

                return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString());
            } catch (JSONException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Error generando JSON");
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
