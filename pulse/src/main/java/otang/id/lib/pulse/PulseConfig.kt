package otang.id.lib.pulse

import androidx.compose.ui.graphics.Color

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
    // Renderer specific options
    val barGapPx: Float = 2f,
    val filledBlockSize: Float = 10f,
    val emptyBlockSize: Float = 4f,
    val segmentCount: Int = 16,
    val isRoundedBarsEnabled: Boolean = true,
    val showOutline: Boolean = true,
    val showFill: Boolean = true
)
