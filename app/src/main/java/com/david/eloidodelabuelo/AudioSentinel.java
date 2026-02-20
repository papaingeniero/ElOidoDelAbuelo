package com.david.eloidodelabuelo;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioSentinel {

    private static final String TAG = "AudioSentinel";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Nombres de las preferencias
    private static final String PREF_DETECTION_ENABLED = "DETECTION_ENABLED";
    private static final String PREF_SHIELD_ENABLED = "SHIELD_ENABLED";
    private static final String PREF_SPIKE_THRESHOLD = "SPIKE_THRESHOLD";
    private static final String PREF_REQUIRED_SPIKES = "REQUIRED_SPIKES";
    private static final String PREF_SHIELD_WINDOW_MS = "SHIELD_WINDOW_MS";
    private static final String PREF_RECORD_DURATION_MS = "RECORD_DURATION_MS";

    private boolean isRunning = false;
    private Thread sentinelThread;
    private AudioRecord audioRecord;
    private Context context;

    private volatile double currentAmplitude = 0;
    private volatile boolean isRecordingStatus = false;

    public double getCurrentAmplitude() {
        return currentAmplitude;
    }

    public boolean isCurrentlyRecording() {
        return isRecordingStatus;
    }

    public AudioSentinel(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        if (isRunning)
            return;
        isRunning = true;

        sentinelThread = new Thread(this::runSentinel);
        sentinelThread.start();
        Log.d(TAG, "Centinela iniciado.");
    }

    public void stop() {
        isRunning = false;
        if (sentinelThread != null) {
            try {
                sentinelThread.join(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error deteniendo hilo centinela", e);
            }
        }
    }

    private void runSentinel() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Buffer size error");
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE);

        // Variables de estado del Escudo Anti-Falsos Positivos
        int spikeCount = 0;
        long firstSpikeTime = 0;

        // Variables de estado de Grabación
        boolean isRecording = false;
        long recordingEndTime = 0;
        FileOutputStream fos = null;
        File currentWavFile = null;
        long totalAudioLen = 0;

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord no inicializado");
                return;
            }

            audioRecord.startRecording();
            short[] buffer = new short[bufferSize / 2];
            byte[] byteBuffer = new byte[bufferSize]; // Para escritura WAV

            while (isRunning) {
                // 1. Lectura Dinámica de Preferencias
                boolean detectionEnabled = prefs.getBoolean(PREF_DETECTION_ENABLED, true);
                boolean shieldEnabled = prefs.getBoolean(PREF_SHIELD_ENABLED, true);
                int spikeThreshold = prefs.getInt(PREF_SPIKE_THRESHOLD, 10000);
                int requiredSpikes = prefs.getInt(PREF_REQUIRED_SPIKES, 3);
                int shieldWindowMs = prefs.getInt(PREF_SHIELD_WINDOW_MS, 500);
                int recordDurationMs = prefs.getInt(PREF_RECORD_DURATION_MS, 15000);

                // 2. Modo Standby (Kill Switch)
                if (!detectionEnabled) {
                    if (isRecording) {
                        // Forzar cierre si se desactiva en medio de una grabación
                        closeWavFile(fos, currentWavFile, totalAudioLen);
                        isRecording = false;
                        isRecordingStatus = false;
                        fos = null;
                        currentWavFile = null;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // Leer audio
                int readResult = audioRecord.read(buffer, 0, buffer.length);
                if (readResult > 0) {
                    long currentTime = System.currentTimeMillis();
                    double amplitude = calculateAmplitude(buffer, readResult);
                    this.currentAmplitude = amplitude;

                    // 3. Lógica del Escudo Analizador de Picos
                    if (amplitude > spikeThreshold) {
                        if (!shieldEnabled) {
                            // Sin escudo: disparo inmediato
                            triggerRecording(currentTime, recordDurationMs, currentWavFile, isRecording);
                        } else {
                            // Con escudo: evaluar ventana de tiempo y picos
                            if (spikeCount == 0 || (currentTime - firstSpikeTime) > shieldWindowMs) {
                                // Iniciar nueva racha
                                firstSpikeTime = currentTime;
                                spikeCount = 1;
                            } else {
                                spikeCount++;
                            }

                            if (spikeCount >= requiredSpikes) {
                                // Evento confirmado
                                Log.d(TAG, "Escudo penetrado: Evento confirmado (" + spikeCount + " picos).");
                                triggerRecording(currentTime, recordDurationMs, currentWavFile, isRecording);
                                spikeCount = 0; // Reset
                            }
                        }
                    }

                    // Función auxiliar para re-trigger implícita aquí para limpieza
                    if (amplitude > spikeThreshold && (!shieldEnabled || spikeCount == 0)) { // spikeCount==0 implica
                                                                                             // que acaba de disparar
                        if (!isRecording) {
                            isRecording = true;
                            isRecordingStatus = true;
                            totalAudioLen = 0;
                            recordingEndTime = currentTime + recordDurationMs;

                            // Iniciar nuevo archivo WAV
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                    .format(new Date());
                            currentWavFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                                    "alerta_" + timestamp + ".wav");

                            try {
                                fos = new FileOutputStream(currentWavFile);
                                writeWavHeader(fos, 0, 0, SAMPLE_RATE, (byte) 1, 16); // Header dummy
                            } catch (IOException e) {
                                Log.e(TAG, "Error creando archivo WAV", e);
                                isRecording = false;
                            }
                            Log.d(TAG, "Iniciando grabación: " + currentWavFile.getName());
                        } else {
                            // 4. El Perro Guardián (Retrigger)
                            recordingEndTime = currentTime + recordDurationMs;
                            Log.d(TAG, "Retrigger: Extendiendo grabación hasta " + recordingEndTime);
                        }
                    }

                    // 5. Volcado a WAV
                    if (isRecording) {
                        // Convertir short[] a byte[] asumiendo Little Endian
                        for (int i = 0; i < readResult; i++) {
                            byteBuffer[i * 2] = (byte) (buffer[i] & 0x00FF);
                            byteBuffer[i * 2 + 1] = (byte) ((buffer[i] & 0xFF00) >> 8);
                        }

                        try {
                            if (fos != null) {
                                fos.write(byteBuffer, 0, readResult * 2);
                                totalAudioLen += readResult * 2;
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error escribiendo en WAV", e);
                            isRecording = false; // Abortar
                            isRecordingStatus = false;
                        }

                        // Verificar si el tiempo de grabación expiró
                        if (currentTime >= recordingEndTime) {
                            Log.d(TAG, "Fin de grabación por tiempo.");
                            closeWavFile(fos, currentWavFile, totalAudioLen);
                            isRecording = false;
                            isRecordingStatus = false;
                            fos = null;
                            currentWavFile = null;
                        }
                    }

                } else {
                    Log.w(TAG, "Error leyendo audio: " + readResult);
                }
            }

            // Cierre seguro al salir del bucle si estaba grabando
            if (isRecording) {
                closeWavFile(fos, currentWavFile, totalAudioLen);
                isRecordingStatus = false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Excepción en bucle centinela", e);
        } finally {
            if (audioRecord != null) {
                try {
                    audioRecord.stop();
                    audioRecord.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error liberando AudioRecord", e);
                }
            }
            Log.d(TAG, "Centinela detenido y recursos liberados.");
        }
    }

    // Método auxiliar para no duplicar lógica al disparar (usado solo para flags
    // lógicos en el bucle
    private void triggerRecording(long currentTime, int recordDurationMs, File currentWavFile, boolean isRecording) {
        // La lógica real de creación de archivo está embebida en el bucle principal
        // Esta función podría usarse si quisiéramos externalizar, pero preferí dejar el
        // estado local del thread en `runSentinel`.
        // Sirve como marcador semántico en el código.
    }

    private void closeWavFile(FileOutputStream fos, File file, long totalAudioLen) {
        if (fos != null) {
            try {
                fos.close();
                long totalDataLen = totalAudioLen + 36;
                long byteRate = SAMPLE_RATE * 2; // 16 bit mono

                // Actualizar cabecera con el tamaño final usando RandomAccessFile
                if (file != null && file.exists()) {
                    try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                        raf.seek(4); // File size
                        raf.writeInt(Integer.reverseBytes((int) totalDataLen));
                        raf.seek(40); // Data size
                        raf.writeInt(Integer.reverseBytes((int) totalAudioLen));
                    }
                    Log.d(TAG, "WAV cerrado correctamente. Tamaño real: " + totalAudioLen + " bytes.");
                }

            } catch (IOException e) {
                Log.e(TAG, "Error cerrando archivo WAV", e);
            }
        }
    }

    private void writeWavHeader(FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, byte channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
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
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
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
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private double calculateAmplitude(short[] buffer, int readSize) {
        double maxAmplitude = 0;
        for (int i = 0; i < readSize; i++) {
            if (Math.abs(buffer[i]) > maxAmplitude) {
                maxAmplitude = Math.abs(buffer[i]);
            }
        }
        return maxAmplitude;
    }
}
