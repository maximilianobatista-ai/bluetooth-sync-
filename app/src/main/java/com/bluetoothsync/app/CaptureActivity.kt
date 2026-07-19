package com.bluetoothsync.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetoothsync.app.service.BluetoothSyncAccessibilityService
import com.bluetoothsync.app.ui.components.BigKnob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CaptureActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CaptureActivity"
        private const val REQUEST_MEDIA_PROJECTION = 1001

        fun createIntent(context: Context): Intent {
            return Intent(context, CaptureActivity::class.java)
        }
    }

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var currentDelayMs: Int = 0

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val mediaProjection = mediaProjectionManager?.getMediaProjection(
                result.resultCode, result.data!!
            )

            mediaProjection?.let { projection ->
                BluetoothSyncAccessibilityService.instance?.startCaptureWithMediaProjection(
                    projection, currentDelayMs
                )
                Toast.makeText(this, "Captura iniciada: ${currentDelayMs}ms delay", Toast.LENGTH_SHORT).show()
                finish() // Cerrar actividad, el servicio sigue corriendo
            }
        } else {
            Toast.makeText(this, "Permiso de captura denegado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        currentDelayMs = intent.getIntExtra("delay_ms", 0)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00D4FF),
                    background = Color(0xFF0F0F1A),
                    surface = Color(0xFF1A1A2E),
                    onBackground = Color(0xFFE0E0E0)
                )
            ) {
                CaptureScreen(
                    initialDelay = currentDelayMs,
                    onDelayChange = { currentDelayMs = it },
                    onStartCapture = { requestCapture() },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun requestCapture() {
        val captureIntent = mediaProjectionManager?.createScreenCaptureIntent()
        captureIntent?.let {
            captureLauncher.launch(it)
        }
    }
}

@Composable
fun CaptureScreen(
    initialDelay: Int,
    onDelayChange: (Int) -> Unit,
    onStartCapture: () -> Unit,
    onCancel: () -> Unit
) {
    var delayMs by remember { mutableIntStateOf(initialDelay) }
    var isCapturing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎧🔗",
                fontSize = 40.sp
            )
            Text(
                "Captura de Audio",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF)
            )
            Text(
                "Aplica delay a cualquier app",
                fontSize = 13.sp,
                color = Color(0xFF8892B0)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Ajusta el delay antes de capturar:",
                fontSize = 14.sp,
                color = Color(0xFF8892B0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            BigKnob(
                currentDelay = delayMs,
                onDelayChange = { 
                    delayMs = it
                    onDelayChange(it)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "$delayMs ms",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                border = BorderStroke(1.dp, Color(0xFF2A2A4A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚠️ Importante",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Al iniciar la captura, Android te pedirá permiso para "grabar la pantalla". " +
                        "Esto es necesario para capturar el audio del sistema.

" +
                        "La app NO graba video, solo audio.",
                        fontSize = 12.sp,
                        color = Color(0xFF8892B0),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A4A)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        isCapturing = true
                        onStartCapture()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D4FF),
                        contentColor = Color(0xFF0F0F1A)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF0F0F1A),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("▶ Iniciar Captura", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
