package com.david.eloidodelabuelo;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.CopyOnWriteArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioSentinel {

    private static final String TAG = "AudioSentinel";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Nombres de las preferencias (V31)
    private static final String PREF_MIC_ENABLED = "MIC_ENABLED";
    private static final String PREF_AUTO_DETECTION = "AUTO_DETECTION_ENABLED"; // Nuevo V38
    private static final String PREF_FORCE_RECORD = "FORCE_RECORD";
    private static final String PREF_SHIELD_ENABLED = "SHIELD_ENABLED";
    private static final String PREF_SPIKE_THRESHOLD = "SPIKE_THRESHOLD";
    private static final String PREF_REQUIRED_SPIKES = "REQUIRED_SPIKES";
    private static final String PREF_SHIELD_WINDOW_MS = "SHIELD_WINDOW_MS";
    private static final String PREF_RECORD_DURATION_MS = "RECORD_DURATION_MS";

    private boolean isRunning = false;
    private Thread sentinelThread;
    private AudioRecord audioRecord;
    private Context context;
    private SharedPreferences prefs;

    // Variables de configuración cacheadas en RAM (Eco-Mode V27 -> V31)
    private volatile boolean micEnabledCached = true;
    private volatile boolean autoDetectionCached = true; // Nuevo V38
    private volatile boolean forceRecordCached = false;
    private volatile boolean shieldEnabledCached = true;
    private volatile int spikeThresholdCached = 10000;
    private volatile int requiredSpikesCached = 3;
    private volatile int shieldWindowMsCached = 500;
    private volatile int recordDurationMsCached = 15000;

    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = (sharedPreferences, key) -> {
        if (key == null)
            return;
        switch (key) {
            case PREF_MIC_ENABLED:
                micEnabledCached = sharedPreferences.getBoolean(PREF_MIC_ENABLED, true);
                break;
            case PREF_AUTO_DETECTION:
                autoDetectionCached = sharedPreferences.getBoolean(PREF_AUTO_DETECTION, true);
                break;
            case PREF_FORCE_RECORD:
                forceRecordCached = sharedPreferences.getBoolean(PREF_FORCE_RECORD, false);
                break;
            case PREF_SHIELD_ENABLED:
                shieldEnabledCached = sharedPreferences.getBoolean(PREF_SHIELD_ENABLED, true);
                break;
            case PREF_SPIKE_THRESHOLD:
                spikeThresholdCached = sharedPreferences.getInt(PREF_SPIKE_THRESHOLD, 10000);
                break;
            case PREF_REQUIRED_SPIKES:
                requiredSpikesCached = sharedPreferences.getInt(PREF_REQUIRED_SPIKES, 3);
                break;
            case PREF_SHIELD_WINDOW_MS:
                shieldWindowMsCached = sharedPreferences.getInt(PREF_SHIELD_WINDOW_MS, 500);
                break;
            case PREF_RECORD_DURATION_MS:
                recordDurationMsCached = sharedPreferences.getInt(PREF_RECORD_DURATION_MS, 15000);
                break;
        }
        Log.d(TAG, "Preferencia actualizada en RAM: " + key);
    };

    private volatile double currentAmplitude = 0;
    private volatile boolean isRecordingStatus = false;
    private volatile Long recordingStartTimestamp = null;
    private final CopyOnWriteArrayList<OutputStream> liveListeners = new CopyOnWriteArrayList<>();

    public void addLiveListener(OutputStream os) {
        liveListeners.add(os);
    }

    public void removeLiveListener(OutputStream os) {
        liveListeners.remove(os);
    }

    public double getCurrentAmplitude() {
        return currentAmplitude;
    }

    public boolean isCurrentlyRecording() {
        return isRecordingStatus;
    }

    public Long getRecordingStartTimestamp() {
        return recordingStartTimestamp;
    }

    public void updateForceRecordTimestamp(boolean isForced) {
        if (isForced) {
            recordingStartTimestamp = System.currentTimeMillis();
        } else {
            recordingStartTimestamp = null;
        }
    }

    public AudioSentinel(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE);

        // Inicializar cache inicial (V31)
        micEnabledCached = prefs.getBoolean(PREF_MIC_ENABLED, true);
        autoDetectionCached = prefs.getBoolean(PREF_AUTO_DETECTION, true);
        forceRecordCached = prefs.getBoolean(PREF_FORCE_RECORD, false);
        if (forceRecordCached) {
            recordingStartTimestamp = System.currentTimeMillis();
        }

        shieldEnabledCached = prefs.getBoolean(PREF_SHIELD_ENABLED, true);
        spikeThresholdCached = prefs.getInt(PREF_SPIKE_THRESHOLD, 10000);
        requiredSpikesCached = prefs.getInt(PREF_REQUIRED_SPIKES, 3);
        shieldWindowMsCached = prefs.getInt(PREF_SHIELD_WINDOW_MS, 500);
        recordDurationMsCached = prefs.getInt(PREF_RECORD_DURATION_MS, 15000);

        // Registrar listener para evitar lecturas de disco futuras
        this.prefs.registerOnSharedPreferenceChangeListener(prefListener);
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

        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Buffer size error");
            return;
        }

        // CUADRUPLICAR el buffer (Eco-Mode V27) para reducir wake-ups de CPU
        int bufferSize = minBufferSize * 4;

        SharedPreferences prefs = context.getSharedPreferences("OidoPrefs", Context.MODE_PRIVATE);

        // Variables de estado del Escudo Anti-Falsos Positivos
        int spikeCount = 0;
        long firstSpikeTime = 0;

        // Variables de estado de Grabación
        boolean isRecording = false;
        long recordingEndTime = 0;
        FileOutputStream fos = null;
        File currentAacFile = null;

        // Chivato JSON: Lista de picos para la forma de onda (V49)
        java.util.List<Integer> wavePeaks = new java.util.ArrayList<>();

        // Variables MediaCodec (V28)
        MediaCodec codec = null;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

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
                // 1. Lectura de Preferencias desde RAM (Eco-Mode V31)
                // NO consultamos SharedPreferences.getXXX en cada ciclo
                boolean micEnabled = micEnabledCached;
                boolean autoDetection = autoDetectionCached;
                boolean forceRecord = forceRecordCached;
                boolean shieldEnabled = shieldEnabledCached;
                int spikeThreshold = spikeThresholdCached;
                int requiredSpikes = requiredSpikesCached;
                int shieldWindowMs = shieldWindowMsCached;
                int recordDurationMs = recordDurationMsCached;

                boolean hasListeners = !liveListeners.isEmpty();

                // 2. Modo Kill Switch (Micrófono Apagado)
                if (!micEnabled) {
                    if (isRecording) {
                        // Forzar cierre si se apaga de golpe
                        isRecording = false;
                        isRecordingStatus = false;
                        if (codec != null) {
                            try {
                                codec.stop();
                                codec.release();
                            } catch (Exception e) {
                            }
                            codec = null;
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                            }
                            fos = null;
                        }
                    }
                    // Leemos el buffer para drenarlo de la RAM y evitar Crash/Overflow en Xiaomi
                }

                // Leer audio (Esto bloqueará el hilo eficientemente)
                int readResult = audioRecord.read(buffer, 0, buffer.length);
                if (readResult > 0) {
                    long currentTime = System.currentTimeMillis();

                    // Si el micro está apagado por software, forzamos silencio matemático
                    double amplitude = !micEnabled ? 0 : calculateAmplitude(buffer, readResult);
                    this.currentAmplitude = amplitude;

                    // Chivato JSON: Capturar pico si estamos grabando (V49)
                    if (isRecording) {
                        wavePeaks.add((int) amplitude);
                    }

                    boolean wantToRecord = false;

                    // 3. Lógica del Modo de Operación (V31)
                    if (micEnabled) {
                        if (forceRecord) {
                            wantToRecord = true; // Continuo absoluto guiado por el Botón REC
                        } else if (autoDetection) { // Detección Clásica solo si está habilitada (V38)
                            if (amplitude > spikeThreshold) {
                                if (!shieldEnabled) {
                                    wantToRecord = true;
                                } else {
                                    if (spikeCount == 0 || (currentTime - firstSpikeTime) > shieldWindowMs) {
                                        firstSpikeTime = currentTime;
                                        spikeCount = 1;
                                    } else {
                                        spikeCount++;
                                    }
                                    if (spikeCount >= requiredSpikes) {
                                        wantToRecord = true;
                                        spikeCount = 0;
                                    }
                                }
                            }
                            if (wantToRecord) {
                                recordingEndTime = currentTime + recordDurationMs;
                            } else if (isRecording && currentTime < recordingEndTime) {
                                wantToRecord = true; // Seguimos dentro de la ventana del Trigger
                            }
                        }
                    }

                    // 4. Gestión del MediaCodec y Archivo
                    if (wantToRecord && !isRecording) {
                        isRecording = true;
                        isRecordingStatus = true;
                        recordingStartTimestamp = System.currentTimeMillis(); // V39.1 Fix: Asignar tiempo para el
                                                                              // cronómetro
                        try {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                    .format(new Date());
                            String fileName = "Oido_" + timeStamp + ".m4a";
                            currentAacFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                                    fileName);
                            fos = new FileOutputStream(currentAacFile);

                            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                                    SAMPLE_RATE, 1);
                            format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                                    MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                            format.setInteger(MediaFormat.KEY_BIT_RATE, 32000);
                            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize * 2);

                            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                            codec.start();
                            Log.d(TAG, "Grabación AAC iniciada: " + currentAacFile.getName());
                            spikeCount = 0;
                        } catch (IOException e) {
                            Log.e(TAG, "Error iniciando MediaCodec/Archivo", e);
                            isRecording = false;
                            isRecordingStatus = false;
                            recordingStartTimestamp = null;
                        }
                    } else if (!wantToRecord && isRecording) {
                        isRecording = false;
                        isRecordingStatus = false;
                        recordingStartTimestamp = null; // Liberar timestamp al terminar
                        if (codec != null) {
                            try {
                                codec.stop();
                                codec.release();
                            } catch (Exception e) {
                                Log.e(TAG, "Error liberando codec", e);
                            }
                            codec = null;
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error cerrando archivo aac", e);
                            }
                            fos = null;
                        }

                        // Chivato JSON: Guardar picos diezmados (1 de cada 2) como archivo .json (V49)
                        if (currentAacFile != null && !wavePeaks.isEmpty()) {
                            try {
                                String jsonName = currentAacFile.getName().replaceAll("\\.[^.]+$", ".json");
                                File jsonFile = new File(currentAacFile.getParentFile(), jsonName);
                                StringBuilder sb = new StringBuilder("[");
                                for (int pi = 0; pi < wavePeaks.size(); pi += 2) {
                                    if (pi > 0)
                                        sb.append(",");
                                    sb.append(wavePeaks.get(pi));
                                }
                                sb.append("]");
                                FileWriter fw = new FileWriter(jsonFile);
                                fw.write(sb.toString());
                                fw.close();
                                Log.d(TAG, "Chivato JSON guardado: " + jsonFile.getName() + " ("
                                        + (wavePeaks.size() / 2) + " picos)");
                            } catch (Exception e) {
                                Log.e(TAG, "Error guardando chivato JSON", e);
                            }
                        }
                        wavePeaks.clear();

                        Log.d(TAG, "Grabación AAC detenida.");
                    }

                    // 5. Procesamiento Multitarea: Inyección de ADTS (Disco + Red)
                    if (micEnabled && (isRecording || hasListeners)) {
                        // Si hay escuchas pero no estamos grabando a disco, encendemos un Codec
                        // fantasma
                        if (codec == null && hasListeners && !isRecording) {
                            try {
                                MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                                        SAMPLE_RATE, 1);
                                format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                                        MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                                format.setInteger(MediaFormat.KEY_BIT_RATE, 32000);
                                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize * 2);
                                codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                                codec.start();
                                Log.d(TAG, "Codec Fantasma (Vivo) iniciado.");
                            } catch (Exception e) {
                                Log.e(TAG, "Error iniciando codec fantasma para red", e);
                            }
                        }

                        if (codec != null) {
                            // Alimentar el MediaCodec (In)
                            int inputBufferIndex = codec.dequeueInputBuffer(10000);
                            if (inputBufferIndex >= 0) {
                                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                                inputBuffer.clear();

                                byte[] byteBufferOut = new byte[readResult * 2];
                                for (int i = 0; i < readResult; i++) {
                                    byteBufferOut[i * 2] = (byte) (buffer[i] & 0x00FF);
                                    byteBufferOut[(i * 2) + 1] = (byte) (buffer[i] >> 8);
                                }
                                inputBuffer.put(byteBufferOut);
                                codec.queueInputBuffer(inputBufferIndex, 0, byteBufferOut.length, currentTime * 1000,
                                        0);
                            }

                            // Ordeñar el MediaCodec (Out) y empaquetar en ADTS
                            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000);
                            while (outputBufferIndex >= 0) {
                                // Ignorar CSD (Codec Specific Data) porque ADTS es autodescriptivo
                                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                    bufferInfo.size = 0;
                                }

                                if (bufferInfo.size > 0) {
                                    int outBitsSize = bufferInfo.size;
                                    int outPacketSize = outBitsSize + 7; // ADTS header es de 7 bytes
                                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);

                                    outputBuffer.position(bufferInfo.offset);
                                    outputBuffer.limit(bufferInfo.offset + outBitsSize);

                                    byte[] outData = new byte[outPacketSize];
                                    addADTStoPacket(outData, outPacketSize); // Inyectar cabecera ADTS
                                    outputBuffer.get(outData, 7, outBitsSize);
                                    outputBuffer.clear();

                                    // A Disco
                                    if (isRecording && fos != null) {
                                        try {
                                            fos.write(outData);
                                        } catch (IOException e) {
                                        }
                                    }

                                    // A Red
                                    if (hasListeners) {
                                        for (OutputStream os : liveListeners) {
                                            try {
                                                os.write(outData);
                                                os.flush();
                                            } catch (IOException e) {
                                                liveListeners.remove(os);
                                            }
                                        }
                                    }
                                }

                                codec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                            }
                        }
                    } else if ((!micEnabled || (!isRecording && !hasListeners)) && codec != null) {
                        try {
                            codec.stop();
                            codec.release();
                        } catch (Exception e) {
                        }
                        codec = null;
                        Log.d(TAG, "Codec Fantasma detenido.");
                    }
                } else {
                    Log.e(TAG, "AudioRecord fallback in loop. readResult=" + readResult);
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }
                }
            }

            // Cierre seguro
            if (codec != null) {
                try {
                    codec.stop();
                    codec.release();
                } catch (Exception e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
            isRecordingStatus = false;

        } catch (Exception e) {
            Log.e(TAG, "Excepción en bucle centinela", e);
        } finally {
            if (audioRecord != null) {
                try {
                    audioRecord.stop();
                    audioRecord.release();
                } catch (Exception e) {
                }
            }
            Log.d(TAG, "Centinela detenido y recursos liberados.");
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 8; // 16 KHz
        int chanCfg = 1; // Mono

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1; // 0xF1 es MPEG-4 AAC. 0xF9 sería MPEG-2. Chrome/Safari requieren exactitud
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
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
