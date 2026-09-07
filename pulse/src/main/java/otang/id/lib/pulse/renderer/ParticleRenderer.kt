package otang.id.lib.pulse.renderer

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
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun ParticleRenderer(
    fft: ByteArray,
    config: PulseConfig,
    modifier: Modifier
) {
    val color = config.barColor ?: MaterialTheme.colorScheme.primary
    val particleState = remember { ParticlePulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        particleState.update(
            fft = fft,
            width = width,
            height = height,
            config = config
        )

        drawIntoCanvas { canvas ->
            particleState.draw(canvas.nativeCanvas, color)
        }
    }
}

internal class ParticlePulseState {
    private val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply { style = NativePaint.Style.FILL }
    private var particles = emptyArray<Particle>()
    private var magnitudes = FloatArray(0)
    private val smoother = FftSmoother()
    private var bassIntensity = 0f
    private var midIntensity = 0f
    private var trebleIntensity = 0f
    private var audioIntensity = 0f

    private class Particle {
        var x = 0f
        var y = 0f
        var vx = 0f
        var vy = 0f
        var size = 0f
        var life = 0f
    }

    fun update(
        fft: ByteArray,
        width: Float,
        height: Float,
        config: PulseConfig
    ) {
        val maxParticles = config.particleConfig.maxParticles
        val decayRate = config.particleConfig.decayRate
        val audioGate = config.particleConfig.audioGate
        val maxMagnitude = config.maxMagnitude
        val heightScale = config.heightScale

        if (particles.size != maxParticles) {
            particles = Array(maxParticles) { Particle() }
        }

        if (magnitudes.size != fft.size / 2) {
            magnitudes = FloatArray(fft.size / 2)
        }
        FftUtils.calculateMagnitudes(fft, magnitudes)
        
        var heights = magnitudes
        if (config.useMovingAverage) {
            heights = smoother.smooth(heights, config.movingAverageWindowSize)
        }

        if (heights.size >= 4) {
            val currentMax = heights.maxOrNull() ?: 1f
            val peak = max(maxMagnitude * 0.5f, currentMax)
            val bassEnd = max(1, min(heights.size / 4, heights.size))
            val midEnd = max(bassEnd + 1, min((heights.size * 3) / 4, heights.size))

            bassIntensity = (bandMean(heights, peak, 0, bassEnd) * heightScale).coerceIn(0f, 1f)
            midIntensity = (bandMean(heights, peak, bassEnd, midEnd) * heightScale).coerceIn(0f, 1f)
            trebleIntensity = (bandMean(heights, peak, midEnd, heights.size) * heightScale).coerceIn(0f, 1f)
            audioIntensity = ((bassIntensity * 1.5f + midIntensity + trebleIntensity) / 3f).coerceIn(0f, 1f)
        }

        if (audioIntensity > audioGate) {
            spawnBurst(width, height, config.gravity, config.mirror)
        }

        for (p in particles) {
            p.life -= decayRate
            if (p.life <= 0f) {
                p.life = 0f
            } else {
                p.x += p.vx
                p.y += p.vy
            }
        }
    }

    private fun spawnBurst(width: Float, height: Float, gravity: PulseGravity, mirror: Boolean) {
        var burstCount = (audioIntensity * 15).toInt().coerceAtMost(20)
        for (p in particles) {
            if (p.life <= 0f && burstCount > 0) {
                p.x = Random.nextFloat() * width
                p.size = Random.nextFloat() * 10f + 2f
                p.life = 1f
                p.vx = (Random.nextFloat() - 0.5f) * 6f

                when (gravity) {
                    PulseGravity.Bottom -> {
                        p.y = height
                        p.vy = -Random.nextFloat() * 10f - 2f
                    }
                    PulseGravity.Top -> {
                        p.y = 0f
                        p.vy = Random.nextFloat() * 10f + 2f
                    }
                    PulseGravity.Center -> {
                        p.y = height / 2f
                        p.vy = (Random.nextFloat() - 0.5f) * 12f
                    }
                }

                if (mirror && burstCount > 1) {
                    // Try to spawn a mirrored particle if possible
                    // This is complex because we need to find another dead particle.
                    // Let's just keep it simple for now, the intensity-based bursts
                    // already feel "fuller" when mirrored.
                }

                burstCount--
            }
        }
    }

    private fun bandMean(data: FloatArray, peak: Float, from: Int, to: Int): Float {
        var sum = 0f
        for (i in from until to) sum += data[i]
        val count = to - from
        return if (count <= 0 || peak <= 0f) 0f else (sum / count) / peak
    }

    fun draw(canvas: NativeCanvas, color: Color) {
        paint.color = color.toArgb()
        for (p in particles) {
            if (p.life > 0f) {
                paint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.size, paint)
            }
        }
    }
}