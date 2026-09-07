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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun WaveFormRenderer(
    fft: ByteArray,
    barColor: Color? = null,
    barCount: Int = 32,
    maxMagnitude: Float = 128f,
    heightScale: Float = 1f,
    showOutline: Boolean = true,
    showFill: Boolean = true,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val state = remember(barCount) { WaveformPulseState(barCount) }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.update(fft, size.height, maxMagnitude, heightScale)
        state.draw(
            drawScope = this,
            width = size.width,
            height = size.height,
            color = color,
            showOutline = showOutline,
            showFill = showFill
        )
    }
}

internal class WaveformPulseState(private val barCount: Int) {
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    private val smoothing = 0.2f

    private val waveformPath = Path()
    private val fillPath = Path()

    fun update(fft: ByteArray, viewHeight: Float, maxMagnitude: Float, heightScale: Float) {
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

    fun draw(
        drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
        width: Float,
        height: Float,
        color: Color,
        showOutline: Boolean,
        showFill: Boolean
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
                color = color.copy(alpha = 0.3f),
                style = Fill
            )
        }

        if (showOutline) {
            drawScope.drawPath(
                path = waveformPath,
                color = color,
                style = Stroke(
                    width = 3f * drawScope.density,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}