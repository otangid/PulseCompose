package otang.id.lib.pulse.renderer

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import otang.id.lib.pulse.FftSmoother
import otang.id.lib.pulse.FftUtils
import otang.id.lib.pulse.PulseConfig
import otang.id.lib.pulse.PulseGravity
import kotlin.random.Random

@Composable
fun SparkleRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val state = remember(config.barCount) { SparklePulseState(config.barCount) }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.update(
            fft = fft,
            width = size.width,
            height = size.height,
            config = config
        )

        drawIntoCanvas { canvas ->
            state.draw(
                canvas = canvas.nativeCanvas,
                color = color,
                glowRadius = config.sparkleConfig.glowRadius
            )
        }
    }
}

internal class SparklePulseState(private val barCount: Int) {
    private val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var sparkles = emptyArray<Sparkle>()
    private var currentHeights = FloatArray(0)
    private val smoother = FftSmoother()
    private var lastGlowRadius = 0f

    private class Sparkle {
        var x = 0f
        var y = 0f
        var vx = 0f
        var vy = 0f
        var size = 0f
        var life = 0f
        var alive = false
    }

    fun update(
        fft: ByteArray,
        width: Float,
        height: Float,
        config: PulseConfig
    ) {
        val sparkleCount = config.sparkleConfig.sparkleCount
        val maxMagnitude = config.maxMagnitude
        val heightScale = config.heightScale

        if (sparkles.size != sparkleCount) {
            sparkles = Array(sparkleCount) { Sparkle() }
        }

        val spacing = if (barCount > 0) width / barCount else 0f

        if (currentHeights.size != barCount) currentHeights = FloatArray(barCount)
        
        var magnitudes = FloatArray(fft.size / 2)
        FftUtils.calculateMagnitudes(fft, magnitudes)
        
        if (config.useMovingAverage) {
            magnitudes = smoother.smooth(magnitudes, config.movingAverageWindowSize)
        }

        if (config.mirror) {
            val halfCount = barCount / 2
            for (i in 0 until halfCount) {
                val mag = if (i < magnitudes.size) magnitudes[i] else 0f
                val normalized = mag / maxMagnitude
                val h = (normalized * height * heightScale).coerceIn(2f, height)
                
                val rightIdx = halfCount + i
                val leftIdx = halfCount - 1 - i
                
                if (rightIdx < barCount) currentHeights[rightIdx] = h
                if (leftIdx >= 0) currentHeights[leftIdx] = h
            }
        } else {
            for ((i, element) in magnitudes.withIndex()) {
                if (i >= barCount) break
                val normalized = element / maxMagnitude
                currentHeights[i] = (normalized * height * heightScale).coerceIn(2f, height)
            }
        }

        for (p in sparkles) {
            if (p.alive) {
                p.life -= 0.02f
                if (p.life <= 0f) p.alive = false
                p.x += p.vx
                p.y += p.vy
            } else if (Random.nextFloat() < 0.05f) {
                val idx = Random.nextInt(barCount)
                p.alive = true
                p.x = (idx * spacing) + (spacing / 2)
                p.vx = (Random.nextFloat() - 0.5f) * 10f
                p.size = Random.nextFloat() * 6f + 2f
                p.life = 1f

                val h = currentHeights.getOrElse(idx) { 2f }
                when (config.gravity) {
                    PulseGravity.Bottom -> {
                        p.y = height - h
                        p.vy = -Random.nextFloat() * 5f - 2f
                    }

                    PulseGravity.Top -> {
                        p.y = h
                        p.vy = Random.nextFloat() * 5f + 2f
                    }

                    PulseGravity.Center -> {
                        if (Random.nextBoolean()) {
                            p.y = height / 2f - h / 2f
                            p.vy = -Random.nextFloat() * 5f - 2f
                        } else {
                            p.y = height / 2f + h / 2f
                            p.vy = Random.nextFloat() * 5f + 2f
                        }
                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas, color: Color, glowRadius: Float) {
        sparklePaint.color = color.toArgb()
        if (glowRadius != lastGlowRadius) {
            lastGlowRadius = glowRadius
            sparklePaint.maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
        }
        for (p in sparkles) {
            if (p.alive) {
                sparklePaint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.size, sparklePaint)
            }
        }
    }
}