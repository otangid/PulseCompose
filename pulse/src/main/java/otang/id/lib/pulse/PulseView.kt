package otang.id.lib.pulse

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import otang.id.lib.pulse.renderer.*

@Composable
fun PulseView(
    audioSessionId: Int,
    modifier: Modifier = Modifier,
    config: PulseConfig = PulseConfig(),
) {
    val pulseState = rememberPulseState(audioSessionId)
    PulseView(
        fft = pulseState.fft.value,
        modifier = modifier,
        config = config
    )
}

@Composable
fun PulseView(
    fft: ByteArray,
    modifier: Modifier = Modifier,
    config: PulseConfig = PulseConfig(),
) {
    when (config.renderer) {
        PulseRenderer.FadingBlock -> FadingBlockRenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            barGapPx = config.barGapPx,
            filledBlockSize = config.filledBlockSize,
            emptyBlockSize = config.emptyBlockSize,
            modifier = modifier
        )

        PulseRenderer.Matrix -> MatrixRenderer(
            fft = fft,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            barGapPx = config.barGapPx,
            modifier = modifier
        )

        PulseRenderer.Minimal -> MinimalRenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            modifier = modifier
        )

        PulseRenderer.Neon -> NeonRenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            barGapPx = config.barGapPx,
            modifier = modifier
        )

        PulseRenderer.Particle -> ParticleRenderer(
            fft = fft,
            barColor = config.barColor,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            modifier = modifier
        )

        PulseRenderer.RetroVU -> RetroVURenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            segmentCount = config.segmentCount,
            modifier = modifier
        )

        PulseRenderer.SolidLine -> SolidLineRenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            barGapPx = config.barGapPx,
            isRoundedBarsEnabled = config.isRoundedBarsEnabled,
            modifier = modifier
        )

        PulseRenderer.Sparkle -> SparkleRenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            modifier = modifier
        )

        PulseRenderer.WaveForm -> WaveFormRenderer(
            fft = fft,
            barColor = config.barColor,
            barCount = config.barCount,
            maxMagnitude = config.maxMagnitude,
            heightScale = config.heightScale,
            showOutline = config.showOutline,
            showFill = config.showFill,
            modifier = modifier
        )
    }
}