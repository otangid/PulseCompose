package otang.id.lib.pulse.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig
import kotlin.math.max

@Composable
fun SolidLineRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val rendererState = remember { SolidLinePulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || config.barCount <= 0) return@Canvas

        rendererState.updateConfig(config.barCount, config.solidLineConfig.barGapPx, config.solidLineConfig.isRoundedBarsEnabled)
        rendererState.updateData(fft, height, config)
        drawIntoCanvas { canvas ->
            rendererState.draw(
                canvas = canvas.nativeCanvas,
                width = width,
                height = height,
                color = color,
                smoothing = config.smoothing,
                cornerRadius = config.solidLineConfig.cornerRadius
            )
        }
    }
}

internal class SolidLinePulseState {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    private var barCount = 0
    private var gap = 0f
    private var isRounded = false
    
    private val smoother = FftSmoother()

    fun updateConfig(count: Int, barGap: Float, rounded: Boolean) {
        barCount = count
        gap = barGap
        isRounded = rounded
    }

    fun updateData(fft: ByteArray, viewHeight: Float, config: PulseConfig) {
        if (targetHeights.size != barCount) {
            targetHeights = FloatArray(barCount)
            currentHeights = FloatArray(barCount) { 2f }
        }
        
        var magnitudes = FloatArray(fft.size / 2)
        FftUtils.calculateMagnitudes(fft, magnitudes)
        
        if (config.useMovingAverage) {
            magnitudes = smoother.smooth(magnitudes, config.movingAverageWindowSize)
        }

        for ((i, element) in magnitudes.withIndex()) {
            if (i >= barCount) break
            val normalized = element / config.maxMagnitude
            targetHeights[i] = (normalized * viewHeight * config.heightScale).coerceIn(2f, viewHeight)
        }
    }

    fun draw(canvas: Canvas, width: Float, height: Float, color: Color, smoothing: Float, cornerRadius: Float) {
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
                val rect = RectF(left, top, left + barWidth, height)
                val radii = floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f, 0f, 0f)
                val p = Path()
                p.addRoundRect(rect, radii, Path.Direction.CW)
                canvas.drawPath(p, paint)
            } else {
                canvas.drawRect(left, top, left + barWidth, height, paint)
            }
        }
    }
}