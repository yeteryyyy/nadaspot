/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.artist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalArtistNameAliases
import com.metrolist.music.LocalListenTogetherManager
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.AppBarHeight
import com.metrolist.music.constants.HideExplicitKey
import com.metrolist.music.constants.ShowArtistDescriptionKey
import com.metrolist.music.constants.ShowArtistSubscriberCountKey
import com.metrolist.music.constants.ShowMonthlyListenersKey
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.AlbumGridItem
import com.metrolist.music.ui.component.ExpandableText
import com.metrolist.music.ui.component.HideOnScrollFAB
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.LinkSegment
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.ShufflePlayBlockHeight
import com.metrolist.music.ui.component.ShufflePlayOverlay
import com.metrolist.music.ui.component.SpotifyHeaderGradient
import com.metrolist.music.ui.component.SpotifyTopAppBar
import com.metrolist.music.ui.component.spotifyPressable
import com.metrolist.music.ui.theme.SpotifyFontFamily
import com.metrolist.music.ui.component.SongListItem
import com.metrolist.music.ui.component.YouTubeGridItem
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.component.shimmer.ButtonPlaceholder
import com.metrolist.music.ui.component.shimmer.ListItemPlaceHolder
import com.metrolist.music.ui.component.shimmer.ShimmerHost
import com.metrolist.music.ui.component.shimmer.TextPlaceholder
import com.metrolist.music.ui.menu.AlbumMenu
import com.metrolist.music.ui.menu.SongMenu
import com.metrolist.music.ui.menu.YouTubeAlbumMenu
import com.metrolist.music.ui.menu.YouTubeArtistMenu
import com.metrolist.music.ui.menu.YouTubePlaylistMenu
import com.metrolist.music.ui.menu.YouTubeSongMenu
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.ui.utils.fadingEdge
import com.metrolist.music.ui.utils.isScrollingUp
import com.metrolist.music.ui.utils.resize
import com.metrolist.music.utils.ArtistNameAliases
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.viewmodels.ArtistViewModel
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val listenTogetherManager = LocalListenTogetherManager.current
    val isGuest = listenTogetherManager?.isInRoom == true && !listenTogetherManager.isHost
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val artistPage = viewModel.artistPage
    val libraryArtist by viewModel.libraryArtist.collectAsStateWithLifecycle()
    val artistNameAliases = LocalArtistNameAliases.current
    val displayArtistName =
        ArtistNameAliases.resolve(
            artistNameAliases,
            viewModel.artistId,
            artistPage?.artist?.title ?: libraryArtist?.artist?.name.orEmpty(),
        ).ifEmpty { null }
    val librarySongs by viewModel.librarySongs.collectAsStateWithLifecycle()
    val libraryAlbums by viewModel.libraryAlbums.collectAsStateWithLifecycle()
    val isChannelSubscribed by viewModel.isChannelSubscribed.collectAsStateWithLifecycle()
    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
    val showArtistDescription by rememberPreference(key = ShowArtistDescriptionKey, defaultValue = true)
    val showArtistSubscriberCount by rememberPreference(key = ShowArtistSubscriberCountKey, defaultValue = true)
    val showMonthlyListeners by rememberPreference(key = ShowMonthlyListenersKey, defaultValue = true)

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLocal by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current

    // Calculate the offset value outside of the offset lambda
    val systemBarsTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val headerOffset =
        with(density) {
            -(systemBarsTopPadding + AppBarHeight).roundToPx()
        }

    val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl
    val heroHeight = if (thumbnail != null) 360.dp else 180.dp
    val heroThresholdPx = with(density) { (heroHeight - 72.dp).toPx() }
    // Top bar turns opaque (and shows the artist name) once the hero has scrolled away.
    val scrolledPastHero by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 ||
                lazyListState.firstVisibleItemScrollOffset > heroThresholdPx
        }
    }
    var songsExpanded by rememberSaveable { mutableStateOf(false) }
    val popularCount = 5
    val firstSongSectionIndex =
        artistPage?.sections?.indexOfFirst { (it.items.firstOrNull() as? SongItem)?.album != null } ?: -1
    val canShuffle =
        !isGuest &&
            if (showLocal) librarySongs.isNotEmpty() else artistPage?.artist?.shuffleEndpoint != null

    val distinctItemsBySection = remember(artistPage?.sections) {
        artistPage?.sections?.map { section ->
            section.items.distinctBy { it.id }
        } ?: emptyList()
    }

    LaunchedEffect(libraryArtist) {
        // always show local page for local artists. Show local page remote artist when offline
        showLocal = libraryArtist?.artist?.isLocal == true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (thumbnail == null && (artistPage != null || showLocal)) {
            SpotifyHeaderGradient(lazyListState)
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
        ) {
            if (artistPage == null && !showLocal) {
                item(key = "shimmer") {
                    ShimmerHost(
                        modifier =
                            Modifier
                                .offset {
                                    IntOffset(x = 0, y = headerOffset)
                                },
                    ) {
                        // Artist Image Placeholder
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.1f),
                        ) {
                            Spacer(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .shimmer()
                                        .background(MaterialTheme.colorScheme.onSurface)
                                        .fadingEdge(
                                            top = systemBarsTopPadding + AppBarHeight,
                                            bottom = 200.dp,
                                        ),
                            )
                        }
                        // Artist Name and Controls Section
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                        ) {
                            // Artist Name Placeholder
                            TextPlaceholder(
                                height = 36.dp,
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.7f)
                                        .padding(bottom = 16.dp),
                            )

                            // Buttons Row Placeholder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Subscribe Button Placeholder
                                ButtonPlaceholder(
                                    modifier =
                                        Modifier
                                            .width(120.dp)
                                            .height(40.dp),
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Right side buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Radio Button Placeholder
                                    ButtonPlaceholder(
                                        modifier =
                                            Modifier
                                                .width(100.dp)
                                                .height(40.dp),
                                    )

                                    // Shuffle Button Placeholder
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .shimmer()
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface,
                                                    RoundedCornerShape(24.dp),
                                                ),
                                    )
                                }
                            }
                        }
                        // Songs List Placeholder
                        repeat(6) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            } else {
                item(key = "header") {
                    val topPad = systemBarsTopPadding + AppBarHeight
                    val topInsetPx = with(density) { topPad.roundToPx() }
                    val monthlyListeners = artistPage?.monthlyListenerCount
                    val subscriberCount = artistPage?.subscriberCountText
                    val listenersText =
                        when {
                            showLocal -> null
                            showMonthlyListeners && !monthlyListeners.isNullOrEmpty() -> monthlyListeners
                            showArtistSubscriberCount && !subscriberCount.isNullOrEmpty() -> subscriberCount
                            else -> null
                        }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (thumbnail != null) {
                                // Full-bleed hero: extends up under the (transparent) top bar. The layout
                                // hides that extra height from the list so the SHUFFLE PLAY overlay lines up.
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(heroHeight + topPad)
                                            .layout { measurable, constraints ->
                                                val placeable = measurable.measure(constraints)
                                                layout(placeable.width, placeable.height - topInsetPx) {
                                                    placeable.place(0, -topInsetPx)
                                                }
                                            },
                                ) {
                                    AsyncImage(
                                        model = thumbnail.resize(1200, 1200),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        0f to Color.Black.copy(alpha = 0.45f),
                                                        0.28f to Color.Transparent,
                                                        0.5f to Color.Transparent,
                                                        1f to MaterialTheme.colorScheme.surface,
                                                    ),
                                                ),
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.fillMaxWidth().height(heroHeight))
                            }

                            // Artist name + listeners, centred at the bottom of the hero
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 16.dp),
                            ) {
                                Text(
                                    text = displayArtistName ?: "Unknown",
                                    fontFamily = SpotifyFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 40.sp,
                                    lineHeight = 44.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (listenersText != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = listenersText.uppercase(),
                                        fontFamily = SpotifyFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        // Room for the SHUFFLE PLAY pill that ShufflePlayOverlay draws here
                        Spacer(modifier = Modifier.height(if (canShuffle) ShufflePlayBlockHeight else 8.dp))
                    }
                }

                // FOLLOW / RADIO pills
                item(key = "actions") {
                    val radioEndpoint = if (!showLocal && !isGuest) artistPage?.artist?.radioEndpoint else null
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 20.dp)
                                .animateItem(),
                    ) {
                        SpotifyOutlinePill(
                            text = stringResource(if (isChannelSubscribed) R.string.spotify_following else R.string.spotify_follow),
                            highlighted = isChannelSubscribed,
                            onClick = { viewModel.toggleChannelSubscription() },
                        )
                        if (radioEndpoint != null) {
                            SpotifyOutlinePill(
                                text = stringResource(R.string.radio).uppercase(),
                                onClick = { playerConnection.playQueue(YouTubeQueue(radioEndpoint)) },
                            )
                        }
                    }
                }

                // About Artist Section
                if (!showLocal && showArtistDescription) {
                    val description = artistPage?.description
                    val descriptionRuns = artistPage?.descriptionRuns
                    val subscriberCount = artistPage?.subscriberCountText

                    if (!description.isNullOrEmpty() || !descriptionRuns.isNullOrEmpty()) {
                        item(key = "about_artist") {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp)
                                        .animateItem(),
                            ) {
                                ArtistSectionTitle(title = stringResource(R.string.about_artist))
                                Column(
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainer)
                                            .padding(16.dp),
                                ) {
                                    if (showArtistSubscriberCount && !subscriberCount.isNullOrEmpty()) {
                                        Text(
                                            text = subscriberCount.uppercase(),
                                            fontFamily = SpotifyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp),
                                        )
                                    }
                                    ExpandableText(
                                        text = description.orEmpty(),
                                        runs =
                                            descriptionRuns?.map {
                                                LinkSegment(
                                                    text = it.text,
                                                    url = it.navigationEndpoint?.urlEndpoint?.url,
                                                )
                                            },
                                        collapsedMaxLines = 4,
                                    )
                                }
                            }
                        }
                    }
                }

                // Show loading shimmer for sections when API hasn't returned yet
                if (artistPage == null && !showLocal) {
                    item(key = "section_shimmer") {
                        ShimmerHost {
                            repeat(4) { ListItemPlaceHolder() }
                        }
                    }
                }

                if (showLocal) {
                    if (librarySongs.isNotEmpty()) {
                        item(key = "local_songs_title") {
                            ArtistSectionTitle(
                                title = stringResource(R.string.popular),
                                modifier = Modifier.animateItem(),
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/songs")
                                },
                            )
                        }

                        val filteredLibrarySongs =
                            if (hideExplicit) {
                                librarySongs.filter { !it.song.explicit }
                            } else {
                                librarySongs
                            }
                        itemsIndexed(
                            items = if (songsExpanded) filteredLibrarySongs else filteredLibrarySongs.take(popularCount),
                            key = { index, item -> "local_song_${item.id}_$index" },
                        ) { index, song ->
                            SongListItem(
                                song = song,
                                albumIndex = index + 1,
                                showInLibraryIcon = true,
                                isActive = song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (!isGuest) {
                                                    if (song.id == mediaMetadata?.id) {
                                                        playerConnection.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            ListQueue(
                                                                title = displayArtistName ?: "Unknown Artist",
                                                                items = librarySongs.map { it.toMediaItem() },
                                                                startIndex = index,
                                                            ),
                                                        )
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ).animateItem(),
                            )
                        }
                    }

                    if (librarySongs.size > popularCount) {
                        item(key = "songs_toggle") {
                            SeeMoreToggle(expanded = songsExpanded, onClick = { songsExpanded = !songsExpanded })
                        }
                    }

                    if (libraryAlbums.isNotEmpty()) {
                        item(key = "local_albums_title") {
                            ArtistSectionTitle(
                                title = stringResource(R.string.albums),
                                modifier = Modifier.animateItem(),
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/albums")
                                },
                            )
                        }

                        item(key = "local_albums_list") {
                            val filteredLibraryAlbums =
                                if (hideExplicit) {
                                    libraryAlbums.filter { !it.album.explicit }
                                } else {
                                    libraryAlbums
                                }
                            LazyRow(
                                contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                            ) {
                                items(
                                    items = filteredLibraryAlbums,
                                    key = { "local_album_${it.id}_${filteredLibraryAlbums.indexOf(it)}" },
                                ) { album ->
                                    AlbumGridItem(
                                        album = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("album/${album.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            AlbumMenu(
                                                                originalAlbum = album,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItem(),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    artistPage?.sections?.forEachIndexed { index, section ->
                        if (section.items.isNotEmpty()) {
                            item(key = "section_${section.title}") {
                                ArtistSectionTitle(
                                    title = if (index == firstSongSectionIndex) stringResource(R.string.popular) else section.title,
                                    modifier = Modifier.animateItem(),
                                    onClick =
                                        section.moreEndpoint?.let {
                                            {
                                                navController.navigate(
                                                    "artist/${viewModel.artistId}/items?browseId=${it.browseId}?params=${it.params}",
                                                )
                                            }
                                        },
                                )
                            }
                        }

                        if ((section.items.firstOrNull() as? SongItem)?.album != null) {
                            val allSongs = distinctItemsBySection.getOrNull(index) ?: section.items
                            val isPopular = index == firstSongSectionIndex
                            itemsIndexed(
                                items = if (isPopular && !songsExpanded) allSongs.take(popularCount) else allSongs,
                                key = { _, it -> "youtube_song_${it.id}" },
                            ) { songIndex, song ->
                                YouTubeListItem(
                                    item = song as SongItem,
                                    albumIndex = if (isPopular) songIndex + 1 else null,
                                    isActive = mediaMetadata?.id == song.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    YouTubeSongMenu(
                                                        song = song,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    if (!isGuest) {
                                                        if (song.id == mediaMetadata?.id) {
                                                            playerConnection.togglePlayPause()
                                                        } else {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    WatchEndpoint(videoId = song.id),
                                                                    song.toMediaMetadata(),
                                                                ),
                                                            )
                                                        }
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            ).animateItem(),
                                )
                            }
                            if (index == firstSongSectionIndex &&
                                (distinctItemsBySection.getOrNull(index) ?: section.items).size > popularCount
                            ) {
                                item(key = "songs_toggle") {
                                    SeeMoreToggle(expanded = songsExpanded, onClick = { songsExpanded = !songsExpanded })
                                }
                            }
                        } else {
                            item(key = "section_list_${section.title}") {
                                LazyRow(
                                    contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                                ) {
                                    items(
                                        items = distinctItemsBySection.getOrNull(index) ?: section.items,
                                        key = { "youtube_album_${it.id}" },
                                    ) { item ->
                                        YouTubeGridItem(
                                            item = item,
                                            isActive =
                                                when (item) {
                                                    is SongItem -> mediaMetadata?.id == item.id
                                                    is AlbumItem -> mediaMetadata?.album?.id == item.id
                                                    else -> false
                                                },
                                            isPlaying = isPlaying,
                                            coroutineScope = coroutineScope,
                                            thumbnailRatio = 1f, // Use square thumbnails for all items in horizontal scroll
                                            modifier =
                                                Modifier
                                                    .combinedClickable(
                                                        onClick = {
                                                            when (item) {
                                                                is SongItem -> {
                                                                    if (!isGuest) {
                                                                        playerConnection.playQueue(
                                                                            YouTubeQueue(
                                                                                WatchEndpoint(videoId = item.id),
                                                                                item.toMediaMetadata(),
                                                                            ),
                                                                        )
                                                                    }
                                                                }

                                                                is AlbumItem -> {
                                                                    navController.navigate("album/${item.id}")
                                                                }

                                                                is ArtistItem -> {
                                                                    navController.navigate("artist/${item.id}")
                                                                }

                                                                is PlaylistItem -> {
                                                                    navController.navigate("online_playlist/${item.id}")
                                                                }

                                                                is PodcastItem -> {
                                                                    navController.navigate("online_podcast/${item.id}")
                                                                }

                                                                is EpisodeItem -> {
                                                                    if (!isGuest) {
                                                                        playerConnection.playQueue(
                                                                            YouTubeQueue(
                                                                                WatchEndpoint(videoId = item.id),
                                                                                item.toMediaMetadata(),
                                                                            ),
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        onLongClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            menuState.show {
                                                                when (item) {
                                                                    is SongItem -> {
                                                                        YouTubeSongMenu(
                                                                            song = item,
                                                                            onDismiss = menuState::dismiss,
                                                                        )
                                                                    }

                                                                    is AlbumItem -> {
                                                                        YouTubeAlbumMenu(
                                                                            albumItem = item,
                                                                            onDismiss = menuState::dismiss,
                                                                        )
                                                                    }

                                                                    is ArtistItem -> {
                                                                        YouTubeArtistMenu(
                                                                            artist = item,
                                                                            onDismiss = menuState::dismiss,
                                                                        )
                                                                    }

                                                                    is PlaylistItem -> {
                                                                        YouTubePlaylistMenu(
                                                                            playlist = item,
                                                                            coroutineScope = coroutineScope,
                                                                            onDismiss = menuState::dismiss,
                                                                        )
                                                                    }

                                                                    is PodcastItem -> {
                                                                        YouTubePlaylistMenu(
                                                                            playlist = item.asPlaylistItem(),
                                                                            coroutineScope = coroutineScope,
                                                                            onDismiss = menuState::dismiss,
                                                                        )
                                                                    }

                                                                    is EpisodeItem -> {
                                                                        YouTubeSongMenu(
                                                                            song = item.asSongItem(),
                                                                            onDismiss = menuState::dismiss,
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        },
                                                    ).animateItem(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val showLocalFab = librarySongs.isNotEmpty() && libraryArtist?.artist?.isLocal != true

        // Library/Local Toggle FAB
        HideOnScrollFAB(
            visible = showLocalFab,
            lazyListState = lazyListState,
            icon = if (showLocal) R.drawable.language else R.drawable.library_music,
            onClick = {
                showLocal = showLocal.not()
                if (!showLocal && artistPage == null) viewModel.fetchArtistsFromYTM()
            },
        )

        // Green SHUFFLE PLAY pill: rides at the bottom of the hero, then sticks under the top bar
        ShufflePlayOverlay(listState = lazyListState, visible = canShuffle, headerKey = "header") {
            if (!isGuest) {
                if (showLocal) {
                    if (librarySongs.isNotEmpty()) {
                        playerConnection.playQueue(
                            ListQueue(
                                title = displayArtistName ?: "Unknown Artist",
                                items = librarySongs.shuffled().map { it.toMediaItem() },
                            ),
                        )
                    }
                } else {
                    artistPage?.artist?.shuffleEndpoint?.let { playerConnection.playQueue(YouTubeQueue(it)) }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                    .align(Alignment.BottomCenter),
        )
    }

    SpotifyTopAppBar(
        scrolled = scrolledPastHero,
        title = {
            if (scrolledPastHero) {
                Text(
                    text = displayArtistName.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    viewModel.artistPage?.artist?.shareLink?.let { link ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Artist Link", link)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(
                    painterResource(R.drawable.link),
                    contentDescription = null,
                )
            }
        },
    )
}

/** Left-aligned bold section header (Popular, Albums, About ...) with an optional "see all" arrow. */
@Composable
private fun ArtistSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = title,
            fontFamily = SpotifyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onClick != null) {
            Icon(
                painter = painterResource(R.drawable.arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Small outlined pill (FOLLOW / FOLLOWING / RADIO) with the Spotify press-shrink effect. */
@Composable
private fun SpotifyOutlinePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .height(34.dp)
                .spotifyPressable(onClick = onClick)
                .border(
                    1.dp,
                    if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(percent = 50),
                ).padding(horizontal = 22.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = SpotifyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            maxLines = 1,
        )
    }
}

/** "SEE MORE" / "SEE LESS" text button under the Popular list. */
@Composable
private fun SeeMoreToggle(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(if (expanded) R.string.spotify_see_less else R.string.spotify_see_more),
            fontFamily = SpotifyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.spotifyPressable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
