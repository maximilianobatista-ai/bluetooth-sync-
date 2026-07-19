package com.bluetoothsync.app.ui.components

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bluetoothsync.app.data.BluetoothDeviceProfile
import com.bluetoothsync.app.data.LatencyDatabase
import com.bluetoothsync.app.data.SyncState

@Composable
fun DeviceCard(
    state: SyncState,
    onDeviceSelected: (BluetoothDeviceProfile?) -> Unit,
    onDetectLatency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDevicePicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredDevices by remember { mutableStateOf(LatencyDatabase.getAll()) }

    // Detectar dispositivo Bluetooth conectado
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) 
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
                val adapter = bluetoothManager?.adapter
                val bonded = adapter?.bondedDevices?.toList() ?: emptyList()

                val activeDevice = bonded.firstOrNull { device ->
                    try {
                        device.type == android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE ||
                        device.bluetoothClass?.majorDeviceClass == android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO
                    } catch (e: SecurityException) { false }
                }

                activeDevice?.let { d ->
                    LatencyDatabase.findByName(d.name)?.let { profile ->
                        onDeviceSelected(profile)
                    }
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DISPOSITIVO DETECTADO",
                fontSize = 11.sp,
                color = Color(0xFF8892B0),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = state.connectedDevice,
                label = "deviceContent"
            ) { device ->
                if (device != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            device.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D4FF)
                        )
                        Text(
                            "${device.brand} • ${device.codec}",
                            fontSize = 13.sp,
                            color = Color(0xFF8892B0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Latencia estimada: ${device.typicalLatencyMs} ms",
                            fontSize = 14.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showDevicePicker = true }) {
                            Text("Cambiar dispositivo", color = Color(0xFF00D4FF))
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No detectado",
                            fontSize = 18.sp,
                            color = Color(0xFF667)
                        )
                        Text(
                            "Conecta un dispositivo Bluetooth",
                            fontSize = 12.sp,
                            color = Color(0xFF556)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showDevicePicker = true }) {
                            Text("Seleccionar manualmente", color = Color(0xFF00D4FF))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDetectLatency,
                enabled = !state.isDetecting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D4FF),
                    contentColor = Color(0xFF0F0F1A)
                )
            ) {
                if (state.isDetecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF0F0F1A),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (state.isDetecting) "Detectando..." else "🔍 Detectar automático",
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(visible = state.detectedLatencyMs != null) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Detectado: ${state.detectedLatencyMs} ms",
                        fontSize = 14.sp,
                        color = Color(0xFF00FF88),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Dialog de selección de dispositivo
    if (showDevicePicker) {
        AlertDialog(
            onDismissRequest = { showDevicePicker = false },
            title = { Text("Seleccionar dispositivo", color = Color(0xFFE0E0E0)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            filteredDevices = if (it.isBlank()) {
                                LatencyDatabase.getAll()
                            } else {
                                LatencyDatabase.getAll().filter { d ->
                                    d.name.contains(it, ignoreCase = true) ||
                                    d.brand.contains(it, ignoreCase = true)
                                }
                            }
                        },
                        placeholder = { Text("Buscar marca o modelo...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { showDevicePicker = false }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00D4FF),
                            unfocusedBorderColor = Color(0xFF2A2A4A)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(filteredDevices) { device ->
                            DeviceListItem(device) {
                                onDeviceSelected(device)
                                showDevicePicker = false
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevicePicker = false }) {
                    Text("Cerrar", color = Color(0xFF00D4FF))
                }
            },
            containerColor = Color(0xFF1A1A2E)
        )
    }
}

@Composable
private fun DeviceListItem(
    device: BluetoothDeviceProfile,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE0E0E0)
                )
                Text(
                    "${device.brand} • ${device.codec} • ~${device.typicalLatencyMs}ms",
                    fontSize = 12.sp,
                    color = Color(0xFF8892B0)
                )
            }
            Text(
                "${device.typicalLatencyMs}ms",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF)
            )
        }
    }
}
