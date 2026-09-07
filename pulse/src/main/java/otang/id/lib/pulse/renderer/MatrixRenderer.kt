package otang.id.lib.pulse.renderer

import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.max
import kotlin.random.Random
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun MatrixRenderer(
    fft: ByteArray,
    barCount: Int = 32,
    maxMagnitude: Float = 128f,
    heightScale: Float = 1f,
    barGapPx: Float = 2f,
    modifier: Modifier
) {
    val matrixState = remember { MatrixPulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0 || barCount <= 0) return@Canvas

        matrixState.checkAndResize(width, height, barCount, barGapPx)

        matrixState.updateData(fft, height, maxMagnitude, heightScale)

        drawIntoCanvas { canvas ->
            matrixState.draw(canvas.nativeCanvas, height)
        }
    }
}

internal class MatrixPulseState {
    private val brightGreen = android.graphics.Color.argb(255, 0, 255, 65)
    private val mediumGreen = android.graphics.Color.argb(200, 0, 220, 55)
    private val darkGreen = android.graphics.Color.argb(120, 0, 160, 40)

    private val glowPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        style = NativePaint.Style.FILL
        maskFilter = BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL)
    }

    private val brightTextPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        color = brightGreen
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.MONOSPACE
        style = NativePaint.Style.FILL
    }

    private val mediumTextPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        color = mediumGreen
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.MONOSPACE
        style = NativePaint.Style.FILL
    }

    private val darkTextPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        color = darkGreen
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.MONOSPACE
        style = NativePaint.Style.FILL
    }

    private val textGlowPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(150, 0, 255, 65)
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.MONOSPACE
        style = NativePaint.Style.FILL
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private var barColumns: Array<MatrixColumn> = emptyArray()
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)

    private var lastW = 0f
    private var lastH = 0f
    private var lastBarCount = 0
    private var lastGapPx = 0f

    private var columnWidth = 0f
    private var charSize = 0f
    private var maxCharsPerColumn = 0

    private val smoothing = 0.22f
    private val numbers = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")

    private var changeCounter = 0
    private val changeInterval = 3

    fun checkAndResize(width: Float, height: Float, barCount: Int, gapPx: Float) {
        if (width != lastW || height != lastH || barCount != lastBarCount || gapPx != lastGapPx) {
            lastW = width
            lastH = height
            lastBarCount = barCount
            lastGapPx = gapPx

            val totalGap = (barCount - 1) * gapPx
            columnWidth = if (barCount > 0) max(1f, (width - totalGap) / barCount) else 0f

            charSize = (columnWidth * 0.7f).coerceIn(16f, 36f)

            brightTextPaint.textSize = charSize
            mediumTextPaint.textSize = charSize
            darkTextPaint.textSize = charSize
            textGlowPaint.textSize = charSize

            maxCharsPerColumn = if (charSize > 0) (height / (charSize * 1.1f)).toInt().coerceAtLeast(1) else 1

            if (barColumns.size != barCount) {
                barColumns = Array(barCount) { MatrixColumn(maxCharsPerColumn) }
                currentHeights = FloatArray(barCount) { 2f }
                targetHeights = FloatArray(barCount) { 2f }
            } else {
                barColumns.forEach { it.ensureCapacity(maxCharsPerColumn) }
            }
        }
    }

    fun updateData(fft: ByteArray, viewHeight: Float, maxMagnitude: Float, heightScale: Float) {
        if (targetHeights.size != lastBarCount) {
            targetHeights = FloatArray(lastBarCount)
            currentHeights = FloatArray(lastBarCount) { 2f }
        }
        otang.id.lib.pulse.FftUtils.calculateMagnitudes(fft, targetHeights)

        for (i in 0 until targetHeights.size) {
            val normalized = targetHeights[i] / maxMagnitude
            targetHeights[i] = (normalized * viewHeight * heightScale).coerceIn(2f, viewHeight)
        }
    }

    fun draw(canvas: NativeCanvas, viewHeight: Float) {
        val count = minOf(lastBarCount, barColumns.size, currentHeights.size, targetHeights.size)

        for (i in 0 until count) {
            val target = targetHeights[i]
            val current = currentHeights[i]
            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > viewHeight) h = viewHeight
            currentHeights[i] = h
        }

        changeCounter++
        val shouldChange = changeCounter >= changeInterval
        if (shouldChange) changeCounter = 0

        val fullBarWidth = columnWidth + lastGapPx

        for (i in 0 until count) {
            val column = barColumns[i]
            val height = currentHeights[i]

            if (height < 10f) continue

            val x = i * fullBarWidth + columnWidth * 0.5f

            if (shouldChange && Random.nextFloat() < 0.4f) {
                column.regenerateRandomChars()
            }

            val charSpacingWithGap = charSize * 1.15f
            val numChars = (height / charSpacingWithGap).toInt().coerceIn(1, maxCharsPerColumn)

            column.ensureCapacity(numChars)

            val glowWidth = columnWidth * 0.85f
            val heightRatio = height / viewHeight
            val glowAlpha = (160 * heightRatio).toInt().coerceIn(0, 160)

            glowPaint.color = android.graphics.Color.argb(glowAlpha, 0, 200, 50)
            canvas.drawRect(
                x - glowWidth / 2f,
                viewHeight - height,
                x + glowWidth / 2f,
                viewHeight,
                glowPaint
            )

            for (j in 0 until numChars) {
                val y = viewHeight - (j * charSpacingWithGap) - charSize * 0.25f
                val char = column.chars[j % column.chars.size]

                val fadeRatio = j.toFloat() / numChars.coerceAtLeast(1)

                val textPaint = when {
                    fadeRatio < 0.25f -> brightTextPaint
                    fadeRatio < 0.6f -> mediumTextPaint
                    else -> darkTextPaint
                }

                if (fadeRatio < 0.4f) {
                    val glowStrength = ((1f - fadeRatio * 2.5f) * 200).toInt().coerceIn(0, 200)
                    textGlowPaint.color = android.graphics.Color.argb(glowStrength, 0, 255, 65)
                    canvas.drawText(char, x, y, textGlowPaint)
                }

                canvas.drawText(char, x, y, textPaint)
            }
        }
    }

    private inner class MatrixColumn(initialCapacity: Int) {
        var chars: Array<String> = Array(initialCapacity) { numbers.random() }

        fun ensureCapacity(needed: Int) {
            if (chars.size < needed) {
                val newChars = Array(needed) { idx ->
                    if (idx < chars.size) chars[idx] else numbers.random()
                }
                chars = newChars
            }
        }

        fun regenerateRandomChars() {
            val numToChange = Random.nextInt(1, 4).coerceAtMost(chars.size)
            repeat(numToChange) {
                val idx = Random.nextInt(chars.size)
                chars[idx] = numbers.random()
            }
        }
    }
}