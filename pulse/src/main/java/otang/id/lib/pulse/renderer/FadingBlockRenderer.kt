package otang.id.lib.pulse.renderer

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.DashPathEffect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig
import otang.id.lib.pulse.PulseGravity
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun FadingBlockRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val bufferWrapper = remember { FadingPulseBuffer() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width.toInt()
        val height = size.height.toInt()

        if (width <= 0 || height <= 0) return@Canvas

        bufferWrapper.checkAndResize(width, height)

        val totalGap = (config.barCount - 1) * config.fadingBlockConfig.barGapPx
        val barWidth = if (config.barCount > 0) ((width - totalGap) / config.barCount.toFloat()).coerceAtLeast(1f) else 0f
        val fullBarWidth = barWidth + config.fadingBlockConfig.barGapPx

        bufferWrapper.updateFadeAndDrawLines(
            fft = fft,
            barColor = color,
            config = config,
            barWidth = barWidth,
            fullBarWidth = fullBarWidth,
            canvasHeight = height.toFloat()
        )

        bufferWrapper.bitmap?.let { bmp ->
            drawImage(image = bmp.asImageBitmap())
        }
    }
}

internal class FadingPulseBuffer {
    var bitmap: Bitmap? = null
    private var nativeCanvas: NativeCanvas? = null
    private val fadePaint = NativePaint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
    }
    private val linePaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        style = NativePaint.Style.STROKE
        strokeCap = NativePaint.Cap.BUTT
    }
    private var fftPoints = FloatArray(0)
    private var lastW = 0
    private var lastH = 0
    private var lastColorArgb = 0
    private var lastFilled = -1f
    private var lastEmpty = -1f
    private var lastFadeAlpha = -1
    private var magnitudes = FloatArray(0)
    private val smoother = FftSmoother()

    fun checkAndResize(width: Int, height: Int) {
        if (width != lastW || height != lastH) {
            bitmap?.recycle()
            bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            nativeCanvas = NativeCanvas(bitmap!!)
            lastW = width
            lastH = height
        }
    }

    fun updateFadeAndDrawLines(
        fft: ByteArray,
        barColor: Color,
        config: PulseConfig,
        barWidth: Float,
        fullBarWidth: Float,
        canvasHeight: Float
    ) {
        val barCount = config.barCount
        val filledBlockSize = config.fadingBlockConfig.filledBlockSize
        val emptyBlockSize = config.fadingBlockConfig.emptyBlockSize
        val maxMagnitude = config.maxMagnitude
        val heightScale = config.heightScale
        val fadeAlpha = config.fadingBlockConfig.fadeAlpha

        val canvas = nativeCanvas ?: return
        if (magnitudes.size != barCount) {
            magnitudes = FloatArray(barCount)
        }

        var heights = FloatArray(fft.size / 2)
        FftUtils.calculateMagnitudes(fft, heights)

        if (config.useMovingAverage) {
            heights = smoother.smooth(heights, config.movingAverageWindowSize)
        }

        val count = minOf(heights.size, barCount)
        if (count <= 0) return

        if (config.mirror) {
            val halfCount = barCount / 2
            for (i in 0 until halfCount) {
                val mag = if (i < heights.size) heights[i] else 0f
                val normalized = mag / maxMagnitude
                val h = (normalized * canvasHeight * heightScale).coerceIn(2f, canvasHeight)

                val rightIdx = halfCount + i
                val leftIdx = halfCount - 1 - i

                if (rightIdx < barCount) magnitudes[rightIdx] = h
                if (leftIdx >= 0) magnitudes[leftIdx] = h
            }
        } else {
            for (i in 0 until count) {
                val normalized = heights[i] / maxMagnitude
                heights[i] = (normalized * canvasHeight * heightScale).coerceIn(2f, canvasHeight)
            }
        }

        val finalHeights = if (config.mirror) magnitudes else heights

        val colorArgb = barColor.toArgb()
        if (colorArgb != lastColorArgb) {
            linePaint.color = colorArgb
            lastColorArgb = colorArgb
        }

        if (filledBlockSize != lastFilled || emptyBlockSize != lastEmpty) {
            linePaint.pathEffect = DashPathEffect(floatArrayOf(filledBlockSize, emptyBlockSize), 0f)
            lastFilled = filledBlockSize
            lastEmpty = emptyBlockSize
        }

        if (fadeAlpha != lastFadeAlpha) {
            fadePaint.color = Color(255, 255, 255, fadeAlpha).toArgb()
            lastFadeAlpha = fadeAlpha
        }

        linePaint.strokeWidth = barWidth

        val needed = count * 4
        if (fftPoints.size != needed) {
            fftPoints = FloatArray(needed)
        }

        var x = barWidth * 0.5f
        var pi = 0
        val center = canvasHeight / 2f
        for (i in 0 until count) {
            val h = finalHeights[i]
            val (y1, y2) = when (config.gravity) {
                PulseGravity.Bottom -> canvasHeight to (canvasHeight - h)
                PulseGravity.Top -> 0f to h
                PulseGravity.Center -> (center - h / 2f) to (center + h / 2f)
            }
            fftPoints[pi++] = x
            fftPoints[pi++] = y1
            fftPoints[pi++] = x
            fftPoints[pi++] = y2
            x += fullBarWidth
        }

        canvas.drawLines(fftPoints, 0, needed, linePaint)
        canvas.drawPaint(fadePaint)
    }
}