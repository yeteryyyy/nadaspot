package com.metrolist.music.ui.component

import androidx.compose.ui.zIndex
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.music.R
import com.metrolist.music.ui.theme.SpotifyFontFamily

/** The big green "SHUFFLE PLAY" pill (Spotify 2018 style) shared by every album/playlist header. */
@Composable
fun ShufflePlayButton(
    onClick: () -> Unit,
                      modifier: Modifier = Modifier,
) {
    Box(
        modifier =
        modifier
        .height(48.dp)
        .fillMaxWidth(0.8f)
        .spotifyPressable(onClick = onClick)
        .clip(RoundedCornerShape(percent = 50))
        .background(MaterialTheme.colorScheme.primary)
        .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.shuffle_play),
             color = Color.White,
             fontSize = 14.sp,
             fontFamily = SpotifyFontFamily,
             fontWeight = FontWeight.Bold,
             letterSpacing = 2.sp,
             maxLines = 1,
        )
    }
}

/** Space reserved at the bottom of a header for the pill (pill + 8dp above and below). */
val ShufflePlayBlockHeight = 64.dp

/**
 * SHUFFLE PLAY pill that rides at the bottom of the header while it is on screen, then sticks right
 * under the top bar. Drawn as an overlay (not a stickyHeader) so it never hides under the top bar.
 * The header item must reserve [ShufflePlayBlockHeight] at its bottom.
 */
@Composable
fun ShufflePlayOverlay(
    listState: androidx.compose.foundation.lazy.LazyListState,
    visible: Boolean = true,
    headerKey: Any = "playlist_header",
    onClick: () -> Unit,
) {
    val blockPx = with(androidx.compose.ui.platform.LocalDensity.current) { ShufflePlayBlockHeight.toPx() }
    // null = hidden, otherwise top offset in px
    val topPx by androidx.compose.runtime.remember(listState, blockPx) {
        androidx.compose.runtime.derivedStateOf<Int?> {
            val info = listState.layoutInfo
            // Centre the pill exactly on the bottom edge of the top bar (half over the bar, half over
            // the content). Use the bar's measured height; fall back to the list's content padding.
            val barBottom = SpotifyBarMetrics.heightPx.takeIf { it > 0 } ?: info.beforeContentPadding
            val stick = barBottom - blockPx.toInt() / 2
            val header = info.visibleItemsInfo.firstOrNull { it.key == headerKey }
            when {
                header != null -> maxOf(stick, header.offset + header.size + info.beforeContentPadding - blockPx.toInt())
                listState.firstVisibleItemIndex > 0 -> stick
                else -> null
            }
        }
    }
    val y = topPx
    if (visible && y != null) {
        Box(
            modifier =
            Modifier
            .zIndex(1f) // draw over the top bar
            .offset { androidx.compose.ui.unit.IntOffset(0, y) }
            .fillMaxWidth()
            .height(ShufflePlayBlockHeight),
            contentAlignment = Alignment.Center,
        ) {
            ShufflePlayButton(onClick = onClick)
        }
    }
}
