/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.withContext
import coil3.request.ImageRequest
import com.metrolist.music.ui.utils.resize
import com.metrolist.music.LocalNavController
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_EPISODE
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_PODCAST
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_PROFILE
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.metrolist.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.metrolist.innertube.YouTubeConstants
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.AutoRadioQueueKey
import com.metrolist.music.constants.HideVideoSongsKey
import com.metrolist.music.constants.MiniPlayerBottomSpacing
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.constants.NavigationBarHeight
import com.metrolist.music.constants.PauseSearchHistoryKey
import com.metrolist.music.db.entities.SearchHistory
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.ChipsRow
import com.metrolist.music.ui.component.EmptyPlaceholder
import com.metrolist.music.ui.component.HideOnScrollFAB
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.component.shimmer.ListItemPlaceHolder
import com.metrolist.music.ui.component.shimmer.ShimmerHost
import com.metrolist.music.ui.menu.YouTubeAlbumMenu
import com.metrolist.music.ui.menu.YouTubeArtistMenu
import com.metrolist.music.ui.menu.YouTubePlaylistMenu
import com.metrolist.music.ui.menu.YouTubeSongMenu
import com.metrolist.music.utils.SearchRoutes
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.viewmodels.OnlineSearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchResult(
    viewModel: OnlineSearchViewModel = hiltViewModel(),
    pureBlack: Boolean = false,
    savedStateHandle: SavedStateHandle? = null
) {
    val navController = LocalNavController.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scrollToTopCount by savedStateHandle
        ?.getStateFlow("scrollToTopCount", 0)
        ?.collectAsStateWithLifecycle(initialValue = 0) ?: remember { mutableIntStateOf(0) }

    var lastHandledCount by rememberSaveable { mutableIntStateOf(0) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(scrollToTopCount) {
        if (scrollToTopCount > lastHandledCount) {
            lastHandledCount = scrollToTopCount
            kotlinx.coroutines.delay(100)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
            }
            // Set focused AFTER requesting focus, not before

            isSearchFocused = true
        }
    }

    val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)
    val hideVideoSongs by rememberPreference(HideVideoSongsKey, defaultValue = false)
    val autoRadioQueue by rememberPreference(AutoRadioQueueKey, defaultValue = true)

    BackHandler(enabled = viewModel.filter.collectAsStateWithLifecycle().value != null) {
        viewModel.filter.value = null
    }

    BackHandler(enabled = isSearchFocused) {
        isSearchFocused = false
        focusManager.clearFocus()
    }

    // Extract query from navigation arguments
    val encodedQuery = navController.currentBackStackEntry?.arguments?.getString("query") ?: ""
    val decodedQuery = remember(encodedQuery) { SearchRoutes.decodeQuery(encodedQuery) }

    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(decodedQuery, TextRange(decodedQuery.length)))
    }

    val onSearch: (String) -> Unit =
        remember {
            { searchQuery ->
                if (searchQuery.isNotEmpty()) {
                    isSearchFocused = false
                    focusManager.clearFocus()

                    navController.navigate(SearchRoutes.resultRoute(searchQuery)) {
                        popUpTo(SearchRoutes.ROUTE) {
                            inclusive = true
                        }

                        if (!pauseSearchHistory) {
                            coroutineScope.launch(Dispatchers.IO) {
                                database.query {
                                    insert(SearchHistory(query = searchQuery))
                                }
                            }
                        }
                    }
                }
            }
        }

    // Update query when decodedQuery changes
    LaunchedEffect(decodedQuery) {
        query = TextFieldValue(decodedQuery, TextRange(decodedQuery.length))
    }

    // Clear video filter if hideVideoSongs setting is enabled and filter is set to FILTER_VIDEO
    LaunchedEffect(hideVideoSongs) {
        if (hideVideoSongs && viewModel.filter.value == FILTER_VIDEO) {
            viewModel.filter.value = null
        }
    }

    val searchFilter by viewModel.filter.collectAsStateWithLifecycle()
    val searchSummary = viewModel.summaryPage
    val itemsPage by remember(searchFilter) {
        derivedStateOf {
            searchFilter?.value?.let {
                viewModel.viewStateMap[it]
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    val ytItemContent: @Composable LazyItemScope.(YTItem) -> Unit = { item: YTItem ->
        val longClick = {
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
        }
        YouTubeListItem(
            item = item,
            isActive =
                when (item) {
                    is SongItem -> mediaMetadata?.id == item.id
                    is AlbumItem -> mediaMetadata?.album?.id == item.id
                    is EpisodeItem -> mediaMetadata?.id == item.id
                    else -> false
                },
            isPlaying = isPlaying,
            trailingContent = {
                if (item is SongItem || item is EpisodeItem) {
                    IconButton(
                        onClick = longClick,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null,
                        )
                    }
                } else {
                    // 2018 Spotify: artists, albums and playlists end in a thin ">" chevron
                    Icon(
                        painter = painterResource(R.drawable.spotify_chevron_right),
                        contentDescription = null,
                        tint = Color(0xFFB3B3B3),
                        modifier = Modifier.padding(end = 8.dp).size(32.dp),
                    )
                }
            },
            modifier =
                Modifier
                    .combinedClickable(
                        onClick = {
                            when (item) {
                                is SongItem -> {
                                    if (item.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
                                        playerConnection.playQueue(
                                            if (autoRadioQueue) {
                                                YouTubeQueue(
                                                    WatchEndpoint(videoId = item.id),
                                                    item.toMediaMetadata(),
                                                )
                                            } else {
                                                ListQueue(
                                                    title = item.title,
                                                    items = listOf(item.toMediaItem())
                                                )
                                            }
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
                                    if (item.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
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
                        onLongClick = longClick,
                    ).animateItem(),
        )
    }

    val pageBase = if (pureBlack) Color.Black else Color(0xFF121212)

    // Tint the header wash with the dominant colour of the top result's artwork
    val context = LocalContext.current
    val topResultThumbnail =
        searchSummary
            ?.summaries
            ?.firstOrNull { it.title == YouTubeConstants.DEFAULT_TOP_RESULT }
            ?.items
            ?.firstOrNull()
            ?.thumbnail
    var topTint by remember { mutableStateOf(Color(0xFF5F6E6A)) }
    LaunchedEffect(topResultThumbnail) {
        if (topResultThumbnail == null) {
            topTint = Color(0xFF5F6E6A)
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val result =
                    ImageLoader(context).execute(
                        ImageRequest
                            .Builder(context)
                            .data(topResultThumbnail.resize(200, 200))
                            .allowHardware(false)
                            .build(),
                    )
                val bmp = result.image?.toBitmap()
                if (bmp != null) {
                    val palette = Palette.from(bmp).generate()
                    val rgb = palette.getDarkVibrantColor(palette.getDarkMutedColor(palette.getDominantColor(0xFF5F6E6A.toInt())))
                    // keep it readable: pull it toward the page black a little
                    topTint = lerp(Color(rgb), pageBase, 0.25f)
                }
            } catch (e: Exception) {
            }
        }
    }
    val animatedTint by animateColorAsState(topTint, label = "searchTint")

    Box(modifier = Modifier.fillMaxSize().background(pageBase)) {
        // 2018 Spotify: a muted grey-green wash that scrolls away with the content
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .graphicsLayer {
                        translationY =
                            if (lazyListState.firstVisibleItemIndex == 0) {
                                -lazyListState.firstVisibleItemScrollOffset.toFloat()
                            } else {
                                -size.height
                            }
                    }.background(Brush.verticalGradient(listOf(animatedTint, pageBase))),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: "<"  query  "X" on a slightly lighter band
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.10f))
                        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        painter = painterResource(R.drawable.spotify_chevron_right),
                        contentDescription = stringResource(R.string.dismiss),
                        tint = Color.White,
                        modifier = Modifier.size(36.dp).graphicsLayer { rotationZ = 180f },
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle =
                        TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                        ),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query.text) }),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search),
                                    style = TextStyle(color = Color.White.copy(alpha = 0.6f), fontSize = 20.sp),
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isSearchFocused = true
                                }
                            },
                )
                if (query.text.isNotEmpty()) {
                    IconButton(onClick = { query = TextFieldValue("") }) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }

            // Main content area below search bar
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val visibleChips =
                        listOf(
                            null to stringResource(R.string.filter_all),
                            FILTER_SONG to stringResource(R.string.filter_songs),
                        ).let { baseChips ->
                            if (!hideVideoSongs) {
                                baseChips + (FILTER_VIDEO to stringResource(R.string.filter_videos))
                            } else {
                                baseChips
                            }
                        } +
                                listOf(
                                    FILTER_ALBUM to stringResource(R.string.filter_albums),
                                    FILTER_ARTIST to stringResource(R.string.filter_artists),
                                    FILTER_COMMUNITY_PLAYLIST to stringResource(R.string.filter_community_playlists),
                                    FILTER_FEATURED_PLAYLIST to stringResource(R.string.filter_featured_playlists),
                                    FILTER_PODCAST to stringResource(R.string.filter_podcasts),
                                    FILTER_EPISODE to stringResource(R.string.filter_episodes),
                                    FILTER_PROFILE to stringResource(R.string.filter_profiles),
                                )

                    // The filter chips are hidden; tapping a section header applies that filter instead.
                    val applyFilter: (YouTube.SearchFilter?) -> Unit = {
                        if (viewModel.filter.value != it) {
                            viewModel.filter.value = it
                        }
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        // reserve room for the mini player and nav bar so the last rows can scroll clear of them
                        contentPadding = LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom).asPaddingValues(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (searchFilter == null) {
                            // Order: Top result, then the Albums row, then everything else (stable sort)
                            searchSummary
                                ?.summaries
                                ?.sortedBy {
                                    when (it.title) {
                                        YouTubeConstants.DEFAULT_TOP_RESULT -> 0
                                        "Albums" -> 1
                                        else -> 2
                                    }
                                }?.forEach { summary ->
                                    item(key = "title/${summary.title}") {
                                        val target =
                                            when (summary.title) {
                                                "Songs" -> FILTER_SONG
                                                "Videos" -> FILTER_VIDEO
                                                "Albums" -> FILTER_ALBUM
                                                "Artists" -> FILTER_ARTIST
                                                "Playlists", "Community playlists" -> FILTER_COMMUNITY_PLAYLIST
                                                "Featured playlists" -> FILTER_FEATURED_PLAYLIST
                                                "Podcasts" -> FILTER_PODCAST
                                                "Episodes" -> FILTER_EPISODE
                                                "Profiles" -> FILTER_PROFILE
                                                else -> null
                                            }
                                        SpotifySectionTitle(
                                            title = summary.title.toLocalizedSearchSection(),
                                            onClick = target?.let { filter -> { applyFilter(filter) } },
                                        )
                                    }

                                    // The "All" view only previews each section
                                    val shown =
                                        when (summary.title) {
                                            YouTubeConstants.DEFAULT_TOP_RESULT -> summary.items.take(1)
                                            "Songs" -> summary.items.take(6)
                                            "Videos" -> summary.items.take(4)
                                            "Artists" -> summary.items.take(5)
                                            else -> summary.items
                                        }

                                    if (shown.isNotEmpty() && (shown.all { it is PlaylistItem } || shown.all { it is AlbumItem })) {
                                        // One horizontal row of cards (playlists, albums)
                                        item(key = "carousel/${summary.title}") {
                                            LazyRow(
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                items(items = shown, key = { it.id }) { card ->
                                                    when (card) {
                                                        is PlaylistItem ->
                                                            SpotifyMediaCard(
                                                                thumbnail = card.thumbnail,
                                                                title = card.title,
                                                                subtitle = null,
                                                                shuffleBadge = true,
                                                                onClick = { navController.navigate("online_playlist/${card.id}") },
                                                            )

                                                        is AlbumItem ->
                                                            SpotifyMediaCard(
                                                                thumbnail = card.thumbnail,
                                                                title = card.title,
                                                                subtitle = card.artists?.joinToString(", ") { it.name },
                                                                shuffleBadge = false,
                                                                onClick = { navController.navigate("album/${card.id}") },
                                                            )

                                                        else -> {}
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        itemsIndexed(
                                            items = shown,
                                            key = { index, item -> "${summary.title}/${item.id}/$index" },
                                            itemContent = { index, item -> ytItemContent(item) },
                                        )
                                    }
                                }

                            if (searchSummary?.summaries?.isEmpty() == true) {
                                item {
                                    EmptyPlaceholder(
                                        icon = R.drawable.search,
                                        text = stringResource(R.string.no_results_found),
                                    )
                                }
                            }
                        } else {
                            item(key = "filtered_title") {
                                SpotifySectionTitle(
                                    title = visibleChips.firstOrNull { it.first == searchFilter }?.second.orEmpty(),
                                    onClick = { applyFilter(null) },
                                )
                            }

                            items(
                                items = itemsPage?.items.orEmpty().distinctBy { it.id },
                                key = { "filtered_${it.id}" },
                                itemContent = ytItemContent,
                            )

                            if (itemsPage?.continuation != null) {
                                item(key = "loading") {
                                    ShimmerHost {
                                        repeat(3) {
                                            ListItemPlaceHolder()
                                        }
                                    }
                                }
                            }

                            if (itemsPage?.items?.isEmpty() == true) {
                                item {
                                    EmptyPlaceholder(
                                        icon = R.drawable.search,
                                        text = stringResource(R.string.no_results_found),
                                    )
                                }
                            }
                        }

                        if (searchFilter == null && searchSummary == null || searchFilter != null && itemsPage == null) {
                            item {
                                ShimmerHost {
                                    repeat(8) {
                                        ListItemPlaceHolder()
                                    }
                                }
                            }
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                if (isSearchFocused) {
                    OnlineSearchScreen(
                        query = query.text,
                        onQueryChange = { query = it },
                        onSearch = onSearch,
                        onDismiss = {
                            isSearchFocused = false
                            focusManager.clearFocus()
                        },
                        pureBlack = pureBlack,
                    )
                }
                HideOnScrollFAB(
                    lazyListState = lazyListState,
                    icon = R.drawable.mic,
                    onClick = { navController.navigate("recognition") },
                )
            }
        }
    }
}

@Composable
private fun String.toLocalizedSearchSection(): String =
    when (this) {
        YouTubeConstants.DEFAULT_TOP_RESULT -> stringResource(R.string.top_result)
        YouTubeConstants.DEFAULT_OTHER_RESULTS -> stringResource(R.string.other)
        "Songs" -> stringResource(R.string.songs)
        "Videos" -> stringResource(R.string.filter_videos)
        "Albums" -> stringResource(R.string.albums)
        "Artists" -> stringResource(R.string.artists)
        "Playlists" -> stringResource(R.string.playlists)
        "Podcasts" -> stringResource(R.string.filter_podcasts)
        "Episodes" -> stringResource(R.string.filter_episodes)
        "Profiles" -> stringResource(R.string.filter_profiles)
        else -> this
    }
/** Centered, bold white section heading ("Top result", "Artists", ...) like 2018 Spotify. */
@Composable
private fun SpotifySectionTitle(
    title: String,
    onClick: (() -> Unit)? = null,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Color.White,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 14.dp),
    )
}

/** Square art with an optional blue shuffle badge, bold title and optional grey subtitle. */
@Composable
private fun SpotifyMediaCard(
    thumbnail: String?,
    title: String,
    subtitle: String?,
    shuffleBadge: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(176.dp)
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(thumbnail?.resize(544, 544))
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF282828)),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp),
        ) {
            if (shuffleBadge) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2E77D0)),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.spotify_shuffle),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = if (shuffleBadge) 8.dp else 0.dp),
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB3B3B3),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}