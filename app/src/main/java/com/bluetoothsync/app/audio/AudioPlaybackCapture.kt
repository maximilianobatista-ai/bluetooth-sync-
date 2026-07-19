package com.bluetoothsync.app.audio

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlaybackCapture(private val context: Context) {

    companion object {
        private const val TAG = "AudioPlaybackCapture"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var isCapturing = false
    private var captureScope = CoroutineScope(Dispatchers.IO + Job())

    private val _isCapturing = MutableStateFlow(false)
    val isCapturingState: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private var onAudioDataCallback: ((ShortArray) -> Unit)? = null

    fun getMediaProjectionManager(): MediaProjectionManager {
        return context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    fun startCapture(
        mediaProjection: MediaProjection,
        onAudioData: (ShortArray) -> Unit
    ) {
        this.mediaProjection = mediaProjection
        this.onAudioDataCallback = onAudioData

        val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .build()

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

        try {
            audioRecord = AudioRecord.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setAudioPlaybackCaptureConfig(config)
                .build()

            audioRecord?.startRecording()
            isCapturing = true
            _isCapturing.value = true

            captureScope.launch {
                val buffer = ShortArray(1024)
                while (isCapturing && isActive) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // Copiar solo los datos leídos
                        val data = buffer.copyOf(read)
                        onAudioDataCallback?.invoke(data)
                    }
                    delay(1)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting capture: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stopCapture() {
        isCapturing = false
        _isCapturing.value = false

        captureScope.cancel()
        captureScope = CoroutineScope(Dispatchers.IO + Job())

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        mediaProjection?.stop()
        mediaProjection = null
    }

    fun release() {
        stopCapture()
    }
}
