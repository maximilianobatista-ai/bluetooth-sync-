package com.bluetoothsync.app.audio

import android.content.Context
import android.media.*
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

class AudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "AudioEngine"
        private const val SAMPLE_RATE = 44100
        private const val BEAT_BPM = 120.0
        private const val BEAT_INTERVAL_MS = (60000.0 / BEAT_BPM).toLong()
    }

    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    private var isMetronomeRunning = false
    private var delayMs: Int = 0

    private val _latencyMeasurement = MutableStateFlow<Int?>(null)
    val latencyMeasurement: StateFlow<Int?> = _latencyMeasurement.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var metronomeJob: Job? = null
    private var audioScope = CoroutineScope(Dispatchers.Default + Job())

    // Ring buffer para delay
    private var delayRingBuffer: ShortArray? = null
    private var ringWriteIndex = 0
    private var ringReadIndex = 0

    private fun getAudioTrackBufferSize(): Int {
        return AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 4
    }

    private fun createAudioTrack(): AudioTrack {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()

        return AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(getAudioTrackBufferSize())
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun startPlayback(delayMs: Int) {
        if (isRunning) stopPlayback()

        this.delayMs = delayMs
        initDelayBuffer(delayMs)

        try {
            audioTrack = createAudioTrack()
            audioTrack?.play()
            isRunning = true
            _isPlaying.value = true

            startAudioStream()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback: ${e.message}")
            stopPlayback()
        }
    }

    fun startMetronome(delayMs: Int) {
        if (isMetronomeRunning) stopMetronome()

        this.delayMs = delayMs
        initDelayBuffer(delayMs)

        try {
            audioTrack = createAudioTrack()
            audioTrack?.play()
            isMetronomeRunning = true
            _isPlaying.value = true

            startMetronomeStream()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting metronome: ${e.message}")
            stopPlayback()
        }
    }

    private fun initDelayBuffer(delayMs: Int) {
        val delaySamples = (delayMs * SAMPLE_RATE / 1000)
        delayRingBuffer = ShortArray(delaySamples * 2) { 0 }
        ringWriteIndex = 0
        ringReadIndex = 0
    }

    private fun applyDelayToBuffer(input: ShortArray, output: ShortArray) {
        val ring = delayRingBuffer ?: return
        if (ring.isEmpty()) {
            input.copyInto(output)
            return
        }

        for (i in input.indices) {
            // Escribir en ring buffer
            ring[ringWriteIndex] = input[i]
            ringWriteIndex = (ringWriteIndex + 1) % ring.size

            // Leer del ring buffer (con delay)
            output[i] = ring[ringReadIndex]
            ringReadIndex = (ringReadIndex + 1) % ring.size
        }
    }

    private fun startAudioStream() {
        audioScope.launch {
            val bufferSize = 1024
            val inputBuffer = ShortArray(bufferSize)
            val outputBuffer = ShortArray(bufferSize)

            while (isRunning && isActive) {
                // En una app real, aquí capturaríamos audio del sistema
                // Por ahora generamos silencio con posibilidad de overlay
                inputBuffer.fill(0)
                applyDelayToBuffer(inputBuffer, outputBuffer)
                audioTrack?.write(outputBuffer, 0, outputBuffer.size)
                delay(1)
            }
        }
    }

    private fun startMetronomeStream() {
        audioScope.launch {
            val beatSamples = (SAMPLE_RATE * 60 / BEAT_BPM).toInt()
            val clickDuration = (SAMPLE_RATE * 0.05).toInt() // 50ms click
            var sampleCounter = 0
            var isClick = false
            var clickCounter = 0

            val bufferSize = 512
            val inputBuffer = ShortArray(bufferSize)
            val outputBuffer = ShortArray(bufferSize)

            while (isMetronomeRunning && isActive) {
                for (i in 0 until bufferSize) {
                    // Generar metrónomo
                    val isBeat = sampleCounter % beatSamples < clickDuration

                    val sample = if (isBeat) {
                        val freq = if ((sampleCounter / beatSamples) % 4 == 0) 880.0 else 440.0
                        val phase = (sampleCounter % beatSamples) * 2.0 * PI * freq / SAMPLE_RATE
                        (sin(phase) * 8000).toInt().toShort()
                    } else {
                        0
                    }

                    inputBuffer[i] = sample
                    sampleCounter++
                }

                applyDelayToBuffer(inputBuffer, outputBuffer)
                audioTrack?.write(outputBuffer, 0, bufferSize)
            }
        }
    }

    fun playClickTest(delayMs: Int) {
        this.delayMs = delayMs
        initDelayBuffer(delayMs)

        audioScope.launch {
            try {
                val track = createAudioTrack()
                track.play()

                // Click 1 (inmediato)
                val click1 = generateClick(880.0, 0.05)
                track.write(click1, 0, click1.size)

                // Click 2 (delayado)
                val delaySamples = (delayMs * SAMPLE_RATE / 1000)
                val silence = ShortArray(delaySamples) { 0 }
                val click2 = generateClick(440.0, 0.05)

                track.write(silence, 0, silence.size)
                track.write(click2, 0, click2.size)

                delay(1000)
                track.stop()
                track.release()

            } catch (e: Exception) {
                Log.e(TAG, "Error in click test: ${e.message}")
            }
        }
    }

    private fun generateClick(frequency: Double, durationSeconds: Double): ShortArray {
        val numSamples = (SAMPLE_RATE * durationSeconds).toInt()
        return ShortArray(numSamples) { i ->
            val phase = i * 2.0 * PI * frequency / SAMPLE_RATE
            val envelope = exp(-i * 8.0 / numSamples) // Decay exponencial
            (sin(phase) * 8000 * envelope).toInt().toShort()
        }
    }

    suspend fun detectLatency(): Int? = withContext(Dispatchers.Default) {
        try {
            // Verificar permiso de grabación
            if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return@withContext null
            }

            val recordBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            if (recordBufferSize <= 0) {
                Log.e(TAG, "Invalid record buffer size: $recordBufferSize")
                return@withContext null
            }

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recordBufferSize * 2
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized")
                return@withContext null
            }

            // Preparar buffer de grabación
            val recordBuffer = ShortArray(recordBufferSize)

            // Iniciar grabación
            audioRecord.startRecording()

            // Emitir tono de prueba por el speaker
            val testTone = generateClick(1000.0, 0.1)
            val testTrack = createAudioTrack()
            testTrack.play()

            val startTime = System.nanoTime()
            testTrack.write(testTone, 0, testTone.size)

            // Esperar y capturar eco
            var maxLatency = 500 // ms máximo a esperar
            var detectedLatency: Int? = null
            var totalRead = 0
            val startReadTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startReadTime < maxLatency && detectedLatency == null) {
                val read = audioRecord.read(recordBuffer, 0, recordBuffer.size)
                if (read > 0) {
                    // Buscar pico de energía (eco)
                    val windowSize = 512
                    for (i in 0 until read - windowSize step windowSize / 4) {
                        var energy = 0L
                        for (j in i until i + windowSize) {
                            energy += abs(recordBuffer[j].toLong())
                        }

                        // Umbral de detección
                        if (energy > windowSize * 500) {
                            val sampleOffset = totalRead + i
                            detectedLatency = (sampleOffset * 1000 / SAMPLE_RATE).toInt()
                            break
                        }
                    }
                    totalRead += read
                }
                delay(10)
            }

            // Cleanup
            audioRecord.stop()
            audioRecord.release()
            testTrack.stop()
            testTrack.release()

            // Ajustar por overhead del sistema
            val adjustedLatency = detectedLatency?.let { max(0, it - 20) }

            _latencyMeasurement.value = adjustedLatency
            adjustedLatency

        } catch (e: Exception) {
            Log.e(TAG, "Error detecting latency: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun setDelay(newDelayMs: Int) {
        this.delayMs = newDelayMs
        initDelayBuffer(newDelayMs)
    }

    fun stopPlayback() {
        isRunning = false
        isMetronomeRunning = false
        _isPlaying.value = false

        metronomeJob?.cancel()
        metronomeJob = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun stopMetronome() {
        isMetronomeRunning = false
        if (!isRunning) {
            _isPlaying.value = false
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }

    fun release() {
        stopPlayback()
        audioScope.cancel()
    }
}
