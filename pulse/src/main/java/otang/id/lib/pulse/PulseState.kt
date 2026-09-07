package otang.id.lib.pulse

import android.media.audiofx.Visualizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

interface PulseState {
    val fft: State<ByteArray>
}

internal class VisualizerPulseState(
    private val audioSessionId: Int,
    private val captureSize: Int = 512,
    private val captureRate: Int = Visualizer.getMaxCaptureRate()
) : PulseState {
    private val _fft = mutableStateOf(ByteArray(0))
    override val fft: State<ByteArray> = _fft

    private var visualizer: Visualizer? = null

    fun start() {
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = this@VisualizerPulseState.captureSize
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        // Not used
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        fft?.let {
                            _fft.value = it.copyOf()
                        }
                    }
                }, captureRate, false, true)
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
    }
}

@Composable
fun rememberPulseState(
    audioSessionId: Int,
    captureSize: Int = 512,
    captureRate: Int = Visualizer.getMaxCaptureRate()
): PulseState {
    val state = remember(audioSessionId) {
        VisualizerPulseState(audioSessionId, captureSize, captureRate)
    }

    DisposableEffect(audioSessionId) {
        state.start()
        onDispose {
            state.stop()
        }
    }

    return state
}
