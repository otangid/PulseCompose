package otang.id.lib.pulse.example.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SliderTrack(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    trackCornerSize: Dp = 14.dp,
    thumbTrackGapSize: Dp = 4.dp,
    height: Dp = IconButtonDefaults.extraSmallContainerSize().height,
) {
    SliderDefaults.Track(
        sliderState = sliderState,
        trackCornerSize = trackCornerSize,
        thumbTrackGapSize = thumbTrackGapSize,
        drawTick = { _, _ -> },
        modifier = modifier.height(height),
    )
}