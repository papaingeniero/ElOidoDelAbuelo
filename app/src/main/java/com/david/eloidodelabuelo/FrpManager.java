package com.david.eloidodelabuelo;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FrpManager {

    private static final String TAG = "FrpManager";
    private static final String BINARY_NAME = "frpc";
    private static final String CONFIG_NAME = "frpc.toml";

    private final Context context;
    private Process frpProcess;
    private Thread stdOutThread;
    private Thread stdErrThread;

    public FrpManager(Context context) {
        this.context = context;
    }

    public void start() {
        Log.d(TAG, "Iniciando FrpManager...");
        try {
            File frpBinary = new File(context.getFilesDir(), BINARY_NAME);
            File frpConfig = new File(context.getFilesDir(), CONFIG_NAME);

            // 1. Extracción de assets si no existen o están vacíos
            if (!frpBinary.exists() || frpBinary.length() == 0) {
                Log.d(TAG, "Binario " + BINARY_NAME + " no encontrado o vacío. Extrayendo...");
                extractAsset(BINARY_NAME, frpBinary);
            }
            if (!frpConfig.exists() || frpConfig.length() == 0) {
                Log.d(TAG, "Configuración " + CONFIG_NAME + " no encontrada o vacía. Extrayendo...");
                extractAsset(CONFIG_NAME, frpConfig);
            }

            // 2. Permisos de Ejecución
            Log.d(TAG, "Aplicando permisos de ejecución a " + BINARY_NAME);
            setExecutablePermissions(frpBinary);

            // 3. Ejecución y Stream Gobblers
            startTunnel(frpBinary, frpConfig);

        } catch (Exception e) {
            Log.e(TAG, "🔥 Error crítico iniciando FrpManager", e);
        }
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

    private void setExecutablePermissions(File file) throws IOException, InterruptedException {
        // En Android, los archivos en getFilesDir() no son ejecutables por defecto.
        // Forzamos el chmod 777 usando la shell del sistema operativo.
        Process process = Runtime.getRuntime().exec("chmod 777 " + file.getAbsolutePath());
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            Log.d(TAG, "Permisos 777 aplicados correctamente a " + file.getName());
        } else {
            Log.w(TAG, "⚠️ Error aplicando chmod 777. Código de salida: " + exitCode);
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
