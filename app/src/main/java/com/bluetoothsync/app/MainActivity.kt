package com.bluetoothsync.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluetoothsync.app.data.SyncQuality
import com.bluetoothsync.app.ui.components.BigKnob
import com.bluetoothsync.app.ui.components.DeviceCard
import com.bluetoothsync.app.ui.components.WaveVisualizer

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val denied = permissions.filter { !it.value }.keys
        if (denied.isNotEmpty()) {
            Toast.makeText(
                this,
                "Algunos permisos fueron denegados. La app puede tener funcionalidad limitada.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00D4FF),
                    secondary = Color(0xFFFF6B6B),
                    background = Color(0xFF0F0F1A),
                    surface = Color(0xFF1A1A2E),
                    onBackground = Color(0xFFE0E0E0),
                    onSurface = Color(0xFFE0E0E0),
                    surfaceVariant = Color(0xFF2A2A4A),
                    outline = Color(0xFF2A2A4A)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0F1A)
                ) {
                    SyncApp()
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun SyncApp(viewModel: SyncViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val captureMode by viewModel.captureMode.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val context = LocalContext.current

    // Verificar estado de accesibilidad periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.checkAccessibilityService()
            kotlinx.coroutines.delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header()
        Spacer(modifier = Modifier.height(16.dp))

        DeviceCard(
            state = state,
            onDeviceSelected = { viewModel.setDeviceProfile(it) },
            onDetectLatency = { viewModel.detectLatency() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Alerta de accesibilidad si no está habilitada
        if (!isAccessibilityEnabled && captureMode == CaptureMode.SYSTEM) {
            AccessibilityAlert(viewModel)
            Spacer(modifier = Modifier.height(12.dp))
        }

        SyncVisualizerCard(state, captureMode)
        Spacer(modifier = Modifier.height(16.dp))

        KnobSection(state, viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        CaptureModeSelector(captureMode, viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        PlaybackControls(state, captureMode, viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        QuickPresets(viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        InfoCard()
    }
}

@Composable
fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎧🔗", fontSize = 40.sp)
        Text(
            "Bluetooth Sync",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00D4FF)
        )
        Text(
            "Compensador de latencia por delay",
            fontSize = 13.sp,
            color = Color(0xFF8892B0)
        )
    }
}

@Composable
fun AccessibilityAlert(viewModel: SyncViewModel) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF331111)),
        border = BorderStroke(1.dp, Color(0xFF663333))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0xFFFF6B6B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Servicio de accesibilidad requerido",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
                Text(
                    "Necesario para capturar audio del sistema",
                    fontSize = 12.sp,
                    color = Color(0xFFCC8888)
                )
            }
            Button(
                onClick = {
                    context.startActivity(viewModel.getAccessibilitySettingsIntent())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B)
                )
            ) {
                Text("Activar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SyncVisualizerCard(state: SyncState, captureMode: CaptureMode) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Visualización de sincronización",
                    fontSize = 12.sp,
                    color = Color(0xFF8892B0)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de modo
                    val modeColor = when (captureMode) {
                        CaptureMode.NONE -> Color(0xFF667)
                        CaptureMode.METRONOME -> Color(0xFF00D4FF)
                        CaptureMode.SYSTEM -> Color(0xFF00FF88)
                    }
                    val modeText = when (captureMode) {
                        CaptureMode.NONE -> "Standby"
                        CaptureMode.METRONOME -> "Metrónomo"
                        CaptureMode.SYSTEM -> "Captura activa"
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(modeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        modeText,
                        fontSize = 11.sp,
                        color = modeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0A14))
            ) {
                WaveVisualizer(
                    delayMs = state.currentDelayMs,
                    isPlaying = state.isPlaying
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Canal A (referencia)",
                    fontSize = 11.sp,
                    color = Color(0xFF00D4FF)
                )
                Text(
                    "Canal B (con delay)",
                    fontSize = 11.sp,
                    color = Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
fun KnobSection(state: SyncState, viewModel: SyncViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "AJUSTE DE DELAY",
                fontSize = 12.sp,
                color = Color(0xFF8892B0),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            BigKnob(
                currentDelay = state.currentDelayMs,
                onDelayChange = { viewModel.setDelay(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = state.currentDelayMs.toFloat(),
                onValueChange = { viewModel.setDelay(it.toInt()) },
                valueRange = 0f..500f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00D4FF),
                    activeTrackColor = Color(0xFF00D4FF),
                    inactiveTrackColor = Color(0xFF2A2A4A)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(
                    onClick = { viewModel.adjustDelay(-10) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2A2A4A)
                    )
                ) { Text("-10ms", color = Color(0xFFFF6B6B)) }

                FilledTonalButton(
                    onClick = { viewModel.adjustDelay(-1) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2A2A4A)
                    )
                ) { Text("-1ms", color = Color(0xFFFF6B6B)) }

                FilledTonalButton(
                    onClick = { viewModel.adjustDelay(+1) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2A2A4A)
                    )
                ) { Text("+1ms", color = Color(0xFF00FF88)) }

                FilledTonalButton(
                    onClick = { viewModel.adjustDelay(+10) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2A2A4A)
                    )
                ) { Text("+10ms", color = Color(0xFF00FF88)) }
            }
        }
    }
}

@Composable
fun CaptureModeSelector(
    captureMode: CaptureMode,
    viewModel: SyncViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "MODO DE CAPTURA",
                fontSize = 12.sp,
                color = Color(0xFF8892B0),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaptureModeButton(
                    icon = Icons.Rounded.MusicNote,
                    label = "Metrónomo",
                    description = "Prueba de calibración",
                    isSelected = captureMode == CaptureMode.METRONOME,
                    color = Color(0xFF00D4FF),
                    onClick = { viewModel.toggleMetronome() }
                )

                CaptureModeButton(
                    icon = Icons.Rounded.Smartphone,
                    label = "Sistema",
                    description = "YouTube, Spotify, etc.",
                    isSelected = captureMode == CaptureMode.SYSTEM,
                    color = Color(0xFF00FF88),
                    onClick = {
                        if (captureMode == CaptureMode.SYSTEM) {
                            viewModel.stopSystemCapture()
                        } else {
                            viewModel.startSystemCapture()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CaptureModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF0F0F1A),
            contentColor = if (isSelected) color else Color(0xFF8892B0)
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) color.copy(alpha = 0.5f) else Color(0xFF2A2A4A)
        ),
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(description, fontSize = 10.sp, color = Color(0xFF667))
        }
    }
}

@Composable
fun PlaybackControls(
    state: SyncState,
    captureMode: CaptureMode,
    viewModel: SyncViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {
                when (captureMode) {
                    CaptureMode.METRONOME -> viewModel.toggleMetronome()
                    CaptureMode.SYSTEM -> viewModel.stopSystemCapture()
                    CaptureMode.NONE -> viewModel.toggleMetronome()
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isPlaying) Color(0xFFFF6B6B) else Color(0xFF00D4FF)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (state.isPlaying) "DETENER" else "REPRODUCIR",
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { viewModel.playClickTest() },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2A2A4A)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("TEST CLICK")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple(0, "0 ms", Color(0xFF00D4FF)),
            Triple(100, "100 ms", Color(0xFFFFC107)),
            Triple(200, "200 ms", Color(0xFFFF6B6B)),
            Triple(300, "300 ms", Color(0xFFE91E63))
        ).forEach { (ms, label, color) ->
            Button(
                onClick = { viewModel.setDelay(ms) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = color.copy(alpha = 0.15f),
                    contentColor = color
                ),
                border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuickPresets(viewModel: SyncViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "PRESETS POR MARCA",
                fontSize = 12.sp,
                color = Color(0xFF8892B0),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val presets = listOf(
                Triple("40 ms", "aptX Low Latency", Color(0xFF00FF88)),
                Triple("120 ms", "Samsung SSC", Color(0xFF00D4FF)),
                Triple("150 ms", "AAC (Apple)", Color(0xFFFFC107)),
                Triple("200 ms", "SBC genérico", Color(0xFFFF6B6B)),
                Triple("250 ms", "LDAC / Bose", Color(0xFFE91E63)),
                Triple("300 ms", "Bluetooth viejo", Color(0xFF9C27B0))
            )

            presets.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (delay, label, color) ->
                        val ms = delay.replace(" ms", "").toInt()
                        Button(
                            onClick = { viewModel.setDelay(ms) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F0F1A),
                                contentColor = Color(0xFFE0E0E0)
                            ),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    delay,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    fontSize = 14.sp
                                )
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    color = Color(0xFF8892B0)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2A2A4A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "💡 Cómo usar",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "1. Conecta auriculares Bluetooth + altavoz cableado
" +
                "2. Selecciona modo 'Metrónomo' para calibrar a oído
" +
                "3. O selecciona modo 'Sistema' para capturar YouTube/Spotify
" +
                "4. Gira la perilla hasta que ambos suenen al unísono

" +
                "💡 En modo Sistema, primero activa el servicio de accesibilidad.",
                fontSize = 12.sp,
                color = Color(0xFF8892B0),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "📊 Compatibilidad por marca",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "• Apple: AirPods (AAC) ~130-200ms
" +
                "• Samsung: Galaxy Buds (SSC) ~120-170ms
" +
                "• Sony: WH/WF series (LDAC) ~180-260ms
" +
                "• Bose: QC series (SBC) ~240-280ms
" +
                "• Gaming: aptX Low Latency ~35-60ms",
                fontSize = 12.sp,
                color = Color(0xFF8892B0),
                lineHeight = 20.sp
            )
        }
    }
}
