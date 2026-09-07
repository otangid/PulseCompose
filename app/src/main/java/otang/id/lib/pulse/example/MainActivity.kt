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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import otang.id.lib.pulse.PulseConfig
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
    var rendererIndex by remember { mutableStateOf(renderers.indexOf(PulseRenderer.WaveForm).toFloat()) }
    val currentRenderer = renderers[rendererIndex.toInt().coerceIn(0, renderers.size - 1)]

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
                        modifier = Modifier.fillMaxSize(),
                        config = PulseConfig(renderer = currentRenderer)
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

                Spacer(modifier = Modifier.padding(8.dp))

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