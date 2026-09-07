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
import otang.id.lib.pulse.MinimalConfig
import otang.id.lib.pulse.example.ui.components.SliderTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Minimal(defConfig: MinimalConfig, onSaved: (config: MinimalConfig) -> Unit, onDismiss: () -> Unit) {
    val state = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    var widthScale by remember { mutableFloatStateOf(defConfig.strokeWidthScale) }
    var alpha by remember { mutableFloatStateOf(defConfig.alpha) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Minimal Config",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Stroke Width Scale: $widthScale", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = widthScale,
                valueRange = 1f..5f,
                onValueChange = { widthScale = it },
                track = { SliderTrack(it) },
            )
            Text("Alpha: $alpha", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = alpha,
                valueRange = 0.3f..1f,
                onValueChange = { alpha = it },
                track = { SliderTrack(it) },
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    onSaved(
                        MinimalConfig(
                            strokeWidthScale = widthScale,
                            alpha = alpha
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