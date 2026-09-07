package otang.id.lib.pulse.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import otang.id.lib.pulse.PulseConfig
import otang.id.lib.pulse.PulseGravity
import otang.id.lib.pulse.PulseRenderer
import otang.id.lib.pulse.PulseView
import otang.id.lib.pulse.example.ui.theme.PulseComposeTheme

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

@OptIn(UnstableApi::class)
@Composable
fun PulseTest() {
    val context = LocalContext.current
    val audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    // ExoPlayer State
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var audioSessionId by remember { mutableIntStateOf(0) }
    var useMovingAverage by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    val renderers = PulseRenderer.entries
    var rendererIndex by remember { mutableFloatStateOf(renderers.indexOf(PulseRenderer.WaveForm).toFloat()) }
    val currentRenderer = renderers[rendererIndex.toInt().coerceIn(0, renderers.size - 1)]

    val gravities = PulseGravity.entries
    var gravityIndex by remember { mutableFloatStateOf(gravities.indexOf(PulseGravity.Bottom).toFloat()) }
    val currentGravity = gravities[gravityIndex.toInt().coerceIn(0, gravities.size - 1)]

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (hasPermission) {
                    PulseView(
                        audioSessionId = audioSessionId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        config = PulseConfig(
                            renderer = currentRenderer,
                            gravity = currentGravity,
                            useMovingAverage = useMovingAverage
                        ),
                        isPlaying = isPlaying
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }) {
                            Text("Grant Record Audio Permission")
                        }
                    }
                }
            }

            // Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Renderer: ${currentRenderer.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Slider(
                    value = rendererIndex,
                    onValueChange = { rendererIndex = it },
                    valueRange = 0f..(renderers.size - 1).toFloat(),
                    steps = renderers.size - 2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Gravity: ${currentGravity.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Slider(
                    value = gravityIndex,
                    onValueChange = { gravityIndex = it },
                    valueRange = 0f..(gravities.size - 1).toFloat(),
                    steps = gravities.size - 2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Moving Average Smoothing", fontSize = 14.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Switch(
                        checked = useMovingAverage,
                        onCheckedChange = { useMovingAverage = it }
                    )
                }

                Text(text = "Now Playing: SoundHelix-Song-1.mp3")

                Spacer(modifier = Modifier.padding(4.dp))

                Button(onClick = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                        audioSessionId = exoPlayer.audioSessionId
                    }
                    isPlaying = !isPlaying
                }) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
            }
        }
    }
}