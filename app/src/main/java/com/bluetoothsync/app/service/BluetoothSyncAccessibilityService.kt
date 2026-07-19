package com.bluetoothsync.app.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.bluetoothsync.app.R
import com.bluetoothsync.app.audio.AudioDelayProcessor
import com.bluetoothsync.app.audio.AudioPlaybackCapture

class BluetoothSyncAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BtSyncAccessibility"
        private const val CHANNEL_ID = "bluetooth_sync_accessibility"
        private const val NOTIFICATION_ID = 100

        const val ACTION_START_CAPTURE = "com.bluetoothsync.app.START_CAPTURE"
        const val ACTION_STOP_CAPTURE = "com.bluetoothsync.app.STOP_CAPTURE"
        const val ACTION_UPDATE_DELAY = "com.bluetoothsync.app.UPDATE_DELAY"
        const val EXTRA_DELAY_MS = "delay_ms"

        var instance: BluetoothSyncAccessibilityService? = null
            private set
    }

    private var audioCapture: AudioPlaybackCapture? = null
    private var delayProcessor: AudioDelayProcessor? = null
    private var currentDelayMs: Int = 0
    private var isServiceActive = false

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_START_CAPTURE -> startCapture()
                ACTION_STOP_CAPTURE -> stopCapture()
                ACTION_UPDATE_DELAY -> {
                    val delay = intent.getIntExtra(EXTRA_DELAY_MS, 0)
                    updateDelay(delay)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")

        // Registrar receiver para comandos
        val filter = IntentFilter().apply {
            addAction(ACTION_START_CAPTURE)
            addAction(ACTION_STOP_CAPTURE)
            addAction(ACTION_UPDATE_DELAY)
        }
        registerReceiver(commandReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        // Inicializar componentes de audio
        audioCapture = AudioPlaybackCapture(this)
        delayProcessor = AudioDelayProcessor(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No necesitamos procesar eventos de accesibilidad
        // Solo usamos el servicio para mantener la app en ejecución
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopCapture()
        try {
            unregisterReceiver(commandReceiver)
        } catch (e: Exception) { }
        audioCapture?.release()
        delayProcessor?.release()
    }

    fun startCapture() {
        if (isServiceActive) return

        // Mostrar notificación persistente
        showForegroundNotification()

        // Iniciar captura de audio del sistema
        // Nota: MediaProjection requiere una actividad para obtener el token
        // La app principal debe iniciar el MediaProjection y pasarlo aquí
        isServiceActive = true
        Log.d(TAG, "Capture started")
    }

    fun startCaptureWithMediaProjection(
        mediaProjection: android.media.projection.MediaProjection,
        delayMs: Int
    ) {
        currentDelayMs = delayMs
        delayProcessor?.startProcessing(delayMs)

        audioCapture?.startCapture(mediaProjection) { audioData ->
            delayProcessor?.processAudioChunk(audioData)
        }

        isServiceActive = true
        showForegroundNotification()
    }

    fun stopCapture() {
        audioCapture?.stopCapture()
        delayProcessor?.stopProcessing()
        isServiceActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "Capture stopped")
    }

    fun updateDelay(delayMs: Int) {
        currentDelayMs = delayMs
        delayProcessor?.setDelay(delayMs)
        updateNotification("Delay: ${delayMs}ms")
    }

    private fun showForegroundNotification() {
        val channel = android.app.NotificationChannel(
            CHANNEL_ID,
            "Bluetooth Sync Accessibility",
            android.app.NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Servicio de sincronización Bluetooth activo"
            setSound(null, null)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.createNotificationChannel(channel)

        val notification = createNotification("Sincronización activa")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(content: String): android.app.Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bluetooth Sync")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(content))
    }

    fun isActive(): Boolean = isServiceActive
}
