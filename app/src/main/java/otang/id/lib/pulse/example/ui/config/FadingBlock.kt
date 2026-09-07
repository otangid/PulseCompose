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
import otang.id.lib.pulse.FadingBlockConfig
import otang.id.lib.pulse.example.ui.components.SliderTrack
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FadingBlock(defConfig: FadingBlockConfig, onSaved: (config: FadingBlockConfig) -> Unit, onDismiss: () -> Unit) {
    val state = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    var barGapPx by remember { mutableFloatStateOf(defConfig.barGapPx) }
    var filledSize by remember { mutableFloatStateOf(defConfig.filledBlockSize) }
    var emptySize by remember { mutableFloatStateOf(defConfig.emptyBlockSize) }
    var fadeAlpha by remember { mutableFloatStateOf(defConfig.fadeAlpha.toFloat()) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "FadingBlock Config",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Bar Gap: $barGapPx", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = barGapPx,
                valueRange = 2f..10f,
                steps = (10 - 2) / 1 - 1,
                onValueChange = { barGapPx = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Filled Block Size: $filledSize", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = filledSize,
                valueRange = 5f..20f,
                steps = (20 - 5) / 1 - 1,
                onValueChange = { filledSize = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Empty Block Size: $emptySize", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = emptySize,
                valueRange = 2f..10f,
                steps = (10 - 2) / 1 - 1,
                onValueChange = { emptySize = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Fade Alpha: $fadeAlpha", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = fadeAlpha,
                valueRange = 150f..230f,
                steps = (230 - 150) / 1 - 1,
                onValueChange = { fadeAlpha = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    onSaved(
                        FadingBlockConfig(
                            barGapPx = barGapPx,
                            filledBlockSize = filledSize,
                            emptyBlockSize = emptySize,
                            fadeAlpha = fadeAlpha.roundToInt()
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