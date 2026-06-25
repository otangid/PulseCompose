package otang.id.lib.pulse.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import otang.id.lib.pulse.PulseView
import otang.id.lib.pulse.example.ui.theme.PulseComposeTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

val staticDummyFft = floatArrayOf(
    10f, 45f, 120f, 250f, 300f, 280f, 150f, 80f, // Bass
    60f, 90f, 110f, 180f, 200f, 160f, 100f, 70f, // Mid
    50f, 85f, 130f, 140f, 95f, 60f, 40f, 25f,    // High-Mid
    15f, 30f, 45f, 55f, 35f, 20f, 10f, 5f        // Treble
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseComposeTheme {
                PulseTest()
            }
        }
    }
}

@Composable
fun PulseTest() {
    var dummyHeights by remember { mutableStateOf(FloatArray(32) { 2f }) }

    // Simulasi update data FFT seolah-olah dari audio engine (setiap ~50ms)
    LaunchedEffect(Unit) {
        while (true) {
            val newHeights = FloatArray(32) { index ->
                // Membuat pola melengkung di tengah (seperti visualizer umumnya)
                // dicampur dengan nilai random agar bergerak.
                // Asumsi max tinggi bar adalah 300f
                val baseHeight = if (index < 32 / 2) index * 10f else (32 - index) * 10f
                val randomJitter = Random.nextFloat() * 150f

                baseHeight + randomJitter
            }
            dummyHeights = newHeights
            delay(500L.milliseconds) // Setara dengan ~20 FPS update rate
        }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PulseView(dummyHeights, modifier = Modifier.fillMaxSize())
        }
    }
}