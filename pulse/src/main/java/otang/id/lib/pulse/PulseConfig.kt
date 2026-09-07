package otang.id.lib.pulse

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class PulseRenderer {
    FadingBlock,
    Matrix,
    Minimal,
    Neon,
    Particle,
    RetroVU,
    SolidLine,
    Sparkle,
    WaveForm
}

data class PulseConfig(
    val renderer: PulseRenderer = PulseRenderer.WaveForm,
    val barColor: Color? = null,
    val barCount: Int = 32,
    val maxMagnitude: Float = 128f,
    val heightScale: Float = 1f,
    val smoothing: Float = 0.2f,
    val useMovingAverage: Boolean = false,
    val movingAverageWindowSize: Int = 2,

    // Renderer specific configurations
    val fadingBlockConfig: FadingBlockConfig = FadingBlockConfig(),
    val matrixConfig: MatrixConfig = MatrixConfig(),
    val minimalConfig: MinimalConfig = MinimalConfig(),
    val neonConfig: NeonConfig = NeonConfig(),
    val particleConfig: ParticleConfig = ParticleConfig(),
    val retroVUConfig: RetroVUConfig = RetroVUConfig(),
    val solidLineConfig: SolidLineConfig = SolidLineConfig(),
    val sparkleConfig: SparkleConfig = SparkleConfig(),
    val waveFormConfig: WaveFormConfig = WaveFormConfig()
)

data class FadingBlockConfig(
    val barGapPx: Float = 2f,
    val filledBlockSize: Float = 10f,
    val emptyBlockSize: Float = 4f,
    val fadeAlpha: Int = 200 // 0..255
)

data class MatrixConfig(
    val barGapPx: Float = 2f,
    val brightGreen: Int = Color(0, 255, 65, 255).toArgb(),
    val mediumGreen: Int = Color(0, 220, 55, 200).toArgb(),
    val darkGreen: Int = Color(0, 160, 40, 120).toArgb(),
    val glowAlpha: Int = 160,
    val changeInterval: Int = 3
)

data class MinimalConfig(
    val strokeWidthScale: Float = 2.5f,
    val alpha: Float = 0.7f
)

data class NeonConfig(
    val barGapPx: Float = 2f,
    val glowAlpha: Int = 180,
    val glowRadius: Float = 12f
)

data class ParticleConfig(
    val maxParticles: Int = 300,
    val decayRate: Float = 0.012f,
    val audioGate: Float = 0.05f
)

data class RetroVUConfig(
    val segmentCount: Int = 16,
    val segmentGapPx: Float = 4f
)

data class SolidLineConfig(
    val barGapPx: Float = 2f,
    val isRoundedBarsEnabled: Boolean = true,
    val cornerRadius: Float = 32f
)

data class SparkleConfig(
    val sparkleCount: Int = 200,
    val glowRadius: Float = 6f
)

data class WaveFormConfig(
    val showOutline: Boolean = true,
    val showFill: Boolean = true,
    val fillAlpha: Float = 0.3f,
    val strokeWidthScale: Float = 3f
)
