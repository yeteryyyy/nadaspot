/**
 * Spotify-2018 style "Your Library" landing list.
 */

package com.metrolist.music.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.ui.component.SongListItem
import com.metrolist.music.ui.theme.SpotifyFontFamily
import com.metrolist.music.ui.theme.SpotifyGrey

@Composable
fun SpotifyLibraryHome(
    onMadeForYou: () -> Unit,
    onPlaylists: () -> Unit,
    onSongs: () -> Unit,
    onAlbums: () -> Unit,
    onArtists: () -> Unit,
) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current
    val recent by database.recentlyPlayedSongs(30).collectAsStateWithLifecycle(initialValue = emptyList())
    val mediaMetadata = playerConnection?.mediaMetadata?.collectAsStateWithLifecycle(initialValue = null)?.value
    val isPlaying = playerConnection?.isPlaying?.collectAsStateWithLifecycle(initialValue = false)?.value == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
        item { LibraryRow(R.drawable.radio, stringResource(R.string.made_for_you), onMadeForYou) }
        item { LibraryRow(R.drawable.queue_music, stringResource(R.string.filter_playlists), onPlaylists) }
        item { LibraryRow(R.drawable.music_note, stringResource(R.string.filter_songs), onSongs) }
        item { LibraryRow(R.drawable.album, stringResource(R.string.filter_albums), onAlbums) }
        item { LibraryRow(R.drawable.artist, stringResource(R.string.filter_artists), onArtists) }

        if (recent.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.recently_played),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpotifyFontFamily,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                )
            }
            items(items = recent, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    showLikedIcon = false,
                    showDownloadIcon = false,
                    isActive = song.id == mediaMetadata?.id,
                    isPlaying = isPlaying,
                    isSwipeable = false,
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.navigate_next),
                            contentDescription = null,
                            tint = SpotifyGrey,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (song.id == mediaMetadata?.id) {
                                    playerConnection?.togglePlayPause()
                                } else {
                                    playerConnection?.playQueue(
                                        ListQueue(
                                            title = song.song.title,
                                            items = listOf(song.toMediaItem()),
                                        ),
                                    )
                                }
                            },
                )
            }
        }
    }
}

@Composable
private fun LibraryRow(
    icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = SpotifyGrey,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 17.sp,
            fontFamily = SpotifyFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.navigate_next),
            contentDescription = null,
            tint = SpotifyGrey,
            modifier = Modifier.size(24.dp),
        )
    }
}
