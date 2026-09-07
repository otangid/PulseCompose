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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import otang.id.lib.pulse.WaveFormConfig
import otang.id.lib.pulse.example.ui.components.SliderTrack
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveForm(defConfig: WaveFormConfig, onSaved: (config: WaveFormConfig) -> Unit, onDismiss: () -> Unit) {
    val state = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    var showFill by remember { mutableStateOf(defConfig.showFill) }
    var showOutline by remember { mutableStateOf(defConfig.showOutline) }
    var fillAlpha by remember { mutableFloatStateOf(defConfig.fillAlpha) }
    var widthScale by remember { mutableFloatStateOf(defConfig.strokeWidthScale) }
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
            Text("Show Fill", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Switch(
                checked = showFill,
                onCheckedChange = { showFill = it }
            )
            Text("Show Outline", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Switch(
                checked = showOutline,
                onCheckedChange = { showOutline = it }
            )
            Text("Fill Alpha: $fillAlpha", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = fillAlpha,
                valueRange = 0.1f..0.5f,
                onValueChange = { fillAlpha = it },
                track = { SliderTrack(it) },
            )
            Text("Stroke Width Scale: $widthScale", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = widthScale,
                valueRange = 2f..6f,
                steps = (6 - 2) / 1 - 1,
                onValueChange = { widthScale = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    onSaved(
                        WaveFormConfig(
                            showFill = showFill,
                            showOutline = showOutline,
                            fillAlpha = fillAlpha,
                            strokeWidthScale = widthScale
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