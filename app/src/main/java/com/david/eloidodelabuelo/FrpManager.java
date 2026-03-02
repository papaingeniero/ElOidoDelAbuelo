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
    private volatile boolean isRunning = false;

    // Tiempos de retardo exponencial (en milisegundos)
    private static final long[] BACKOFF_DELAYS = {
        10 * 1000,      // 10 segundos
        30 * 1000,      // 30 segundos
        2 * 60 * 1000,  // 2 minutos
        5 * 60 * 1000   // 5 minutos (Tope)
    };

    public FrpManager(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        Log.d(TAG, "Iniciando FrpManager Watchdog (Backoff Exponencial)...");
        
        executorService.submit(() -> {
            int retryCount = 0;
            long lastTunnelStartTime = 0;

            while (isRunning) {
                try {
                    File frpBinary = new File(context.getApplicationInfo().nativeLibraryDir, "libfrpc.so");
                    File frpConfig = new File(context.getFilesDir(), CONFIG_NAME);

                    if (!frpConfig.exists() || frpConfig.length() == 0) {
                        Log.d(TAG, "Extrayendo configuración " + CONFIG_NAME + "...");
                        extractAsset(CONFIG_NAME, frpConfig);
                    }

                    if (!frpBinary.exists()) {
                        Log.e(TAG, "🔥 Binario FRP no extraído por Android.");
                        isRunning = false;
                        return;
                    }

                    setExecutablePermissions(frpBinary);

                    // Si el túnel aguantó vivo más de 5 minutos en el intento anterior, reseteamos el castigo
                    if (System.currentTimeMillis() - lastTunnelStartTime > 5 * 60 * 1000) {
                        retryCount = 0;
                    }

                    lastTunnelStartTime = System.currentTimeMillis();
                    startTunnel(frpBinary, frpConfig);

                    // Bloqueamos el hilo de Watchdog esperando a que el proceso FRP muera nativamente
                    int exitCode = frpProcess.waitFor();
                    Log.w(TAG, "⚠️ Proceso FRP terminó con código: " + exitCode);

                } catch (InterruptedException e) {
                    Log.d(TAG, "Watchdog interrumpido voluntariamente.");
                    Thread.currentThread().interrupt();
                    break; // Cierre de túnel ordenado por stop()
                } catch (Exception e) {
                    Log.e(TAG, "🔥 Error cíclico iniciando FrpManager", e);
                }

                // Si seguimos vivos (no nos han parado), aplicamos el castigo (Backoff)
                if (isRunning) {
                    long delay = BACKOFF_DELAYS[Math.min(retryCount, BACKOFF_DELAYS.length - 1)];
                    Log.d(TAG, "Zzz... FRP durmiendo por " + (delay/1000) + " segundos antes de reintentar (Intento " + (retryCount+1) + ")");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    retryCount++;
                }
            }
        });
    }

    public void stop() {
        Log.d(TAG, "Deteniendo FrpManager Watchdog...");
        isRunning = false;
        
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

                // Wathdog Activo: Si detectamos que no hay servidor, matamos el proceso para forzar el Backoff de batería
                if (line.contains("connect to server error") || line.contains("login to server failed")) {
                    Log.e(TAG, "🔥 Servidor FRP inalcanzable. Destruyendo proceso nativo para forzar suspensión (Backoff)...");
                    if (frpProcess != null) {
                        frpProcess.destroy();
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error leyendo " + streamType + " del proceso FRP", e);
        }
    }
}
