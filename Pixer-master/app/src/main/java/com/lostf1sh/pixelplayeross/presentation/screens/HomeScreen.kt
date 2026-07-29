package com.lostf1sh.pixelplayeross.presentation.screens

import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafely
import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafelyReplacing

import android.content.Intent
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.preferences.CollagePattern
import com.lostf1sh.pixelplayeross.presentation.components.AlbumArtCollage
import com.lostf1sh.pixelplayeross.presentation.components.BetaInfoBottomSheet
import com.lostf1sh.pixelplayeross.presentation.components.ChangelogBottomSheet
import com.lostf1sh.pixelplayeross.presentation.components.DailyMixSection
import com.lostf1sh.pixelplayeross.presentation.components.HomeGradientTopBar
import com.lostf1sh.pixelplayeross.presentation.components.HomeOptionsBottomSheet
import com.lostf1sh.pixelplayeross.presentation.components.MiniPlayerHeight
import com.lostf1sh.pixelplayeross.presentation.components.RecentlyPlayedSection
import com.lostf1sh.pixelplayeross.presentation.components.RecentlyPlayedSectionMinSongsToShow
import com.lostf1sh.pixelplayeross.presentation.components.SmartImage
import com.lostf1sh.pixelplayeross.presentation.components.StatsOverviewCard
import com.lostf1sh.pixelplayeross.presentation.components.resolveMainScreenBottomGradientHeight
import com.lostf1sh.pixelplayeross.presentation.model.collectRecentlyPlayedSongIds
import com.lostf1sh.pixelplayeross.presentation.model.mapRecentlyPlayedSongs
import com.lostf1sh.pixelplayeross.presentation.components.subcomps.PlayingEqIcon
import com.lostf1sh.pixelplayeross.presentation.navigation.Screen
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.SettingsViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.StatsViewModel
import com.lostf1sh.pixelplayeross.ui.theme.ExpTitleTypography
import com.lostf1sh.pixelplayeross.ui.theme.rememberAppTileShape
import com.lostf1sh.pixelplayeross.ui.theme.AppSkin
import com.lostf1sh.pixelplayeross.ui.theme.LocalAppSkin
import com.lostf1sh.pixelplayeross.ui.theme.gradientForFlowConfig
import com.lostf1sh.pixelplayeross.ui.theme.deezerFlowBrandGradient
import androidx.compose.foundation.combinedClickable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerTrack
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylist
import com.lostf1sh.pixelplayeross.presentation.screens.mapDeezerTrackToSong

private const val HomeLoadingPlaceholderMinDurationMillis = 1200L

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    paddingValuesParent: PaddingValues,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    libraryViewModel: com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel = hiltViewModel(androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity),
    onOpenSidebar: () -> Unit
) {
    val context = LocalContext.current
    val isBenchmarkMode = remember {
        (context as? android.app.Activity)?.intent?.getBooleanExtra("is_benchmark", false) ?: false
    }
    val statsViewModel: StatsViewModel = hiltViewModel()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val playbackHistory by playerViewModel.playbackHistory.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val flowConfigsState by libraryViewModel.deezerFlowConfigs.collectAsState()
    val playlists by libraryViewModel.deezerRecommendedPlaylists.collectAsState()
    val yourMixes by libraryViewModel.yourMixes.collectAsState()
    val yourMixSongs by libraryViewModel.yourMixSongs.collectAsState()
    val yourDiscovery by libraryViewModel.yourDiscovery.collectAsState()
    
    val flowConfigs = flowConfigsState?.data?.included ?: emptyList()
    val recommendedPlaylists = playlists?.data?.included ?: emptyList()
    val coroutineScope = rememberCoroutineScope()
    val recentSongIds = remember(playbackHistory) {
        collectRecentlyPlayedSongIds(
            playbackHistory = playbackHistory,
            maxItems = 64
        )
    }
    val recentlyPlayedSourceSongsInitialValue = remember(recentSongIds) {
        if (recentSongIds.isEmpty()) persistentListOf<Song>() else null
    }
    val recentlyPlayedSourceSongs by remember(recentSongIds, playerViewModel) {
        playerViewModel.observeSongs(recentSongIds)
            .map<List<Song>, List<Song>?> { it }
    }.collectAsStateWithLifecycle(initialValue = recentlyPlayedSourceSongsInitialValue)
    val latestRecentlyPlayedSongs = remember(playbackHistory, recentlyPlayedSourceSongs) {
        val sourceSongs = recentlyPlayedSourceSongs ?: return@remember emptyList()
        mapRecentlyPlayedSongs(
            playbackHistory = playbackHistory,
            songs = sourceSongs,
            maxItems = 64
        )
    }
    var recentlyPlayedSongs by rememberSaveable { mutableStateOf(latestRecentlyPlayedSongs) }
    val latestRecentlyPlayedSongsState = rememberUpdatedState(latestRecentlyPlayedSongs)

    LaunchedEffect(latestRecentlyPlayedSongs, lifecycleOwner) {
        val isHomeVisible = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (recentlyPlayedSongs.isEmpty() || !isHomeVisible) {
            recentlyPlayedSongs = latestRecentlyPlayedSongs
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                recentlyPlayedSongs = latestRecentlyPlayedSongsState.value
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val recentlyPlayedQueue = remember(recentlyPlayedSongs) {
        recentlyPlayedSongs.map { it.song }.toImmutableList()
    }

    ReportDrawnWhen {
        yourMixes.isNotEmpty() || isBenchmarkMode
    }

    val currentSong by remember(playerViewModel.stablePlayerState) {
        playerViewModel.stablePlayerState.map { it.currentSong }
    }.collectAsStateWithLifecycle(initialValue = null)

    val isShuffleEnabled by remember(playerViewModel.stablePlayerState) {
        playerViewModel.stablePlayerState
            .map { it.isShuffleEnabled }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = false)

    val bottomPadding = if (currentSong != null) MiniPlayerHeight else 0.dp
    val navBarCompactMode by playerViewModel.navBarCompactMode.collectAsStateWithLifecycle()
    val bottomGradientHeight = resolveMainScreenBottomGradientHeight(
        navBarCompactMode,
        LocalAppSkin.current == AppSkin.DEEZER
    )

    var showOptionsBottomSheet by remember { mutableStateOf(false) }
    var showChangelogBottomSheet by remember { mutableStateOf(false) }
    var showBetaInfoBottomSheet by remember { mutableStateOf(false) }
    var showStreamingProviderSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val betaSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    LocalContext.current

    val homeStatsOverview by statsViewModel.homeOverview.collectAsStateWithLifecycle()

    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val density = LocalDensity.current
    val scrollThresholdPx = remember(density) { with(density) { 180.dp.toPx() } }
    val isScrolledPastThreshold = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollThresholdPx }
    }

    var savedScrollIndex by rememberSaveable { mutableIntStateOf(0) }
    var savedScrollOffset by rememberSaveable { mutableIntStateOf(0) }
    var needsScrollRestore by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, listState) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                savedScrollIndex = listState.firstVisibleItemIndex
                savedScrollOffset = listState.firstVisibleItemScrollOffset
                needsScrollRestore = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(
        needsScrollRestore,
        yourMixes.isNotEmpty(),
        yourDiscovery != null,
        recentlyPlayedSongs.size,
        homeStatsOverview
    ) {
        if (!needsScrollRestore) return@LaunchedEffect
        val totalItems = listState.layoutInfo.totalItemsCount
        if (totalItems == 0) return@LaunchedEffect
        val targetIndex = savedScrollIndex.coerceIn(0, (totalItems - 1).coerceAtLeast(0))
        listState.scrollToItem(targetIndex, savedScrollOffset)
        needsScrollRestore = false
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    if (LocalAppSkin.current == AppSkin.DEEZER) {
        DeezerHomeLayout(
            navController = navController,
            paddingValuesParent = paddingValuesParent,
            playerViewModel = playerViewModel,
            libraryViewModel = libraryViewModel,
            flowConfigs = flowConfigs,
            recommendedPlaylists = recommendedPlaylists,
            yourMixes = yourMixes,
            yourDiscovery = yourDiscovery,
            recentlyPlayedSongs = recentlyPlayedSongs,
            recentlyPlayedQueue = recentlyPlayedQueue,
            currentSong = currentSong,
            homeStatsOverview = homeStatsOverview,
            listState = listState,
            bottomPadding = bottomPadding,
            bottomGradientHeight = bottomGradientHeight,
            coroutineScope = coroutineScope,
            onBellClick = { showChangelogBottomSheet = true },
            onBetaLongPress = { showBetaInfoBottomSheet = true }
        )
    } else {
        DefaultHomeLayout(
            navController = navController,
            paddingValuesParent = paddingValuesParent,
            playerViewModel = playerViewModel,
            libraryViewModel = libraryViewModel,
            isScrolledPastThreshold = isScrolledPastThreshold.value,
            listState = listState,
            flowConfigs = flowConfigs,
            coroutineScope = coroutineScope,
            yourMixes = yourMixes,
            isShuffleEnabled = isShuffleEnabled,
            recommendedPlaylists = recommendedPlaylists,
            yourDiscovery = yourDiscovery,
            recentlyPlayedSongs = recentlyPlayedSongs,
            recentlyPlayedQueue = recentlyPlayedQueue,
            currentSong = currentSong,
            homeStatsOverview = homeStatsOverview,
            bottomPadding = bottomPadding,
            bottomGradientHeight = bottomGradientHeight,
            onOpenSettings = { navController.navigateSafely(Screen.Settings.route) },
            onShowChangelog = { showChangelogBottomSheet = true },
            onShowBetaInfo = { showBetaInfoBottomSheet = true },
            onShowStreamingProvider = { showStreamingProviderSheet = true }
        )
    }
    if (showOptionsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsBottomSheet = false },
            sheetState = sheetState
        ) {
            HomeOptionsBottomSheet(
                onNavigateToMashup = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showOptionsBottomSheet = false
                            navController.navigateSafely(Screen.DJSpace.route)
                        }
                    }
                }
            )
        }
    }
    if (showChangelogBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChangelogBottomSheet = false },
            sheetState = sheetState
        ) {
            ChangelogBottomSheet()
        }
    }
    if (showBetaInfoBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBetaInfoBottomSheet = false },
            sheetState = betaSheetState,
        ) {
            BetaInfoBottomSheet()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun DefaultHomeLayout(
    navController: NavController,
    paddingValuesParent: PaddingValues,
    playerViewModel: PlayerViewModel,
    libraryViewModel: com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel,
    isScrolledPastThreshold: Boolean,
    listState: LazyListState,
    flowConfigs: List<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerMultiFlowConfig>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    yourMixes: List<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData>,
    isShuffleEnabled: Boolean,
    recommendedPlaylists: List<DeezerPlaylist>,
    yourDiscovery: com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData?,
    recentlyPlayedSongs: List<com.lostf1sh.pixelplayeross.presentation.model.RecentlyPlayedSongUiModel>,
    recentlyPlayedQueue: kotlinx.collections.immutable.ImmutableList<Song>,
    currentSong: Song?,
    homeStatsOverview: com.lostf1sh.pixelplayeross.data.stats.PlaybackStatsRepository.PlaybackStatsSummary?,
    bottomPadding: Dp,
    bottomGradientHeight: Dp,
    onOpenSettings: () -> Unit,
    onShowChangelog: () -> Unit,
    onShowBetaInfo: () -> Unit,
    onShowStreamingProvider: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HomeGradientTopBar(
                    onNavigationIconClick = onOpenSettings,
                    onMoreOptionsClick = onShowChangelog,
                    onBetaClick = onShowBetaInfo,
                    onStreamingClick = onShowStreamingProvider,
                    onMenuClick = {
                    },
                    isScrolled = isScrolledPastThreshold
                )
            }
        ) { innerPadding ->

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValuesParent.calculateBottomPadding()
                            + 38.dp + bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (flowConfigs.isNotEmpty()) {
                    item(
                        key = "deezer_flow_header",
                        contentType = "deezer_flow_header"
                    ) {
                        Text(
                            text = stringResource(R.string.home_flow_short),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item(
                        key = "deezer_flow_list",
                        contentType = "deezer_flow_list"
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(flowConfigs, key = { index, _ -> "flow_$index" }) { _, config ->
                                DeezerHomeFlowConfigItem(config = config, onClick = {
                                    coroutineScope.launch {
                                        val flowTracksResponse = libraryViewModel.getMultiFlowTracks(config.links?.self ?: return@launch)
                                        val flowTracksList = flowTracksResponse?.data?.included?.filter { it.type == "track" } ?: emptyList()
                                        if (flowTracksList.isNotEmpty()) {
                                            val song = mapDeezerTrackToSong(flowTracksList.first())
                                            val songsToPlay = flowTracksList.map { mapDeezerTrackToSong(it) }
                                            playerViewModel.playSongs(songsToPlay, song, config.attributes?.title ?: "Flow", null, config.links?.self)
                                        }
                                    }
                                })
                            }
                        }
                    }
                }

                item(
                    key = "your_mix_header",
                    contentType = "your_mix_header"
                ) {
                    YourMixHeader(
                        song = "Today, we made for you",
                        isShuffleEnabled = isShuffleEnabled,
                        onPlayShuffled = {
                            if (yourMixes.isNotEmpty()) {
                                val randomMix = yourMixes.random()
                                val randomMixTracks = randomMix.included.map { mapDeezerTrackToSong(it) }
                                if (randomMixTracks.isNotEmpty()) {
                                    playerViewModel.playSongsShuffled(
                                        songsToPlay = randomMixTracks.toImmutableList(),
                                        queueName = randomMix.attributes?.title ?: randomMix.attributes?.name ?: "Your Mix",
                                        startAtZero = true
                                    )
                                }
                            }
                        }
                    )
                }
                item(
                    key = "your_mix_list",
                    contentType = "your_mix_list"
                ) {
                    if (yourMixes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(yourMixes, key = { index, _ -> "yourmix_$index" }) { _, mix ->
                                val playlist = com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylist(
                                    id = mix.id,
                                    attributes = mix.attributes
                                )
                                DeezerHomePlaylistItem(playlist = playlist, onClick = {
                                    navController.navigate(Screen.SmartTrackList.createRoute(mix.id))
                                })
                            }
                        }
                    }
                }

                if (recommendedPlaylists.isNotEmpty()) {
                    item(
                        key = "deezer_playlists_header",
                        contentType = "deezer_playlists_header"
                    ) {
                        Text(
                            text = stringResource(R.string.home_recommended_playlists),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item(
                        key = "deezer_playlists_list",
                        contentType = "deezer_playlists_list"
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(recommendedPlaylists, key = { index, _ -> "recommended_$index" }) { _, playlist ->
                                DeezerHomePlaylistItem(playlist = playlist, onClick = {
                                    navController.navigate(Screen.PlaylistDetail.createRoute("deezer_${playlist.id}"))
                                })
                            }
                        }
                    }
                }

                val discoveryTracks = yourDiscovery?.included?.map { mapDeezerTrackToSong(it) } ?: emptyList()
                if (discoveryTracks.isNotEmpty()) {
                    item(
                        key = "your_discovery_section",
                        contentType = "your_discovery_section"
                    ) {
                        DailyMixSection(
                            songs = discoveryTracks.toImmutableList(),
                            title = stringResource(R.string.home_your_discovery),
                            subtitle = yourDiscovery?.attributes?.description ?: "Based on your recent listening",
                            onClickOpen = {
                                navController.navigate(Screen.SmartTrackList.createRoute("discovery"))
                            },
                            onNavigateToAlbum = { song ->
                                navController.navigateSafelyReplacing(
                                    route = Screen.AlbumDetail.createRoute(song.albumId),
                                    patternToPop = Screen.AlbumDetail.route
                                )
                            },
                            onNavigateToArtist = { song ->
                                navController.navigateSafelyReplacing(
                                    route = Screen.ArtistDetail.createRoute(song.artistId),
                                    patternToPop = Screen.ArtistDetail.route
                                )
                            },
                            onNavigateToGenre = { song ->
                                song.genre?.let {
                                    navController.navigateSafely(Screen.GenreDetail.createRoute(java.net.URLEncoder.encode(it, "UTF-8")))
                                }
                            },
                            playerViewModel = playerViewModel
                        )
                    }
                }

                if (recentlyPlayedSongs.size >= RecentlyPlayedSectionMinSongsToShow) {
                    item(
                        key = "recently_played_section",
                        contentType = "recently_played_section"
                    ) {
                        RecentlyPlayedSection(
                            songs = recentlyPlayedSongs,
                            onSongClick = { song ->
                                if (recentlyPlayedQueue.isNotEmpty()) {
                                    playerViewModel.playSongs(
                                        songsToPlay = recentlyPlayedQueue,
                                        startSong = song,
                                        queueName = "Recently Played"
                                    )
                                }
                            },
                            onOpenAllClick = {
                                navController.navigateSafely(Screen.RecentlyPlayed.route)
                            },
                            themeStateHolder = playerViewModel.themeStateHolder,
                            currentSongId = currentSong?.id,
                            contentPadding = PaddingValues(start = 8.dp, end = 24.dp)
                        )
                    }
                }

                if (homeStatsOverview != null) {
                    item(
                        key = "listening_stats_preview",
                        contentType = "listening_stats_preview"
                    ) {
                        StatsOverviewCard(
                            summary = homeStatsOverview,
                            onClick = { navController.navigateSafely(Screen.Stats.route) }
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(bottomGradientHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.2f to Color.Transparent,
                            0.8f to MaterialTheme.colorScheme.surfaceContainerLowest,
                            1.0f to MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
        ) {

        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun YourMixLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(256.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            modifier = Modifier.size(128.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun YourMixEmptyPlaceholder(
    onRefresh: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 256.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = AbsoluteSmoothCornerShape(
                    cornerRadiusTL = 28.dp,
                    smoothnessAsPercentTR = 60,
                    cornerRadiusBR = 28.dp,
                    smoothnessAsPercentTL = 60,
                    cornerRadiusBL = 28.dp,
                    smoothnessAsPercentBR = 60,
                    cornerRadiusTR = 28.dp,
                    smoothnessAsPercentBL = 60,
                ),
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_empty_placeholder_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.home_empty_placeholder_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FilledTonalButton(
                onClick = onRefresh,
                shape = AbsoluteSmoothCornerShape(
                    cornerRadiusTL = 22.dp,
                    smoothnessAsPercentTR = 60,
                    cornerRadiusBR = 22.dp,
                    smoothnessAsPercentTL = 60,
                    cornerRadiusBL = 22.dp,
                    smoothnessAsPercentBR = 60,
                    cornerRadiusTR = 22.dp,
                    smoothnessAsPercentBL = 60,
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.home_empty_placeholder_refresh))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun YourMixHeader(
    song: String,
    isShuffleEnabled: Boolean = false,
    onPlayShuffled: () -> Unit
) {
    val buttonCorners = 68.dp
    val colors = MaterialTheme.colorScheme

    val titleStyle = rememberYourMixTitleStyle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.home_your_mix_title),
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
            )

            Text(
                text = song,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        LargeExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp),
            onClick = onPlayShuffled,
            containerColor = if (isShuffleEnabled) colors.primary else colors.tertiaryContainer,
            contentColor = if (isShuffleEnabled) colors.onPrimary else colors.onTertiaryContainer,
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTL = buttonCorners,
                smoothnessAsPercentTR = 60,
                cornerRadiusBR = buttonCorners,
                smoothnessAsPercentTL = 60,
                cornerRadiusBL = buttonCorners,
                smoothnessAsPercentBR = 60,
                cornerRadiusTR = buttonCorners,
                smoothnessAsPercentBL = 60,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_shuffle_24),
                contentDescription = stringResource(R.string.cd_shuffle_play),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}


@Composable
fun SongListItemFavs(
    modifier: Modifier = Modifier,
    cardCorners: Dp = 12.dp,
    title: String,
    artist: String,
    albumArtUrl: String?,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isCurrentSong) colors.primaryContainer.copy(alpha = 0.46f) else colors.surfaceContainer
    val contentColor = if (isCurrentSong) colors.primary else colors.onSurface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cardCorners),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmartImage(
                    model = albumArtUrl,
                    contentDescription = stringResource(R.string.cd_album_art_for_title, title),
                    contentScale = ContentScale.Crop,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist, style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            if (isCurrentSong) {
                PlayingEqIcon(
                    modifier = Modifier
                        .weight(0.1f)
                        .padding(start = 8.dp)
                        .size(width = 18.dp, height = 16.dp),
                    color = colors.primary,
                    isPlaying = isPlaying
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SongListItemFavsWrapper(
    song: Song,
    playerViewModel: PlayerViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()

    val isThisSongPlaying = remember(song.id, stablePlayerState.currentSong?.id, stablePlayerState.isPlaying) {
        song.id == stablePlayerState.currentSong?.id
    }

    SongListItemFavs(
        modifier = modifier,
        cardCorners = 0.dp,
        title = song.title,
        artist = song.displayArtist,
        albumArtUrl = song.albumArtUriString,
        isPlaying = stablePlayerState.isPlaying,
        isCurrentSong = song.id == stablePlayerState.currentSong?.id,
        onClick = onClick
    )
}


@OptIn(ExperimentalTextApi::class)
@Composable
private fun rememberYourMixTitleStyle(): TextStyle {
    return remember {
        TextStyle(
            fontFamily = FontFamily(
                Font(
                    resId = R.font.gflex_variable,
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(636),
                        FontVariation.width(152f),
                        FontVariation.Setting("ROND", 50f),
                        FontVariation.Setting("XTRA", 520f),
                        FontVariation.Setting("YOPQ", 90f),
                        FontVariation.Setting("YTLC", 505f)
                    )
                )
            ),
            fontWeight = FontWeight(760),
            fontSize = 64.sp,
            lineHeight = 62.sp
        )
    }
}

@Composable
fun DeezerHomeTrackItem(track: DeezerTrack, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        val imageUrl = track.attributes?.image?.medium ?: track.attributes?.image?.small ?: track.attributes?.image?.tiny
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.content_desc_track_artwork),
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.attributes?.title ?: "Unknown Track",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.attributes?.artistName ?: "Unknown Artist",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DeezerHomeFlowConfigItem(
    config: com.lostf1sh.pixelplayeross.data.network.deezer.DeezerMultiFlowConfig,
    isPinned: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val isDeezerSkin = LocalAppSkin.current == AppSkin.DEEZER
    val title = config.attributes?.title ?: "Flow"
    val isMainFlow = title.equals("Flow", ignoreCase = true)
    Column(
        modifier = Modifier
            .width(if (isDeezerSkin) 92.dp else 130.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        val images = config.attributes?.images
        val imageUrl = if (isDeezerSkin) {
            images?.circle?.medium ?: images?.circle?.small
                ?: images?.square?.medium ?: images?.square?.small
        } else {
            images?.square?.medium ?: images?.square?.small
        }
        Box(
            modifier = Modifier
                .size(if (isDeezerSkin) 92.dp else 130.dp)
                .let {
                    when {
                        !isDeezerSkin -> it
                        isMainFlow -> it.background(deezerFlowBrandGradient(), CircleShape)
                        else -> it.background(Color(0xFF1C1C1C), CircleShape)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!(isDeezerSkin && isMainFlow)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.content_desc_flow_artwork),
                    modifier = Modifier
                        .size(if (isDeezerSkin) 82.dp else 130.dp)
                        .clip(CircleShape),
                    contentScale = if (isDeezerSkin) ContentScale.Fit else ContentScale.Crop
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            if (isPinned) {
                Icon(
                    painter = painterResource(R.drawable.rounded_push_pin_24),
                    contentDescription = stringResource(R.string.content_desc_pinned),
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(if (isDeezerSkin) 14.dp else 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = if (isDeezerSkin) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = if (isDeezerSkin) TextAlign.Center else null,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = if (isDeezerSkin) Modifier.fillMaxWidth() else Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun DeezerHomePlaylistItem(
    playlist: DeezerPlaylist,
    isPinned: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box {
            AsyncImage(
                model = playlist.attributes?.image?.medium ?: playlist.attributes?.image?.small,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(rememberAppTileShape(16.dp))
            )
            if (isPinned) {
                Icon(
                    painter = painterResource(R.drawable.rounded_push_pin_24),
                    contentDescription = stringResource(R.string.content_desc_pinned),
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = playlist.attributes?.title ?: playlist.attributes?.name ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            minLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
