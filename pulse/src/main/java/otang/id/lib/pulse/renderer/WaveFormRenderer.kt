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
import otang.id.lib.pulse.PulseGravity

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
            gravity = config.gravity,
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
        gravity: PulseGravity,
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

        val center = height / 2f

        for (i in 0 until count) {
            val target = targetHeights[i]
            val current = currentHeights[i]
            var h = current + smoothing * (target - current)
            h = h.coerceIn(2f, height)
            currentHeights[i] = h

            val x = i * spacing
            val y = when (gravity) {
                PulseGravity.Bottom -> height - h
                PulseGravity.Top -> h
                PulseGravity.Center -> center - h / 2f
            }

            if (i == 0) {
                waveformPath.moveTo(x, y)
                when (gravity) {
                    PulseGravity.Bottom -> {
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    }
                    PulseGravity.Top -> {
                        fillPath.moveTo(x, 0f)
                        fillPath.lineTo(x, y)
                    }
                    PulseGravity.Center -> {
                        fillPath.moveTo(x, center + h / 2f)
                        fillPath.lineTo(x, y)
                    }
                }
            } else {
                val prevX = (i - 1) * spacing
                val prevH = currentHeights[i - 1]
                val prevY = when (gravity) {
                    PulseGravity.Bottom -> height - prevH
                    PulseGravity.Top -> prevH
                    PulseGravity.Center -> center - prevH / 2f
                }
                val midX = (prevX + x) / 2f

                waveformPath.cubicTo(midX, prevY, midX, y, x, y)
                fillPath.cubicTo(midX, prevY, midX, y, x, y)
                
                if (gravity == PulseGravity.Center) {
                    // For Center, we also need to draw the bottom half of the fill
                    // This is tricky with a single cubicTo.
                }
            }
        }

        when (gravity) {
            PulseGravity.Bottom -> fillPath.lineTo(width, height)
            PulseGravity.Top -> fillPath.lineTo(width, 0f)
            PulseGravity.Center -> {
                // Symmetric bottom path for center gravity
                for (i in count - 1 downTo 0) {
                    val x = i * spacing
                    val h = currentHeights[i]
                    val y = center + h / 2f
                    
                    if (i == count - 1) {
                        fillPath.lineTo(x, y)
                    } else {
                        val nextX = (i + 1) * spacing
                        val nextH = currentHeights[i + 1]
                        val nextY = center + nextH / 2f
                        val midX = (nextX + x) / 2f
                        fillPath.cubicTo(midX, nextY, midX, y, x, y)
                    }
                }
            }
        }
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
            
            if (gravity == PulseGravity.Center) {
                // Draw bottom outline for center gravity
                val bottomOutline = Path()
                for (i in 0 until count) {
                    val x = i * spacing
                    val h = currentHeights[i]
                    val y = center + h / 2f
                    if (i == 0) bottomOutline.moveTo(x, y)
                    else {
                        val prevX = (i - 1) * spacing
                        val prevH = currentHeights[i - 1]
                        val prevY = center + prevH / 2f
                        val midX = (prevX + x) / 2f
                        bottomOutline.cubicTo(midX, prevY, midX, y, x, y)
                    }
                }
                drawScope.drawPath(
                    path = bottomOutline,
                    color = color,
                    style = Stroke(
                        width = strokeWidthScale * drawScope.density,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}