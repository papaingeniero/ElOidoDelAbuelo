package com.david.eloidodelabuelo;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrpManager {

    private static final String TAG = "FrpManager";
    private static final String BINARY_NAME = "frpc";
    private static final String CONFIG_NAME = "frpc.toml";

    private final Context context;
    private Process frpProcess;
    private Thread stdOutThread;
    private Thread stdErrThread;
    private ExecutorService executorService;

    public FrpManager(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void start() {
        Log.d(TAG, "Iniciando FrpManager background task...");
        executorService.submit(() -> {
            try {
                // El binario está empaquetado en jniLibs como libfrpc.so bajo W^X de API 29
                File frpBinary = new File(context.getApplicationInfo().nativeLibraryDir, "libfrpc.so");
                File frpConfig = new File(context.getFilesDir(), CONFIG_NAME);

                // 1. Extracción de configuración
                if (!frpConfig.exists() || frpConfig.length() == 0) {
                    Log.d(TAG, "Configuración " + CONFIG_NAME + " no encontrada o vacía. Extrayendo...");
                    extractAsset(CONFIG_NAME, frpConfig);
                }

                if (!frpBinary.exists()) {
                    Log.e(TAG, "🔥 Binario FRP no extraído por Android en: " + frpBinary.getAbsolutePath());
                    return;
                } else {
                    Log.d(TAG, "Binario FRP listado en: " + frpBinary.getAbsolutePath());
                }

                // 2. Aseguramos permisos
                setExecutablePermissions(frpBinary);

                // 3. Ejecución
                startTunnel(frpBinary, frpConfig);

            } catch (Exception e) {
                Log.e(TAG, "🔥 Error crítico iniciando FrpManager", e);
            }
        });
    }

    public void stop() {
        Log.d(TAG, "Deteniendo FrpManager...");
        if (frpProcess != null) {
            frpProcess.destroy();
            frpProcess = null;
            Log.d(TAG, "Proceso FRP destruido.");
        }
        
        // Interrumpimos los hilos de log (Stream Gobblers)
        if (stdOutThread != null && stdOutThread.isAlive()) {
            stdOutThread.interrupt();
        }
        if (stdErrThread != null && stdErrThread.isAlive()) {
            stdErrThread.interrupt();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void extractAsset(String assetName, File targetFile) throws IOException {
        try (InputStream in = context.getAssets().open(assetName);
             FileOutputStream out = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            Log.d(TAG, "Asset " + assetName + " extraído en " + targetFile.getAbsolutePath());
        }
    }

    private void setExecutablePermissions(File file) {
        // Usamos la API de Archivos nativa de Java para sortear fallos de shell W^X
        boolean r = file.setReadable(true, false);
        boolean w = file.setWritable(true, false);
        boolean e = file.setExecutable(true, false);
        if (e) {
            Log.d(TAG, "Permisos de ejecución (File API) aplicados correctamente a " + file.getName());
        } else {
            Log.w(TAG, "⚠️ Error aplicando setExecutable a " + file.getName());
        }
    }

    private void startTunnel(File frpBinary, File frpConfig) throws IOException {
        Log.d(TAG, "Levantando túnel FRP en segundo plano...");
        
        // Usamos ProcessBuilder para mayor control sobre el working directory y el comando
        ProcessBuilder builder = new ProcessBuilder(
                frpBinary.getAbsolutePath(),
                "-c",
                frpConfig.getAbsolutePath()
        );
        // Establecemos el directorio de ejecución donde residen los binarios extraídos
        builder.directory(context.getFilesDir());

        frpProcess = builder.start();

        // Creamos los Stream Gobblers (Vital para evitar que el búfer bloquee el proceso)
        stdOutThread = new Thread(() -> streamGobbler(frpProcess.getInputStream(), "STDOUT"));
        stdErrThread = new Thread(() -> streamGobbler(frpProcess.getErrorStream(), "STDERR"));

        stdOutThread.start();
        stdErrThread.start();
        
        Log.d(TAG, "Túnel FRP lanzado. Stream Gobblers escuchando.");
    }

    private void streamGobbler(InputStream inputStream, String streamType) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Filtramos a Log.d o Log.e dependiendo del tipo de stream
                if ("STDERR".equals(streamType)) {
                    Log.e(TAG, "[FRP-ERR] " + line);
                } else {
                    Log.d(TAG, "[FRP-OUT] " + line);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error leyendo " + streamType + " del proceso FRP", e);
        }
    }
}
