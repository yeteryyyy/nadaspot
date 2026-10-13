/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.metrolist.music.LocalNavController
import com.metrolist.music.constants.AlbumViewTypeKey
import com.metrolist.music.constants.ChipSortTypeKey
import com.metrolist.music.constants.LibraryFilter
import com.metrolist.music.constants.LibraryViewType
import com.metrolist.music.utils.rememberEnumPreference

@Composable
fun LibraryScreen() {
    val navController = LocalNavController.current
    var filterType by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
    var libraryViewType by rememberEnumPreference(AlbumViewTypeKey, LibraryViewType.GRID)
    var showMix by rememberSaveable { mutableStateOf(false) }

    // Spotify 2018: the library is a plain list; no filter chips
    val noFilters = @Composable {}

    BackHandler(enabled = filterType != LibraryFilter.LIBRARY || showMix) {
        filterType = LibraryFilter.LIBRARY
        showMix = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showMix -> LibraryMixScreen(
                navController = navController,
                filterContent = noFilters,
                viewType = libraryViewType,
                onViewTypeChange = { libraryViewType = it },
            )
            else -> when (filterType) {
                LibraryFilter.LIBRARY, LibraryFilter.PODCASTS -> SpotifyLibraryHome(
                    onMadeForYou = { showMix = true },
                    onPlaylists = { filterType = LibraryFilter.PLAYLISTS },
                    onSongs = { filterType = LibraryFilter.SONGS },
                    onAlbums = { filterType = LibraryFilter.ALBUMS },
                    onArtists = { filterType = LibraryFilter.ARTISTS },
                )
                LibraryFilter.PLAYLISTS -> LibraryPlaylistsScreen(
                    navController = navController,
                    filterContent = noFilters,
                    viewType = LibraryViewType.LIST,
                    onViewTypeChange = {},
                )
                LibraryFilter.SONGS -> LibrarySongsScreen(
                    navController,
                    { filterType = LibraryFilter.LIBRARY },
                )
                LibraryFilter.ALBUMS -> LibraryAlbumsScreen(
                    navController,
                    { filterType = LibraryFilter.LIBRARY },
                )
                LibraryFilter.ARTISTS -> LibraryArtistsScreen(
                    navController,
                    { filterType = LibraryFilter.LIBRARY },
                )
            }
        }
    }
}
