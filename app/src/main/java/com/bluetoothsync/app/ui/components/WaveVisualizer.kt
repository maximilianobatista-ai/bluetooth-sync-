package com.bluetoothsync.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaveVisualizer(
    delayMs: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val delayOffset = (delayMs / 500f) * 100f

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // Canal A (cian, arriba) - referencia
        val pathA = androidx.compose.ui.graphics.Path()
        for (x in 0..width.toInt() step 2) {
            val y = centerY - 18 + sin((x + offset) * 0.05f) * 14
            if (x == 0) pathA.moveTo(x.toFloat(), y)
            else pathA.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = pathA,
            color = Color(0xFF00D4FF).copy(alpha = if (isPlaying) 0.7f else 0.3f),
            style = Stroke(width = 2.5f)
        )

        // Canal B (rojo, abajo, desfasado)
        val pathB = androidx.compose.ui.graphics.Path()
        for (x in 0..width.toInt() step 2) {
            val y = centerY + 18 + sin((x + offset - delayOffset) * 0.05f) * 14
            if (x == 0) pathB.moveTo(x.toFloat(), y)
            else pathB.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = pathB,
            color = Color(0xFFFF6B6B).copy(alpha = if (isPlaying) 0.7f else 0.3f),
            style = Stroke(width = 2.5f)
        )

        // Línea de sincronización cuando hay delay
        if (delayMs > 0) {
            drawLine(
                color = Color(0xFFFFD700).copy(alpha = 0.25f),
                start = Offset(width / 2, 0f),
                end = Offset(width / 2, height),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            )
        }

        // Línea central
        drawLine(
            color = Color(0xFF2A2A4A),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1f
        )
    }
}
