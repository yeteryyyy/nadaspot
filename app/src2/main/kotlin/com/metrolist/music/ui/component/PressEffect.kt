package com.metrolist.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Clickable without a ripple: while pressed the element instantly shrinks to 94% and dims a little.
 * Put it before any border/clip modifiers so those scale and dim along with the content.
 */
fun Modifier.spotifyPressable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    composed {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        this
            .graphicsLayer {
                val scale = if (pressed) 0.94f else 1f
                scaleX = scale
                scaleY = scale
                alpha = if (pressed) 0.8f else 1f
            }.clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
    }