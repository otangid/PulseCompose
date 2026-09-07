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
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig
import otang.id.lib.pulse.PulseGravity
import kotlin.math.max
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun NeonRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val neonState = remember { NeonPulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || config.barCount <= 0) return@Canvas

        neonState.checkAndResize(width, height, config.barCount, gapPx = config.neonConfig.barGapPx)
        neonState.updateData(fft, height, config)

        drawIntoCanvas { canvas ->
            neonState.draw(
                canvas = canvas.nativeCanvas,
                viewHeight = height,
                barColor = color,
                smoothing = config.smoothing,
                glowAlpha = config.neonConfig.glowAlpha,
                glowRadius = config.neonConfig.glowRadius,
                gravity = config.gravity
            )
        }
    }
}

internal class NeonPulseState {
    private val glowPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        style = NativePaint.Style.STROKE
        strokeCap = NativePaint.Cap.ROUND
    }
    private val corePaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        style = NativePaint.Style.STROKE
        strokeCap = NativePaint.Cap.ROUND
        color = Color(255, 255, 255, 255).toArgb()
    }
    private var pointsX = FloatArray(0)
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    private val smoother = otang.id.lib.pulse.FftSmoother()
    private var lastW = 0f
    private var lastH = 0f
    private var lastBarCount = 0
    private var lastGapPx = 0f
    private var lastColorArgb = 0
    private var lastGlowRadius = 0f

    fun checkAndResize(width: Float, height: Float, barCount: Int, gapPx: Float) {
        if (width != lastW || height != lastH || barCount != lastBarCount || gapPx != lastGapPx) {
            lastW = width
            lastH = height
            lastBarCount = barCount
            lastGapPx = gapPx

            val totalGap = (barCount - 1) * gapPx
            val barWidth = if (barCount > 0) max(1f, (width - totalGap) / barCount) else 0f
            val fullBarWidth = barWidth + gapPx

            val coreWidth = max(2f, barWidth * 0.3f)
            val glowWidth = max(4f, barWidth * 0.8f)

            corePaint.strokeWidth = coreWidth
            glowPaint.strokeWidth = glowWidth

            if (pointsX.size != barCount) {
                pointsX = FloatArray(barCount)
                currentHeights = FloatArray(barCount) { 2f }
                targetHeights = FloatArray(barCount) { 2f }
            }

            for (i in 0 until barCount) {
                pointsX[i] = i * fullBarWidth + barWidth * 0.5f
            }
        }
    }

    fun updateData(fft: ByteArray, viewHeight: Float, config: PulseConfig) {
        if (targetHeights.size != lastBarCount) {
            targetHeights = FloatArray(lastBarCount)
            currentHeights = FloatArray(lastBarCount) { 2f }
        }
        
        var magnitudes = FloatArray(fft.size / 2)
        FftUtils.calculateMagnitudes(fft, magnitudes)
        
        if (config.useMovingAverage) {
            magnitudes = smoother.smooth(magnitudes, config.movingAverageWindowSize)
        }

        if (config.mirror) {
            val halfCount = lastBarCount / 2
            for (i in 0 until halfCount) {
                val mag = if (i < magnitudes.size) magnitudes[i] else 0f
                val normalized = mag / config.maxMagnitude
                val h = (normalized * viewHeight * config.heightScale).coerceIn(2f, viewHeight)

                val rightIdx = halfCount + i
                val leftIdx = halfCount - 1 - i

                if (rightIdx < lastBarCount) targetHeights[rightIdx] = h
                if (leftIdx >= 0) targetHeights[leftIdx] = h
            }
        } else {
            for ((i, element) in magnitudes.withIndex()) {
                if (i >= lastBarCount) break
                val normalized = element / config.maxMagnitude
                targetHeights[i] = (normalized * viewHeight * config.heightScale).coerceIn(2f, viewHeight)
            }
        }
    }

    fun draw(
        canvas: NativeCanvas,
        viewHeight: Float,
        barColor: Color,
        smoothing: Float,
        glowAlpha: Int,
        glowRadius: Float,
        gravity: PulseGravity
    ) {
        val count = minOf(lastBarCount, currentHeights.size, pointsX.size)
        if (count <= 0) return

        val colorArgb = barColor.toArgb()
        if (colorArgb != lastColorArgb || glowRadius != lastGlowRadius) {
            lastColorArgb = colorArgb
            lastGlowRadius = glowRadius

            val r = barColor.red.toInt()
            val g = barColor.green.toInt()
            val b = barColor.blue.toInt()

            glowPaint.color = Color(r, g, b, glowAlpha).toArgb()
            glowPaint.maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
        }

        val center = viewHeight / 2f

        for (i in 0 until count) {
            val target = targetHeights.getOrElse(i) { 2f }
            val current = currentHeights.getOrElse(i) { 2f }

            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > viewHeight) h = viewHeight

            currentHeights[i] = h

            val x = pointsX[i]
            val (y1, y2) = when (gravity) {
                PulseGravity.Bottom -> viewHeight to (viewHeight - h)
                PulseGravity.Top -> 0f to h
                PulseGravity.Center -> (center - h / 2f) to (center + h / 2f)
            }

            canvas.drawLine(x, y1, x, y2, glowPaint)
            canvas.drawLine(x, y1, x, y2, corePaint)
        }
    }
}