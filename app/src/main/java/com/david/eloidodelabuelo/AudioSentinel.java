package com.david.eloidodelabuelo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioSentinel {

    private static final String TAG = "AudioSentinel";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int NOISE_THRESHOLD = 500; // Umbral inicial arbitrario

    private boolean isRunning = false;
    private Thread sentinelThread;
    private AudioRecord audioRecord;

    public void start() {
        if (isRunning) return;
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

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord no inicializado");
                return;
            }

            audioRecord.startRecording();
            short[] buffer = new short[bufferSize / 2];

            while (isRunning) {
                int readResult = audioRecord.read(buffer, 0, buffer.length);
                if (readResult > 0) {
                    double amplitude = calculateAmplitude(buffer, readResult);
                    if (amplitude > NOISE_THRESHOLD) {
                        Log.d(TAG, "Ruido detectado: " + amplitude);
                    }
                } else {
                     Log.w(TAG, "Error leyendo audio: " + readResult);
                }
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
