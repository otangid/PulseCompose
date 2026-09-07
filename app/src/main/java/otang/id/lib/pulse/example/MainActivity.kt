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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import otang.id.lib.pulse.FadingBlockConfig
import otang.id.lib.pulse.MatrixConfig
import otang.id.lib.pulse.MinimalConfig
import otang.id.lib.pulse.NeonConfig
import otang.id.lib.pulse.ParticleConfig
import otang.id.lib.pulse.PulseConfig
import otang.id.lib.pulse.PulseGravity
import otang.id.lib.pulse.PulseRenderer
import otang.id.lib.pulse.PulseView
import otang.id.lib.pulse.RetroVUConfig
import otang.id.lib.pulse.SolidLineConfig
import otang.id.lib.pulse.SparkleConfig
import otang.id.lib.pulse.WaveFormConfig
import otang.id.lib.pulse.example.ui.components.NoPaddingDropDownMenu
import otang.id.lib.pulse.example.ui.components.SliderTrack
import otang.id.lib.pulse.example.ui.config.FadingBlock
import otang.id.lib.pulse.example.ui.config.Matrix
import otang.id.lib.pulse.example.ui.config.Minimal
import otang.id.lib.pulse.example.ui.config.Neon
import otang.id.lib.pulse.example.ui.config.Particle
import otang.id.lib.pulse.example.ui.config.RetroVU
import otang.id.lib.pulse.example.ui.config.SolidLine
import otang.id.lib.pulse.example.ui.config.Sparkle
import otang.id.lib.pulse.example.ui.config.WaveForm
import otang.id.lib.pulse.example.ui.theme.PulseComposeTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseComposeTheme {
                PulseExample()
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PulseExample() {
    val context = LocalContext.current

    // Permissions
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
    ) { isGranted -> hasPermission = isGranted }

    //ExoPlayer
    val audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var audioSessionId by remember { mutableIntStateOf(0) }

    var checked by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Renderer
    val renderers = PulseRenderer.entries
    var rendererIndex by remember { mutableIntStateOf(renderers.indexOf(PulseRenderer.FadingBlock)) }
    val currentRenderer = renderers[rendererIndex.coerceIn(0, renderers.size - 1)]
    // Gravity
    val gravities = PulseGravity.entries
    var gravityIndex by remember { mutableIntStateOf(gravities.indexOf(PulseGravity.Bottom)) }
    val currentGravity = gravities[gravityIndex.coerceIn(0, gravities.size - 1)]
    // Others
    var smooth by remember { mutableStateOf(true) }
    var mirror by remember { mutableStateOf(false) }
    var magnitude by remember { mutableFloatStateOf(128f) }
    var barCount by remember { mutableIntStateOf(32) }
    var windowSize by remember { mutableIntStateOf(2) }
    var heightScale by remember { mutableFloatStateOf(1f) }
    var smoothing by remember { mutableFloatStateOf(0.2f) }
    // Renderer
    var fadingBlockConfig by remember { mutableStateOf(FadingBlockConfig()) }
    var matrixConfig by remember { mutableStateOf(MatrixConfig()) }
    var minimalConfig by remember { mutableStateOf(MinimalConfig()) }
    var neonConfig by remember { mutableStateOf(NeonConfig()) }
    var particleConfig by remember { mutableStateOf(ParticleConfig()) }
    var retroVUConfig by remember { mutableStateOf(RetroVUConfig()) }
    var solidLineConfig by remember { mutableStateOf(SolidLineConfig()) }
    var sparkleConfig by remember { mutableStateOf(SparkleConfig()) }
    var waveFormConfig by remember { mutableStateOf(WaveFormConfig()) }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pulse ($currentRenderer)") },
                actions = {
                    SplitButtonLayout(
                        spacing = 4.dp,
                        modifier = Modifier.padding(end = 12.dp),
                        leadingButton = {
                            SplitButtonDefaults.LeadingButton(
                                { showConfigDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) { Icon(Icons.Default.Tune, null) }
                        },
                        trailingButton = {
                            SplitButtonDefaults.TrailingButton(
                                checked = checked,
                                onCheckedChange = { checked = it },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                val rotation: Float by animateFloatAsState(targetValue = if (checked) 180f else 0f)
                                Icon(
                                    Icons.Outlined.KeyboardArrowDown,
                                    null,
                                    modifier = Modifier
                                        .size(SplitButtonDefaults.LeadingIconSize)
                                        .graphicsLayer { this.rotationZ = rotation },
                                )
                            }
                        }
                    )
                    NoPaddingDropDownMenu(expanded = checked, onDismissRequest = { checked = false }) {
                        DropdownMenuItem(
                            text = { Text("Fading Block") },
                            leadingIcon = { Icon(if (rendererIndex == 0) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 0
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Matrix") },
                            leadingIcon = { Icon(if (rendererIndex == 1) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 1
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Minimal") },
                            leadingIcon = { Icon(if (rendererIndex == 2) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 2
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Neon") },
                            leadingIcon = { Icon(if (rendererIndex == 3) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 3
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Particle") },
                            leadingIcon = { Icon(if (rendererIndex == 4) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 4
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("RetroVU") },
                            leadingIcon = { Icon(if (rendererIndex == 5) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 5
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Solid Line") },
                            leadingIcon = { Icon(if (rendererIndex == 6) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 6
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Sparkle") },
                            leadingIcon = { Icon(if (rendererIndex == 7) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 7
                                checked = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Wave Form") },
                            leadingIcon = { Icon(if (rendererIndex == 8) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null) },
                            onClick = {
                                rendererIndex = 8
                                checked = false
                            },
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (hasPermission) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    PulseView(
                        audioSessionId = audioSessionId,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize(),
                        config = PulseConfig(
                            renderer = currentRenderer,
                            gravity = currentGravity,
                            barCount = barCount,
                            maxMagnitude = magnitude,
                            heightScale = heightScale,
                            smoothing = smoothing,
                            useMovingAverage = smooth,
                            movingAverageWindowSize = windowSize,
                            mirror = mirror,
                            fadingBlockConfig = fadingBlockConfig,
                            matrixConfig = matrixConfig,
                            minimalConfig = minimalConfig,
                            neonConfig = neonConfig,
                            particleConfig = particleConfig,
                            retroVUConfig = retroVUConfig,
                            solidLineConfig = solidLineConfig,
                            sparkleConfig = sparkleConfig,
                            waveFormConfig = waveFormConfig
                        )
                    )
                }
                Row {
                    gravities.forEachIndexed { index, gravity ->
                        ToggleButton(
                            checked = index == gravityIndex,
                            onCheckedChange = { gravityIndex = index },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                gravities.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) { Text("$gravity") }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Smooth")
                    Switch(
                        checked = smooth,
                        onCheckedChange = { smooth = it }
                    )
                    Text("Mirror")
                    Switch(
                        checked = mirror,
                        onCheckedChange = { mirror = it }
                    )
                }
                Text("Max Magnitude: $magnitude")
                Slider(
                    value = magnitude,
                    valueRange = 64f..255f,
                    steps = (255 - 64) / 1 - 1,
                    track = { SliderTrack(it) },
                    onValueChange = { magnitude = it.roundToInt().toFloat() }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bar Count: $barCount")
                        Slider(
                            value = barCount.toFloat(),
                            valueRange = 16f..128f,
                            steps = (128 - 16) / 16 - 1,
                            track = { SliderTrack(it) },
                            onValueChange = { barCount = it.roundToInt() }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Average Window: $windowSize")
                        Slider(
                            value = windowSize.toFloat(),
                            valueRange = 2f..8f,
                            steps = (8 - 2) / 1 - 1,
                            track = { SliderTrack(it) },
                            onValueChange = { windowSize = it.roundToInt() }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("High Scale: $heightScale")
                        Slider(
                            value = heightScale,
                            valueRange = 0.5f..1.5f,
                            track = { SliderTrack(it) },
                            onValueChange = { heightScale = it }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Smoothing: $smoothing")
                        Slider(
                            value = smoothing,
                            valueRange = 0.1f..0.5f,
                            track = { SliderTrack(it) },
                            onValueChange = { smoothing = it }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Now Playing: SoundHelix-Song-1.mp3")
                FilledIconButton(
                    {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                            audioSessionId = exoPlayer.audioSessionId
                        }
                        isPlaying = !isPlaying
                    },
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 16.dp)
                        .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        "Player",
                        modifier = Modifier.size(IconButtonDefaults.largeIconSize)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button({
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }) { Text("Grant Record Audio Permission") }
                }
            }
        }
    }

    if (showConfigDialog) {
        when (rendererIndex) {
            0 -> FadingBlock(
                defConfig = fadingBlockConfig,
                onSaved = { fadingBlockConfig = it }
            ) { showConfigDialog = false }

            1 -> Matrix(
                defConfig = matrixConfig,
                onSaved = { matrixConfig = it }
            ) { showConfigDialog = false }

            2 -> Minimal(
                defConfig = minimalConfig,
                onSaved = { minimalConfig = it }
            ) { showConfigDialog = false }

            3 -> Neon(
                defConfig = neonConfig,
                onSaved = { neonConfig = it }
            ) { showConfigDialog = false }

            4 -> Particle(
                defConfig = particleConfig,
                onSaved = { particleConfig = it }
            ) { showConfigDialog = false }

            5 -> RetroVU(
                defConfig = retroVUConfig,
                onSaved = { retroVUConfig = it }
            ) { showConfigDialog = false }

            6 -> SolidLine(
                defConfig = solidLineConfig,
                onSaved = { solidLineConfig = it }
            ) { showConfigDialog = false }

            7 -> Sparkle(
                defConfig = sparkleConfig,
                onSaved = { sparkleConfig = it }
            ) { showConfigDialog = false }

            8 -> WaveForm(
                defConfig = waveFormConfig,
                onSaved = { waveFormConfig = it }
            ) { showConfigDialog = false }
        }
    }
}