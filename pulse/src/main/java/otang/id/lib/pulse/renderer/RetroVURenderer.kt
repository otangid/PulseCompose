package otang.id.lib.pulse.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import kotlin.math.min

@Composable
fun RetroVURenderer(
    fft: FloatArray,
    barColor: Color? = null,
    barCount: Int = 32,
    segmentCount: Int = 16,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val state = remember(barCount, segmentCount) { RetroVUState(barCount, segmentCount) }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.updateData(fft)
        state.draw(this, size.width, size.height, color)
    }
}

internal class RetroVUState(
    private val barCount: Int,
    private val segmentCount: Int
) {
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)

    // Cache posisi segmen untuk menghindari alokasi saat drawing
    private var segmentRects: Array<Array<Pair<Offset, Size>>> = emptyArray()

    private var lastW = 0f
    private var lastH = 0f
    private val smoothing = 0.15f
    private val backgroundColor = Color(40, 100, 100, 100).toArgb()

    fun updateData(heights: FloatArray) {
        if (targetHeights.size != barCount) {
            targetHeights = FloatArray(barCount)
            currentHeights = FloatArray(barCount) { 2f }
        }
        System.arraycopy(heights, 0, targetHeights, 0, min(heights.size, barCount))
    }

    private fun updateLayout(width: Float, height: Float) {
        if (width == lastW && height == lastH) return
        lastW = width
        lastH = height

        val barWidth = width / barCount
        val segHeight = height / segmentCount

        segmentRects = Array(barCount) { i ->
            Array(segmentCount) { j ->
                val x = i * barWidth
                val y = height - ((j + 1) * segHeight)
                Offset(x + 2f, y + 2f) to Size(barWidth - 4f, segHeight - 4f)
            }
        }
    }

    fun draw(drawScope: DrawScope, width: Float, height: Float, barColor: Color) {
        updateLayout(width, height)
        val unlitSegments = (0.5f * segmentCount).toInt()

        for (i in 0 until barCount) {
            val target = targetHeights.getOrElse(i) { 2f }
            val current = currentHeights.getOrElse(i) { 2f }

            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > height) h = height
            currentHeights[i] = h

            val heightPercent = h / height
            val litSegments = (heightPercent * segmentCount).toInt()

            for (seg in 0 until segmentCount) {
                val (offset, size) = segmentRects[i][seg]

                val color = when {
                    seg <= litSegments -> barColor
                    seg < unlitSegments -> Color(backgroundColor)
                    else -> Color.Transparent
                }

                drawScope.drawRect(color = color, topLeft = offset, size = size)
            }
        }
    }
}