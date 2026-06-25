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
import kotlin.math.max
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun NeonRenderer(
    fft: FloatArray,
    barColor: Color? = null,
    barCount: Int = 32,
    barGapPx: Float = 2f,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val neonState = remember { NeonPulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || barCount <= 0) return@Canvas

        neonState.checkAndResize(width, height, barCount, barGapPx)
        neonState.updateData(fft)

        drawIntoCanvas { canvas ->
            neonState.draw(canvas.nativeCanvas, height, color)
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
        color = android.graphics.Color.argb(255, 255, 255, 255) // Inti selalu putih (terang)
    }

    private var pointsX = FloatArray(0)
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)

    private var lastW = 0f
    private var lastH = 0f
    private var lastBarCount = 0
    private var lastGapPx = 0f
    private var lastColorArgb = 0

    private val smoothing = 0.25f

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

    fun updateData(heights: FloatArray) {
        val count = minOf(heights.size, lastBarCount)
        if (targetHeights.size != count) {
            targetHeights = FloatArray(count)
            currentHeights = FloatArray(count) { 2f }
        }
        System.arraycopy(heights, 0, targetHeights, 0, count)
    }

    fun draw(canvas: NativeCanvas, viewHeight: Float, barColor: Color) {
        val count = minOf(lastBarCount, currentHeights.size, pointsX.size)
        if (count <= 0) return

        // Mencegah alokasi ulang objek Color & BlurMaskFilter secara berulang
        val colorArgb = barColor.toArgb()
        if (colorArgb != lastColorArgb) {
            lastColorArgb = colorArgb

            val r = android.graphics.Color.red(colorArgb)
            val g = android.graphics.Color.green(colorArgb)
            val b = android.graphics.Color.blue(colorArgb)

            glowPaint.color = android.graphics.Color.argb(180, r, g, b)
            glowPaint.maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
        }

        for (i in 0 until count) {
            val target = targetHeights.getOrElse(i) { 2f }
            val current = currentHeights.getOrElse(i) { 2f }

            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > viewHeight) h = viewHeight

            currentHeights[i] = h

            val x = pointsX[i]
            val y1 = viewHeight
            val y0 = y1 - h

            // Gambar efek glow di belakang
            canvas.drawLine(x, y1, x, y0, glowPaint)

            // Gambar inti putih solid di depan
            canvas.drawLine(x, y1, x, y0, corePaint)
        }
    }
}