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
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun FadingBlockRenderer(
    fft: FloatArray,
    barColor: Color? = null,
    barCount: Int = 32,
    barGapPx: Float = 2f,
    filledBlockSize: Float = 0f,
    emptyBlockSize: Float = 0f,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val bufferWrapper = remember { FadingPulseBuffer() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width.toInt()
        val height = size.height.toInt()

        if (width <= 0 || height <= 0) return@Canvas

        bufferWrapper.checkAndResize(width, height)

        val totalGap = (barCount - 1) * barGapPx
        val barWidth = if (barCount > 0) ((width - totalGap) / barCount.toFloat()).coerceAtLeast(1f) else 0f
        val fullBarWidth = barWidth + barGapPx

        bufferWrapper.updateFadeAndDrawLines(
            fft = fft,
            barColor = color,
            barCount = barCount,
            barWidth = barWidth,
            fullBarWidth = fullBarWidth,
            filledBlockSize = filledBlockSize,
            emptyBlockSize = emptyBlockSize,
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
        color = android.graphics.Color.argb(200, 255, 255, 255)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
    }

    private val linePaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        style = NativePaint.Style.STROKE
        strokeCap = NativePaint.Cap.BUTT
    }

    private var fftPoints = FloatArray(0)
    private var lastW = 0
    private var lastH = 0

    // Caching state untuk optimasi 60fps
    private var lastColorArgb = 0
    private var lastFilled = -1f
    private var lastEmpty = -1f

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
        fft: FloatArray,
        barColor: Color,
        barCount: Int,
        barWidth: Float,
        fullBarWidth: Float,
        filledBlockSize: Float,
        emptyBlockSize: Float,
        canvasHeight: Float
    ) {
        val canvas = nativeCanvas ?: return
        val count = minOf(fft.size, barCount)
        if (count <= 0) return

        // 1. Optimasi properti Paint (hanya set ulang jika nilainya berubah)
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

        linePaint.strokeWidth = barWidth

        // 2. Alokasi ulang array HANYA jika ukuran berubah
        val needed = count * 4
        if (fftPoints.size != needed) {
            fftPoints = FloatArray(needed)
        }

        // 3. Kalkulasi titik garis
        var x = barWidth * 0.5f
        var pi = 0
        for (i in 0 until count) {
            fftPoints[pi++] = x
            fftPoints[pi++] = canvasHeight
            fftPoints[pi++] = x
            fftPoints[pi++] = canvasHeight - fft[i]
            x += fullBarWidth
        }

        // 4. Draw ke off-screen
        canvas.drawLines(fftPoints, 0, needed, linePaint)
        canvas.drawPaint(fadePaint)
    }
}