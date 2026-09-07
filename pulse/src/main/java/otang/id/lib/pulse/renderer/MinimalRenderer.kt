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
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig
import otang.id.lib.pulse.PulseGravity

@Composable
fun MinimalRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val rendererState = remember { MinimalPulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f || config.barCount <= 0) return@Canvas

        rendererState.updateData(fft, config.barCount, height, config)
        rendererState.draw(
            drawScope = this,
            width = width,
            height = height,
            baseColor = color,
            smoothing = config.smoothing,
            strokeWidthScale = config.minimalConfig.strokeWidthScale,
            alpha = config.minimalConfig.alpha,
            gravity = config.gravity
        )
    }
}

internal class MinimalPulseState {
    private var currentHeights = FloatArray(0)
    private var targetHeights = FloatArray(0)
    private val smoother = FftSmoother()
    private var pointsX = FloatArray(0)
    private var pointsY = FloatArray(0)
    private val path = Path()

    fun updateData(fft: ByteArray, barCount: Int, viewHeight: Float, config: PulseConfig) {
        if (targetHeights.size != barCount) {
            targetHeights = FloatArray(barCount)
            currentHeights = FloatArray(barCount) { 2f }
            pointsX = FloatArray(barCount)
            pointsY = FloatArray(barCount)
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
        baseColor: Color,
        smoothing: Float,
        strokeWidthScale: Float,
        alpha: Float,
        gravity: PulseGravity
    ) {
        val count = currentHeights.size
        if (count <= 0) return

        val spacing = if (count > 1) width / (count - 1) else width
        path.reset()

        val center = height / 2f

        for (i in 0 until count) {
            val target = targetHeights[i]
            val current = currentHeights[i]

            var h = current + smoothing * (target - current)
            if (h < 2f) h = 2f
            if (h > height) h = height

            currentHeights[i] = h

            pointsX[i] = i * spacing
            pointsY[i] = when (gravity) {
                PulseGravity.Bottom -> height - h
                PulseGravity.Top -> h
                PulseGravity.Center -> center - h / 2f
            }
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

            val strokeColor = baseColor.copy(alpha = alpha)

            drawScope.drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = strokeWidthScale * drawScope.density,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            if (gravity == PulseGravity.Center) {
                // Draw symmetric bottom line
                val bottomPath = Path()
                bottomPath.moveTo(pointsX[0], center + (center - pointsY[0]))
                for (i in 0 until count - 1) {
                    val x1 = pointsX[i]
                    val y1 = center + (center - pointsY[i])
                    val x2 = pointsX[i + 1]
                    val y2 = center + (center - pointsY[i + 1])
                    val cx = (x1 + x2) / 2f
                    val cy = (y1 + y2) / 2f
                    bottomPath.quadraticTo(x1, y1, cx, cy)
                }
                bottomPath.lineTo(pointsX[count - 1], center + (center - pointsY[count - 1]))
                drawScope.drawPath(
                    path = bottomPath,
                    color = strokeColor,
                    style = Stroke(
                        width = strokeWidthScale * drawScope.density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}