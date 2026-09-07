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
import otang.id.lib.pulse.SparkleConfig
import otang.id.lib.pulse.example.ui.components.SliderTrack
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sparkle(defConfig: SparkleConfig, onSaved: (config: SparkleConfig) -> Unit, onDismiss: () -> Unit) {
    val state = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    var count by remember { mutableFloatStateOf(defConfig.sparkleCount.toFloat()) }
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
                "FadingBlock Config",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Sparkle Count: $count", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = count,
                valueRange = 100f..500f,
                steps = (500 - 100) / 50 - 1,
                onValueChange = { count = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Text("Glow Radius: $glowRadius", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            Slider(
                value = glowRadius,
                valueRange = 2f..10f,
                steps = (10 - 2) / 1 - 1,
                onValueChange = { glowRadius = it.roundToInt().toFloat() },
                track = { SliderTrack(it) },
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    onSaved(
                        SparkleConfig(
                            sparkleCount = count.roundToInt(),
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