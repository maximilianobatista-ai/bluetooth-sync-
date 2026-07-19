package com.bluetoothsync.app.data

enum class DeviceCategory {
    HEADPHONES, SPEAKER, EARBUDS, GAMING, UNKNOWN
}

data class BluetoothDeviceProfile(
    val name: String,
    val brand: String,
    val typicalLatencyMs: Int,
    val codec: String,
    val category: DeviceCategory
)

data class SyncState(
    val isPlaying: Boolean = false,
    val currentDelayMs: Int = 0,
    val detectedLatencyMs: Int? = null,
    val connectedDevice: BluetoothDeviceProfile? = null,
    val isDetecting: Boolean = false,
    val syncQuality: SyncQuality = SyncQuality.UNCALIBRATED
)

enum class SyncQuality {
    UNCALIBRATED, POOR, FAIR, GOOD, EXCELLENT
}
