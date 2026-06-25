package otang.id.lib.pulse

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import otang.id.lib.pulse.renderer.RetroVURenderer
import otang.id.lib.pulse.renderer.SparkleRenderer
import otang.id.lib.pulse.renderer.WaveFormRenderer

@Composable
fun PulseView(
    fft: FloatArray,
    modifier: Modifier,
) {
    WaveFormRenderer(fft, modifier = modifier)
}