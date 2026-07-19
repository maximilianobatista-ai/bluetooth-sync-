package com.bluetoothsync.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*

class AudioDelayProcessor(private val context: android.content.Context) {

    companion object {
        private const val TAG = "AudioDelayProcessor"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioTrack: AudioTrack? = null
    private var isProcessing = false
    private var processingScope = CoroutineScope(Dispatchers.IO + Job())

    // Ring buffer para delay
    private var ringBuffer: ShortArray? = null
    private var ringWriteIndex = 0
    private var ringSize = 0
    private var delayMs: Int = 0

    private fun createAudioTrack(): AudioTrack {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AUDIO_FORMAT)
            .setChannelMask(CHANNEL_CONFIG)
            .build()

        val bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 4

        return AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun initDelayBuffer(delayMs: Int) {
        this.delayMs = delayMs
        val delaySamples = (delayMs * SAMPLE_RATE / 1000)
        ringSize = delaySamples * 2  // Stereo = 2 canales
        ringBuffer = ShortArray(ringSize) { 0 }
        ringWriteIndex = 0
        Log.d(TAG, "Delay buffer initialized: $delayMs ms = $delaySamples samples")
    }

    fun startProcessing(delayMs: Int) {
        if (isProcessing) stopProcessing()

        initDelayBuffer(delayMs)

        try {
            audioTrack = createAudioTrack()
            audioTrack?.play()
            isProcessing = true

        } catch (e: Exception) {
            Log.e(TAG, "Error starting processing: ${e.message}")
        }
    }

    fun processAudioChunk(inputData: ShortArray) {
        if (!isProcessing || audioTrack == null) return

        val ring = ringBuffer ?: return
        val outputData = ShortArray(inputData.size)

        for (i in inputData.indices) {
            // Escribir en ring buffer
            ring[ringWriteIndex] = inputData[i]
            ringWriteIndex = (ringWriteIndex + 1) % ring.size

            // Leer con delay: el índice de lectura está "delayMs" atrás del de escritura
            val readIndex = (ringWriteIndex - ringSize + ring.size) % ring.size
            outputData[i] = ring[readIndex]
        }

        audioTrack?.write(outputData, 0, outputData.size)
    }

    fun setDelay(newDelayMs: Int) {
        if (newDelayMs != delayMs) {
            initDelayBuffer(newDelayMs)
        }
    }

    fun stopProcessing() {
        isProcessing = false
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    fun release() {
        stopProcessing()
        processingScope.cancel()
    }
}
