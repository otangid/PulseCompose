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
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig

@Composable
fun RetroVURenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val state = remember(config.barCount, config.retroVUConfig.segmentCount) { 
        RetroVUState(config.barCount, config.retroVUConfig.segmentCount) 
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.updateData(fft, size.height, config)
        state.draw(
            drawScope = this,
            width = size.width,
            height = size.height,
            barColor = color,
            smoothing = config.smoothing,
            segmentGapPx = config.retroVUConfig.segmentGapPx
        )
    }
}

internal class RetroVUState(
    private val barCount: Int,
    private val segmentCount: Int
) {
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    private val smoother = FftSmoother()
    private var segmentRects: Array<Array<Pair<Offset, Size>>> = emptyArray()
    private var lastW = 0f
    private var lastH = 0f
    private var lastSegGap = 0f

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

    private fun updateLayout(width: Float, height: Float, segmentGapPx: Float) {
        if (width == lastW && height == lastH && segmentGapPx == lastSegGap) return
        lastW = width
        lastH = height
        lastSegGap = segmentGapPx

        val barWidth = width / barCount
        val segHeight = height / segmentCount

        segmentRects = Array(barCount) { i ->
            Array(segmentCount) { j ->
                val x = i * barWidth
                val y = height - ((j + 1) * segHeight)
                Offset(x + segmentGapPx / 2f, y + segmentGapPx / 2f) to Size(barWidth - segmentGapPx, segHeight - segmentGapPx)
            }
        }
    }

    fun draw(drawScope: DrawScope, width: Float, height: Float, barColor: Color, smoothing: Float, segmentGapPx: Float) {
        updateLayout(width, height, segmentGapPx)

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
                    else -> Color.Transparent
                }

                drawScope.drawRect(color = color, topLeft = offset, size = size)
            }
        }
    }
}