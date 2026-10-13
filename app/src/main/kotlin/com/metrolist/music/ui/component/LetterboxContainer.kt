package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Draws black bars at the top and bottom so the content area has a fixed aspect ratio
 * (height / width), when the screen is taller than that ratio. [ratio] == null disables it.
 */
@Composable
fun LetterboxContainer(
    ratio: Float?,
    content: @Composable () -> Unit,
) {
    if (ratio == null) {
        content()
        return
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val totalHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val targetHeightPx = widthPx * ratio
        if (maxHeight <= maxWidth || targetHeightPx >= totalHeightPx) {
            // Landscape or already not taller than target: nothing to letterbox.
            content()
        } else {
            val barDp = with(LocalDensity.current) { ((totalHeightPx - targetHeightPx) / 2f).toDp() }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(maxHeight - barDp * 2)
                        .consumeWindowInsets(PaddingValues(top = barDp, bottom = barDp)),
            ) {
                content()
            }
        }
    }
}
