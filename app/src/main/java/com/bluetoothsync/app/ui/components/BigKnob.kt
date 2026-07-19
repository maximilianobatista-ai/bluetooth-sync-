package com.bluetoothsync.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun BigKnob(
    currentDelay: Int,
    onDelayChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val targetRotation = remember(currentDelay) {
        -135f + (currentDelay / 500f) * 270f
    }

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "knobRotation"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pos = change.position
                    val angle = atan2(
                        pos.y - center.y,
                        pos.x - center.x
                    ) * (180f / PI.toFloat()) + 90f

                    val clampedAngle = when {
                        angle < -180 -> angle + 360
                        angle > 180 -> angle - 360
                        else -> angle
                    }.coerceIn(-135f, 135f)

                    val newDelay = ((clampedAngle + 135f) / 270f * 500).toInt()
                    onDelayChange(newDelay.coerceIn(0, 500))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 - 15

            // Track de fondo (gris oscuro)
            drawArc(
                color = Color(0xFF2A2A4A),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Track activo (gradiente)
            val progress = currentDelay / 500f
            val sweep = 270f * progress

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF00D4FF),
                        Color(0xFF00FF88),
                        Color(0xFFFFC107),
                        Color(0xFFFF6B6B),
                        Color(0xFFE91E63)
                    ),
                    center = Offset(centerX, centerY)
                ),
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Cuerpo de la perilla
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2A2A4A), Color(0xFF0F0F1A)),
                    center = Offset(centerX, centerY),
                    radius = radius - 25
                ),
                radius = radius - 25,
                center = Offset(centerX, centerY)
            )

            // Brillo superficial
            drawCircle(
                color = Color(0xFF3A3A5A).copy(alpha = 0.3f),
                radius = radius - 35,
                center = Offset(centerX - 10, centerY - 10)
            )

            // Indicador de posición
            val indicatorAngle = Math.toRadians((animatedRotation - 90).toDouble())
            val indicatorLength = radius - 35
            drawLine(
                color = Color(0xFF00D4FF),
                start = Offset(centerX, centerY),
                end = Offset(
                    centerX + cos(indicatorAngle).toFloat() * indicatorLength,
                    centerY + sin(indicatorAngle).toFloat() * indicatorLength
                ),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Centro
            drawCircle(
                color = Color(0xFF00D4FF),
                radius = 8f,
                center = Offset(centerX, centerY)
            )

            // Sombra del centro
            drawCircle(
                color = Color(0xFF00A0CC),
                radius = 4f,
                center = Offset(centerX + 1, centerY + 1)
            )
        }

        // Valor numérico superpuesto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 4.dp)
        ) {
            Text(
                text = "$currentDelay",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4FF)
            )
            Text(
                text = "ms",
                fontSize = 13.sp,
                color = Color(0xFF667)
            )
        }
    }
}
