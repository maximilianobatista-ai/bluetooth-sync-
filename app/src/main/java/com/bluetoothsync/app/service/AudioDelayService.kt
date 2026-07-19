package com.bluetoothsync.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bluetoothsync.app.MainActivity
import com.bluetoothsync.app.R
import com.bluetoothsync.app.audio.AudioEngine

class AudioDelayService : Service() {

    companion object {
        const val CHANNEL_ID = "bluetooth_sync_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.bluetoothsync.app.START"
        const val ACTION_STOP = "com.bluetoothsync.app.STOP"
        const val EXTRA_DELAY_MS = "delay_ms"
    }

    private val binder = LocalBinder()
    private var audioEngine: AudioEngine? = null

    inner class LocalBinder : Binder() {
        fun getService(): AudioDelayService = this@AudioDelayService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioEngine = AudioEngine(this)
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val delayMs = intent.getIntExtra(EXTRA_DELAY_MS, 0)
                startForegroundService(delayMs)
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService(delayMs: Int) {
        val notification = createNotification("Sincronizando... ${delayMs}ms delay")
        startForeground(NOTIFICATION_ID, notification)
        audioEngine?.startPlayback(delayMs)
    }

    private fun stopForegroundService() {
        audioEngine?.stopPlayback()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bluetooth Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio de compensación de latencia Bluetooth"
                setSound(null, null)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bluetooth Sync Activo")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun updateDelay(delayMs: Int) {
        audioEngine?.setDelay(delayMs)
        val notification = createNotification("Delay: ${delayMs}ms")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun getAudioEngine(): AudioEngine? = audioEngine

    override fun onDestroy() {
        audioEngine?.release()
        super.onDestroy()
    }
}
