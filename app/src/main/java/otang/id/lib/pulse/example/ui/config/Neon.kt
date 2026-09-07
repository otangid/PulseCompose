package otang.id.lib.pulse.example.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import otang.id.lib.pulse.NeonConfig
import otang.id.lib.pulse.example.ui.components.SliderTrack
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Neon(defConfig: NeonConfig, onSaved: (config: NeonConfig) -> Unit, onDismiss: () -> Unit) {
    val state = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    var barGapPx by remember { mutableFloatStateOf(defConfig.barGapPx) }
    var glowAlpha by remember { mutableFloatStateOf(defConfig.glowAlpha.toFloat()) }
    var glowRadius by remember { mutableFloatStateOf(defConfig.glowRadius) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Neon Config",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Bar Gap: $barGapPx", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = barGapPx,
                valueRange = 2f..12f,
                steps = (12 - 2) / 1 - 1,
                onValueChange = { barGapPx = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Glow Alpha: $glowAlpha", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = glowAlpha,
                valueRange = 100f..200f,
                steps = (200 - 100) / 1 - 1,
                onValueChange = { glowAlpha = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Glow Radius: $glowRadius", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = glowRadius,
                valueRange = 8f..25f,
                steps = (25 - 8) / 1 - 1,
                onValueChange = { glowRadius = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    onSaved(
                        NeonConfig(
                            barGapPx = barGapPx,
                            glowAlpha = glowAlpha.roundToInt(),
                            glowRadius = glowRadius
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
            Spacer(Modifier.height(16.dp))
        }
    }
}