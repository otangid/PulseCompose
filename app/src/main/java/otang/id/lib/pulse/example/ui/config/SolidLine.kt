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
import otang.id.lib.pulse.SolidLineConfig
import otang.id.lib.pulse.example.ui.components.SliderTrack
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolidLine(defConfig: SolidLineConfig, onSaved: (config: SolidLineConfig) -> Unit, onDismiss: () -> Unit) {
    val state = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    var isRounding by remember { mutableStateOf(defConfig.isRoundedBarsEnabled) }
    var barGapPx by remember { mutableFloatStateOf(defConfig.barGapPx) }
    var cornerRadius by remember { mutableFloatStateOf(defConfig.cornerRadius) }
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
            Text("Enable Rounding", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Switch(
                checked = isRounding,
                onCheckedChange = { isRounding = it }
            )
            Text("Bar Gap: $barGapPx", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = barGapPx,
                valueRange = 2f..10f,
                steps = (10 - 2) / 1 - 1,
                onValueChange = { barGapPx = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Corner Radius: $cornerRadius", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = cornerRadius,
                valueRange = 4f..50f,
                steps = (50 - 4) / 1 - 1,
                onValueChange = { cornerRadius = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    onSaved(
                        SolidLineConfig(
                            barGapPx = barGapPx,
                            isRoundedBarsEnabled = isRounding,
                            cornerRadius = cornerRadius
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