package com.metrolist.music.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Real pixel height (status bar included) of the SpotifyTopAppBar currently on screen, so overlays
 * such as [ShufflePlayOverlay] can line up with its bottom edge instead of guessing.
 */
object SpotifyBarMetrics {
    var heightPx by mutableIntStateOf(0)
}

/** App-colour tint that fades into the page surface, like the 2018 Spotify header wash. */
@Composable
private fun spotifyGradientColors(): Pair<Color, Color> {
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    return lerp(surface, primary, 0.38f) to surface
}

/**
 * Big gradient behind the playlist/album header. It scrolls away with the list.
 */
@Composable
fun SpotifyHeaderGradient(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val (top, bottom) = spotifyGradientColors()
    Box(
        modifier =
        modifier
        .fillMaxWidth()
        .height(420.dp)
        .graphicsLayer {
            translationY =
            if (listState.firstVisibleItemIndex == 0) {
                -listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                -size.height
            }
        }.background(Brush.verticalGradient(listOf(top, bottom))),
    )
}

/**
 * Centered, bold-title app bar with its own app-colour gradient. The gradient and the title fade in
 * once the list has scrolled ([scrolled]); before that the bar is transparent over the header gradient.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyTopAppBar(
    title: @Composable () -> Unit,
                     scrolled: Boolean,
                     navigationIcon: @Composable () -> Unit = {},
                     actions: @Composable RowScope.() -> Unit = {},
                     forceOpaque: Boolean = false,
) {
    val (top, bottom) = spotifyGradientColors()
    val barAlpha by animateFloatAsState(if (scrolled || forceOpaque) 1f else 0f, label = "barAlpha")
    Box(modifier = Modifier.onSizeChanged { SpotifyBarMetrics.heightPx = it.height }) {
        Box(
            modifier =
            Modifier
            .matchParentSize()
            .alpha(barAlpha)
            .background(
                Brush.verticalGradient(
                    listOf(lerp(MaterialTheme.colorScheme.surface, top, 0.85f), lerp(MaterialTheme.colorScheme.surface, top, 0.45f)),
                ),
            ),
        )
        CenterAlignedTopAppBar(
            title = {
                ProvideTextStyle(MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) {
                    title()
                }
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
        )
    }
}
