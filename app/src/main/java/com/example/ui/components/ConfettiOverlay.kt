package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.domain.GraphicsQuality
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val xFrac: Float,
    val yOffset: Float,
    val speed: Float,
    val sizePx: Float,
    val color: Color,
    val swayAmp: Float,
    val spinSpeed: Float
)

@Composable
fun ConfettiOverlay(
    graphicsQuality: GraphicsQuality,
    modifier: Modifier = Modifier
) {
    val count = when (graphicsQuality) {
        GraphicsQuality.HIGH -> 56
        GraphicsQuality.BALANCED -> 32
        GraphicsQuality.BATTERY_SAVER -> 14
    }

    val particles = remember(count) {
        val palette = listOf(
            Color(0xFF1E88E5),
            Color(0xFFFFB300),
            Color(0xFF16A34A),
            Color(0xFFE53935),
            Color(0xFF0288D1),
            Color(0xFF8B5CF6)
        )
        val rng = Random(42)
        List(count) {
            ConfettiParticle(
                xFrac = rng.nextFloat(),
                yOffset = rng.nextFloat(),
                speed = 0.55f + rng.nextFloat() * 0.9f,
                sizePx = 10f + rng.nextFloat() * 14f,
                color = palette[rng.nextInt(palette.size)],
                swayAmp = 14f + rng.nextFloat() * 24f,
                spinSpeed = 120f + rng.nextFloat() * 240f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in particles) {
            val yFrac = (p.yOffset + time * p.speed) % 1.15f - 0.08f
            val x = p.xFrac * size.width + sin((time * 6.28f + p.yOffset * 10f).toDouble()).toFloat() * p.swayAmp
            val y = yFrac * size.height
            val angle = time * p.spinSpeed + p.yOffset * 360f

            rotate(degrees = angle, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(x - p.sizePx / 2f, y - p.sizePx * 0.3f),
                    size = Size(p.sizePx, p.sizePx * 0.6f)
                )
            }
        }
    }
}
