package otang.id.lib.pulse.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig

@Composable
fun WaveFormRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val state = remember(config.barCount) { WaveformPulseState(config.barCount) }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.update(fft, size.height, config)
        state.draw(
            drawScope = this,
            width = size.width,
            height = size.height,
            color = color,
            smoothing = config.smoothing,
            showOutline = config.waveFormConfig.showOutline,
            showFill = config.waveFormConfig.showFill,
            fillAlpha = config.waveFormConfig.fillAlpha,
            strokeWidthScale = config.waveFormConfig.strokeWidthScale
        )
    }
}

internal class WaveformPulseState(private val barCount: Int) {
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    
    private val smoother = FftSmoother()

    private val waveformPath = Path()
    private val fillPath = Path()

    fun update(fft: ByteArray, viewHeight: Float, config: PulseConfig) {
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

    fun draw(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        color: Color,
        smoothing: Float,
        showOutline: Boolean,
        showFill: Boolean,
        fillAlpha: Float,
        strokeWidthScale: Float
    ) {
        val count = currentHeights.size
        if (count < 2) return

        val spacing = width / (count - 1)
        waveformPath.reset()
        fillPath.reset()

        for (i in 0 until count) {
            val target = targetHeights[i]
            val current = currentHeights[i]
            var h = current + smoothing * (target - current)
            h = h.coerceIn(2f, height)
            currentHeights[i] = h

            val x = i * spacing
            val y = height - h

            if (i == 0) {
                waveformPath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (i - 1) * spacing
                val prevY = height - currentHeights[i - 1]
                val midX = (prevX + x) / 2f

                waveformPath.cubicTo(midX, prevY, midX, y, x, y)
                fillPath.cubicTo(midX, prevY, midX, y, x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        if (showFill) {
            drawScope.drawPath(
                path = fillPath,
                color = color.copy(alpha = fillAlpha),
                style = Fill
            )
        }

        if (showOutline) {
            drawScope.drawPath(
                path = waveformPath,
                color = color,
                style = Stroke(
                    width = strokeWidthScale * drawScope.density,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}