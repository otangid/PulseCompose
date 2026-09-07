package otang.id.lib.pulse.renderer

import android.graphics.Canvas
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
import kotlin.math.max

@Composable
fun SolidLineRenderer(
    fft: ByteArray,
    barColor: Color? = null,
    barCount: Int = 32,
    maxMagnitude: Float = 128f,
    heightScale: Float = 1f,
    barGapPx: Float = 2f,
    isRoundedBarsEnabled: Boolean = true,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val rendererState = remember { SolidLinePulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || barCount <= 0) return@Canvas

        rendererState.updateConfig(barCount, barGapPx, isRoundedBarsEnabled)
        rendererState.updateData(fft, height, maxMagnitude, heightScale)
        drawIntoCanvas { canvas ->
            rendererState.draw(canvas.nativeCanvas, width, height, color)
        }
    }
}

internal class SolidLinePulseState {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
    }

    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)

    private var barCount = 0
    private var gap = 0f
    private var isRounded = false
    private val smoothing = 0.2f

    fun updateConfig(count: Int, barGap: Float, rounded: Boolean) {
        barCount = count
        gap = barGap
        isRounded = rounded
    }

    fun updateData(fft: ByteArray, viewHeight: Float, maxMagnitude: Float, heightScale: Float) {
        if (targetHeights.size != barCount) {
            targetHeights = FloatArray(barCount)
            currentHeights = FloatArray(barCount) { 2f }
        }
        otang.id.lib.pulse.FftUtils.calculateMagnitudes(fft, targetHeights)

        for (i in 0 until targetHeights.size) {
            val normalized = targetHeights[i] / maxMagnitude
            targetHeights[i] = (normalized * viewHeight * heightScale).coerceIn(2f, viewHeight)
        }
    }

    fun draw(canvas: Canvas, width: Float, height: Float, color: Color) {
        paint.color = color.toArgb()

        val totalGap = (barCount - 1) * gap
        val barWidth = max(0f, width - totalGap) / barCount
        val fullBarWidth = barWidth + gap

        for (i in 0 until barCount) {
            val target = targetHeights.getOrElse(i) { 2f }
            val current = currentHeights.getOrElse(i) { 2f }

            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > height) h = height

            currentHeights[i] = h

            val left = i * fullBarWidth
            val top = height - h

            if (isRounded) {
                val rect = android.graphics.RectF(left, top, left + barWidth, height)
                val radii = floatArrayOf(32f, 32f, 32f, 32f, 0f, 0f, 0f, 0f)
                val p = android.graphics.Path()
                p.addRoundRect(rect, radii, android.graphics.Path.Direction.CW)
                canvas.drawPath(p, paint)
            } else {
                canvas.drawRect(left, top, left + barWidth, height, paint)
            }
        }
    }
}