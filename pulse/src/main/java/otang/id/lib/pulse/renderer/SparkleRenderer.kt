package otang.id.lib.pulse.renderer

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.random.Random

@Composable
fun SparkleRenderer(
    fft: FloatArray,
    barColor: Color? = null,
    barCount: Int = 32,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val state = remember(barCount) { SparklePulseState(barCount) }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.update(fft, size.width, size.height)

        drawIntoCanvas { canvas ->
            state.draw(canvas.nativeCanvas, color)
        }
    }
}

internal class SparklePulseState(private val barCount: Int) {
    private val sparklePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
    }

    private val sparkles = Array(200) { Sparkle() }
    private var currentHeights = FloatArray(0)

    private class Sparkle {
        var x = 0f;
        var y = 0f;
        var vx = 0f;
        var vy = 0f
        var size = 0f;
        var life = 0f;
        var alive = false
    }

    fun update(heights: FloatArray, width: Float, height: Float) {
        val spacing = if (barCount > 0) width / barCount else 0f

        // Sinkronisasi data tinggi bar
        if (currentHeights.size != heights.size) currentHeights = heights.copyOf()
        else System.arraycopy(heights, 0, currentHeights, 0, heights.size)

        // Update partikel
        for (p in sparkles) {
            if (p.alive) {
                p.life -= 0.02f
                if (p.life <= 0f) p.alive = false
                p.x += p.vx
                p.y += p.vy
            } else if (Random.nextFloat() < 0.05f) { // Probabilitas spawn
                val idx = Random.nextInt(minOf(heights.size, barCount))
                p.alive = true
                p.x = (idx * spacing) + (spacing / 2)
                p.y = height - (currentHeights.getOrElse(idx) { 2f })
                p.vx = (Random.nextFloat() - 0.5f) * 10f
                p.vy = -Random.nextFloat() * 5f - 2f
                p.size = Random.nextFloat() * 6f + 2f
                p.life = 1f
            }
        }
    }

    fun draw(canvas: android.graphics.Canvas, color: Color) {
        sparklePaint.color = color.toArgb()
        for (p in sparkles) {
            if (p.alive) {
                sparklePaint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.size, sparklePaint)
            }
        }
    }
}