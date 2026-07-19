package com.bluetoothsync.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothsync.app.audio.AudioEngine
import com.bluetoothsync.app.data.BluetoothDeviceProfile
import com.bluetoothsync.app.data.LatencyDatabase
import com.bluetoothsync.app.data.SyncQuality
import com.bluetoothsync.app.data.SyncState
import com.bluetoothsync.app.service.AudioDelayService
import com.bluetoothsync.app.service.BluetoothSyncAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class CaptureMode {
    NONE,       // Sin captura activa
    METRONOME,  // Solo metrónomo de prueba
    SYSTEM      // Captura de audio del sistema (YouTube, Spotify, etc.)
}

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _captureMode = MutableStateFlow(CaptureMode.NONE)
    val captureMode: StateFlow<CaptureMode> = _captureMode.asStateFlow()

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<BluetoothDeviceProfile>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothDeviceProfile>> = _availableDevices.asStateFlow()

    private val audioEngine = AudioEngine(application)

    init {
        _availableDevices.value = LatencyDatabase.getAll()
        checkAccessibilityService()
    }

    // ==================== DELAY ====================

    fun setDelay(delayMs: Int) {
        val clamped = delayMs.coerceIn(0, 500)
        _state.value = _state.value.copy(currentDelayMs = clamped)
        audioEngine.setDelay(clamped)

        // Si hay captura del sistema activa, actualizar delay en tiempo real
        if (_captureMode.value == CaptureMode.SYSTEM) {
            val context = getApplication<Application>()
            val intent = Intent(BluetoothSyncAccessibilityService.ACTION_UPDATE_DELAY).apply {
                setPackage(context.packageName)
                putExtra(BluetoothSyncAccessibilityService.EXTRA_DELAY_MS, clamped)
            }
            context.sendBroadcast(intent)
        }

        updateSyncQuality()
    }

    fun adjustDelay(delta: Int) {
        val newDelay = (_state.value.currentDelayMs + delta).coerceIn(0, 500)
        setDelay(newDelay)
    }

    // ==================== METRÓNOMO ====================

    fun toggleMetronome() {
        val current = _state.value.isPlaying
        if (!current) {
            audioEngine.startMetronome(_state.value.currentDelayMs)
            _captureMode.value = CaptureMode.METRONOME
        } else {
            audioEngine.stopMetronome()
            if (_captureMode.value == CaptureMode.METRONOME) {
                _captureMode.value = CaptureMode.NONE
            }
        }
        _state.value = _state.value.copy(isPlaying = !current)
    }

    fun stopAllPlayback() {
        audioEngine.stopPlayback()
        _state.value = _state.value.copy(isPlaying = false)
        _captureMode.value = CaptureMode.NONE
    }

    fun playClickTest() {
        audioEngine.playClickTest(_state.value.currentDelayMs)
    }

    // ==================== CAPTURA DEL SISTEMA ====================

    fun startSystemCapture() {
        val context = getApplication<Application>()

        // Verificar si el servicio de accesibilidad está activo
        if (!isAccessibilityServiceEnabled(context)) {
            _isAccessibilityEnabled.value = false
            return
        }

        _isAccessibilityEnabled.value = true

        // Lanzar CaptureActivity para obtener MediaProjection
        val intent = CaptureActivity.createIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("delay_ms", _state.value.currentDelayMs)
        }
        context.startActivity(intent)

        _captureMode.value = CaptureMode.SYSTEM
        _state.value = _state.value.copy(isPlaying = true)
    }

    fun stopSystemCapture() {
        val context = getApplication<Application>()
        val intent = Intent(BluetoothSyncAccessibilityService.ACTION_STOP_CAPTURE).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)

        _captureMode.value = CaptureMode.NONE
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun isSystemCaptureActive(): Boolean {
        return _captureMode.value == CaptureMode.SYSTEM &&
               BluetoothSyncAccessibilityService.instance?.isActive() == true
    }

    // ==================== ACCESIBILIDAD ====================

    fun checkAccessibilityService() {
        val context = getApplication<Application>()
        _isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val componentName = ComponentName(context, BluetoothSyncAccessibilityService::class.java)
        return enabledServices.contains(componentName.flattenToString())
    }

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    // ==================== DETECCIÓN DE LATENCIA ====================

    fun detectLatency() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDetecting = true)
            try {
                val detected = audioEngine.detectLatency()
                _state.value = _state.value.copy(
                    isDetecting = false,
                    detectedLatencyMs = detected
                )
                detected?.let { setDelay(it) }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isDetecting = false)
            }
        }
    }

    // ==================== DISPOSITIVOS ====================

    fun setDeviceProfile(profile: BluetoothDeviceProfile?) {
        _state.value = _state.value.copy(connectedDevice = profile)
        profile?.let { setDelay(it.typicalLatencyMs) }
    }

    fun searchDevice(query: String) {
        if (query.isBlank()) {
            _availableDevices.value = LatencyDatabase.getAll()
        } else {
            _availableDevices.value = LatencyDatabase.getAll().filter {
                it.name.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true)
            }
        }
    }

    // ==================== SYNC QUALITY ====================

    private fun updateSyncQuality() {
        val detected = _state.value.detectedLatencyMs
        val current = _state.value.currentDelayMs

        val quality = if (detected != null) {
            val diff = abs(current - detected)
            when {
                diff < 15 -> SyncQuality.EXCELLENT
                diff < 40 -> SyncQuality.GOOD
                diff < 80 -> SyncQuality.FAIR
                else -> SyncQuality.POOR
            }
        } else {
            when {
                current == 0 -> SyncQuality.UNCALIBRATED
                current in 100..250 -> SyncQuality.FAIR
                current in 40..99 -> SyncQuality.GOOD
                current > 250 -> SyncQuality.POOR
                else -> SyncQuality.GOOD
            }
        }

        _state.value = _state.value.copy(syncQuality = quality)
    }

    override fun onCleared() {
        audioEngine.release()
        super.onCleared()
    }
}
