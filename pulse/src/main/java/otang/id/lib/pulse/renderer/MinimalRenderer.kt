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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun MinimalRenderer(
    fft: ByteArray,
    barColor: Color? = null,
    barCount: Int = 32,
    maxMagnitude: Float = 128f,
    heightScale: Float = 1f,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val rendererState = remember { MinimalPulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || barCount <= 0) return@Canvas

        rendererState.updateData(fft, barCount, height, maxMagnitude, heightScale)
        rendererState.draw(this, width, height, color)
    }
}

internal class MinimalPulseState {
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    private var pointsX = FloatArray(0)
    private var pointsY = FloatArray(0)

    private val smoothing = 0.3f
    private val path = Path()

    fun updateData(fft: ByteArray, barCount: Int, viewHeight: Float, maxMagnitude: Float, heightScale: Float) {
        if (targetHeights.size != barCount) {
            targetHeights = FloatArray(barCount)
            currentHeights = FloatArray(barCount) { 2f }
            pointsX = FloatArray(barCount)
            pointsY = FloatArray(barCount)
        }
        otang.id.lib.pulse.FftUtils.calculateMagnitudes(fft, targetHeights)

        for (i in 0 until targetHeights.size) {
            val normalized = targetHeights[i] / maxMagnitude
            targetHeights[i] = (normalized * viewHeight * heightScale).coerceIn(2f, viewHeight)
        }
    }

    fun draw(drawScope: DrawScope, width: Float, height: Float, baseColor: Color) {
        val count = currentHeights.size
        if (count <= 0) return

        val spacing = if (count > 1) width / (count - 1) else width
        path.reset()

        for (i in 0 until count) {
            val target = targetHeights[i]
            val current = currentHeights[i]

            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > height) h = height

            currentHeights[i] = h

            pointsX[i] = i * spacing
            pointsY[i] = height - h
        }

        if (count >= 2) {
            path.moveTo(pointsX[0], pointsY[0])

            for (i in 0 until count - 1) {
                val x1 = pointsX[i]
                val y1 = pointsY[i]
                val x2 = pointsX[i + 1]
                val y2 = pointsY[i + 1]

                val cx = (x1 + x2) / 2f
                val cy = (y1 + y2) / 2f

                path.quadraticTo(x1, y1, cx, cy)
            }

            path.lineTo(pointsX[count - 1], pointsY[count - 1])

            val strokeColor = baseColor.copy(alpha = 0.7f)

            drawScope.drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = 2.5f * drawScope.density,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}