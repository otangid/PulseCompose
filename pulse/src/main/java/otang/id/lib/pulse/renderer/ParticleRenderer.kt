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
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint as NativePaint

@Composable
fun ParticleRenderer(
    fft: ByteArray,
    barColor: Color? = null,
    maxMagnitude: Float = 128f,
    heightScale: Float = 1f,
    modifier: Modifier
) {
    val color = barColor ?: MaterialTheme.colorScheme.primary
    val particleState = remember { ParticlePulseState() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        particleState.update(fft, width, height, maxMagnitude, heightScale)

        drawIntoCanvas { canvas ->
            particleState.draw(canvas.nativeCanvas, color)
        }
    }
}

internal class ParticlePulseState(
    maxParticles: Int = 300,
    private val decayRate: Float = 0.012f,
    private val audioGate: Float = 0.05f
) {
    private val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply { style = NativePaint.Style.FILL }
    private val particles = Array(maxParticles) { Particle() }
    private var magnitudes = FloatArray(0)

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

    fun update(fft: ByteArray, width: Float, height: Float, maxMagnitude: Float, heightScale: Float) {
        if (magnitudes.size != fft.size / 2) {
            magnitudes = FloatArray(fft.size / 2)
        }
        otang.id.lib.pulse.FftUtils.calculateMagnitudes(fft, magnitudes)
        val heights = magnitudes

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
            spawnBurst(width, height)
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

    private fun spawnBurst(width: Float, height: Float) {
        var burstCount = (audioIntensity * 15).toInt().coerceAtMost(20)
        for (p in particles) {
            if (p.life <= 0f && burstCount > 0) {
                p.x = Random.nextFloat() * width
                p.y = height
                p.vx = (Random.nextFloat() - 0.5f) * 6f
                p.vy = -Random.nextFloat() * 10f - 2f
                p.size = Random.nextFloat() * 10f + 2f
                p.life = 1f
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