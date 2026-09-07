package otang.id.lib.pulse

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import otang.id.lib.pulse.renderer.FadingBlockRenderer
import otang.id.lib.pulse.renderer.MatrixRenderer
import otang.id.lib.pulse.renderer.MinimalRenderer
import otang.id.lib.pulse.renderer.NeonRenderer
import otang.id.lib.pulse.renderer.ParticleRenderer
import otang.id.lib.pulse.renderer.RetroVURenderer
import otang.id.lib.pulse.renderer.SolidLineRenderer
import otang.id.lib.pulse.renderer.SparkleRenderer
import otang.id.lib.pulse.renderer.WaveFormRenderer

@Composable
fun PulseView(
    audioSessionId: Int,
    modifier: Modifier = Modifier,
    config: PulseConfig = PulseConfig(),
    isPlaying: Boolean = true,
) {
    val pulseState = rememberPulseState(audioSessionId)
    PulseView(
        fft = pulseState.fft.value,
        modifier = modifier,
        config = config,
        isPlaying = isPlaying
    )
}

@Composable
fun PulseView(
    fft: ByteArray,
    modifier: Modifier = Modifier,
    config: PulseConfig = PulseConfig(),
    isPlaying: Boolean = true,
) {
    val alpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(500),
        label = "PulseViewAlpha"
    )

    val animatedModifier = modifier.graphicsLayer(alpha = alpha)

    when (config.renderer) {
        PulseRenderer.FadingBlock -> FadingBlockRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.Matrix -> MatrixRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.Minimal -> MinimalRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.Neon -> NeonRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.Particle -> ParticleRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.RetroVU -> RetroVURenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.SolidLine -> SolidLineRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.Sparkle -> SparkleRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )

        PulseRenderer.WaveForm -> WaveFormRenderer(
            fft = fft,
            config = config,
            modifier = animatedModifier
        )
    }
}