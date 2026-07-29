package com.lostf1sh.pixelplayeross.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.size.Size
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerMultiFlowConfig
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylist
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData
import com.lostf1sh.pixelplayeross.data.preferences.PinnedHomeItem
import com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType
import com.lostf1sh.pixelplayeross.data.stats.PlaybackStatsRepository
import com.lostf1sh.pixelplayeross.presentation.components.DailyMixSection
import com.lostf1sh.pixelplayeross.presentation.components.DeezerMixesInspiredSection
import com.lostf1sh.pixelplayeross.presentation.components.DeezerRecentlyPlayedRow
import com.lostf1sh.pixelplayeross.presentation.components.SmartImage
import com.lostf1sh.pixelplayeross.presentation.components.StatsOverviewCard
import com.lostf1sh.pixelplayeross.presentation.model.RecentlyPlayedSongUiModel
import com.lostf1sh.pixelplayeross.presentation.navigation.Screen
import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafely
import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafelyReplacing
import com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeezerHomeTopBar(
    avatarUrl: String?,
    homeLabel: String,
    onAvatarClick: () -> Unit,
    onBellClick: () -> Unit,
    onTitleLongPress: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmartImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.content_desc_account),
                    contentScale = ContentScale.Crop,
                    shape = CircleShape,
                    targetSize = Size(120, 120),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = homeLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onTitleLongPress)
                )
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 12.dp)) {
                Icon(
                    painter = painterResource(R.drawable.rounded_notifications_active_24),
                    contentDescription = stringResource(R.string.content_desc_notifications),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(onClick = onBellClick)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

private fun resolvePinnedTileClick(
    item: PinnedHomeItem,
    navController: NavController,
    flowConfigs: List<DeezerMultiFlowConfig>,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    coroutineScope: CoroutineScope,
) {
    when (item.type) {
        PinnedItemType.FLOW -> {
            val config = flowConfigs.firstOrNull { it.id == item.id } ?: return
            coroutineScope.launch {
                val flowTracksResponse = libraryViewModel.getMultiFlowTracks(config.links?.self ?: return@launch)
                val flowTracksList = flowTracksResponse?.data?.included?.filter { it.type == "track" } ?: emptyList()
                if (flowTracksList.isNotEmpty()) {
                    val song = mapDeezerTrackToSong(flowTracksList.first())
                    val songsToPlay = flowTracksList.map { mapDeezerTrackToSong(it) }
                    playerViewModel.playSongs(songsToPlay, song, config.attributes?.title ?: "Flow", null, config.links?.self)
                }
            }
        }
        PinnedItemType.DISCOVERY -> navController.navigate(Screen.SmartTrackList.createRoute("discovery"))
        PinnedItemType.MIX -> navController.navigate(Screen.SmartTrackList.createRoute(item.id))
        PinnedItemType.PLAYLIST -> {
            val route = if (item.id.startsWith("deezer_") || !item.id.all { it.isDigit() }) {
                item.id
            } else {
                "deezer_${item.id}"
            }
            navController.navigate(Screen.PlaylistDetail.createRoute(route))
        }
        PinnedItemType.ARTIST -> navController.navigate(Screen.ArtistDetail.createRoute(item.id))
        PinnedItemType.ALBUM -> navController.navigate(Screen.AlbumDetail.createRoute(item.id))
        PinnedItemType.TRACK -> {
            coroutineScope.launch {
                val song = playerViewModel.observeSongs(listOf(item.id)).firstOrNull()?.firstOrNull()
                if (song != null) {
                    playerViewModel.playSongs(listOf(song), song, item.label)
                }
            }
        }
    }
}

@Composable
fun DeezerPinnedGrid(
    items: List<PinnedHomeItem>,
    onTileClick: (PinnedHomeItem) -> Unit,
    onUnpin: (PinnedHomeItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val rows = items.take(8).chunked(4)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    DeezerPinnedTile(
                        item = item,
                        onClick = { onTileClick(item) },
                        onUnpin = { onUnpin(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DeezerPinnedTile(
    item: PinnedHomeItem,
    onClick: () -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileShape = when (item.type) {
        PinnedItemType.FLOW, PinnedItemType.ARTIST -> CircleShape
        else -> RoundedCornerShape(8.dp)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1C1C1C))
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .combinedClickable(onClick = onClick, onLongClick = onUnpin)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(tileShape)
            ) {
                when {
                    item.imageUrl != null -> SmartImage(
                        model = item.imageUrl,
                        contentDescription = item.label,
                        contentScale = ContentScale.Crop,
                        targetSize = Size(256, 256),
                        modifier = Modifier.fillMaxSize()
                    )
                    item.fallbackTrackArtUrls.size >= 4 -> Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            SmartImage(model = item.fallbackTrackArtUrls[0], contentDescription = null, contentScale = ContentScale.Crop, targetSize = Size(128, 128), modifier = Modifier.weight(1f).fillMaxHeight())
                            SmartImage(model = item.fallbackTrackArtUrls[1], contentDescription = null, contentScale = ContentScale.Crop, targetSize = Size(128, 128), modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            SmartImage(model = item.fallbackTrackArtUrls[2], contentDescription = null, contentScale = ContentScale.Crop, targetSize = Size(128, 128), modifier = Modifier.weight(1f).fillMaxHeight())
                            SmartImage(model = item.fallbackTrackArtUrls[3], contentDescription = null, contentScale = ContentScale.Crop, targetSize = Size(128, 128), modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                    else -> Box(
                        Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.label.take(1), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            Icon(
                painter = painterResource(R.drawable.rounded_push_pin_24),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(11.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun DeezerHomeLayout(
    navController: NavController,
    paddingValuesParent: PaddingValues,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    flowConfigs: List<DeezerMultiFlowConfig>,
    recommendedPlaylists: List<DeezerPlaylist>,
    yourMixes: List<DeezerPlaylistDetailData>,
    yourDiscovery: DeezerPlaylistDetailData?,
    recentlyPlayedSongs: List<RecentlyPlayedSongUiModel>,
    recentlyPlayedQueue: ImmutableList<Song>,
    currentSong: Song?,
    homeStatsOverview: PlaybackStatsRepository.PlaybackStatsSummary?,
    listState: LazyListState,
    bottomPadding: Dp,
    bottomGradientHeight: Dp,
    coroutineScope: CoroutineScope,
    onBellClick: () -> Unit,
    onBetaLongPress: () -> Unit,
) {
    val userProfile by libraryViewModel.userProfile.collectAsState()
    val pinnedHomeItems by libraryViewModel.pinnedHomeItems.collectAsState()

    fun isPinned(type: PinnedItemType, id: String) = pinnedHomeItems.any { it.type == type && it.id == id }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DeezerHomeTopBar(
                avatarUrl = userProfile?.pictureMedium,
                homeLabel = "Home",
                onAvatarClick = { navController.navigateSafely(Screen.Accounts.route) },
                onBellClick = onBellClick,
                onTitleLongPress = onBetaLongPress
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
                bottom = paddingValuesParent.calculateBottomPadding() + 38.dp + bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (pinnedHomeItems.isNotEmpty()) {
                item(key = "deezer_pinned_grid") {
                    DeezerPinnedGrid(
                        items = pinnedHomeItems,
                        onTileClick = { item ->
                            resolvePinnedTileClick(item, navController, flowConfigs, libraryViewModel, playerViewModel, coroutineScope)
                        },
                        onUnpin = { item -> libraryViewModel.togglePin(item) }
                    )
                }
            }

            if (yourMixes.isNotEmpty()) {
                item(key = "deezer_mixes_inspired") {
                    val rows = yourMixes.map { mix ->
                        val firstTrack = mix.included.firstOrNull()
                        com.lostf1sh.pixelplayeross.presentation.components.DeezerInspiredMixRow(
                            mixId = mix.id,
                            imageUrl = firstTrack?.attributes?.image?.medium
                                ?: firstTrack?.attributes?.image?.small
                                ?: mix.attributes?.image?.medium ?: mix.attributes?.image?.small,
                            title = firstTrack?.attributes?.title ?: mix.attributes?.title ?: mix.attributes?.name ?: "Mix",
                            subtitle = firstTrack?.attributes?.artistName ?: ""
                        )
                    }
                    DeezerMixesInspiredSection(
                        rows = rows,
                        onRowClick = { row -> navController.navigate(Screen.SmartTrackList.createRoute(row.mixId)) }
                    )
                }
            }

            if (recentlyPlayedSongs.isNotEmpty()) {
                item(key = "deezer_recently_played") {
                    DeezerRecentlyPlayedRow(
                        songs = recentlyPlayedSongs,
                        onSongClick = { song ->
                            if (recentlyPlayedQueue.isNotEmpty()) {
                                playerViewModel.playSongs(
                                    songsToPlay = recentlyPlayedQueue,
                                    startSong = song,
                                    queueName = "Recently Played"
                                )
                            }
                        }
                    )
                }
            }

            if (flowConfigs.isNotEmpty()) {
                item(key = "deezer_flow_mood_header") {
                    Text(
                        text = stringResource(R.string.home_flow_tagline),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item(key = "deezer_flow_mood_list") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(flowConfigs, key = { index, _ -> "flow_mood_$index" }) { _, config ->
                            DeezerHomeFlowConfigItem(
                                config = config,
                                isPinned = isPinned(PinnedItemType.FLOW, config.id),
                                onClick = {
                                    resolvePinnedTileClick(
                                        PinnedHomeItem(PinnedItemType.FLOW, config.id, config.attributes?.title ?: "Flow"),
                                        navController, flowConfigs, libraryViewModel, playerViewModel, coroutineScope
                                    )
                                },
                                onLongClick = {
                                    libraryViewModel.togglePin(
                                        PinnedHomeItem(
                                            type = PinnedItemType.FLOW,
                                            id = config.id,
                                            label = config.attributes?.title ?: "Flow",
                                            imageUrl = config.attributes?.images?.square?.medium ?: config.attributes?.images?.square?.small
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            val discoveryTracks = yourDiscovery?.included?.map { mapDeezerTrackToSong(it) } ?: emptyList()
            if (discoveryTracks.isNotEmpty()) {
                item(key = "deezer_your_discovery") {
                    DailyMixSection(
                        songs = discoveryTracks.toImmutableList(),
                        title = stringResource(R.string.home_your_discovery),
                        subtitle = yourDiscovery?.attributes?.description ?: "Based on your recent listening",
                        onClickOpen = { navController.navigate(Screen.SmartTrackList.createRoute("discovery")) },
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

            if (recommendedPlaylists.isNotEmpty()) {
                item(key = "deezer_recommended_header") {
                    Text(
                        text = stringResource(R.string.home_recommended_playlists),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item(key = "deezer_recommended_list") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(recommendedPlaylists, key = { index, _ -> "recommended_$index" }) { _, playlist ->
                            DeezerHomePlaylistItem(
                                playlist = playlist,
                                isPinned = isPinned(PinnedItemType.PLAYLIST, playlist.id),
                                onClick = { navController.navigate(Screen.PlaylistDetail.createRoute("deezer_${playlist.id}")) },
                                onLongClick = {
                                    libraryViewModel.togglePin(
                                        PinnedHomeItem(
                                            type = PinnedItemType.PLAYLIST,
                                            id = playlist.id,
                                            label = playlist.attributes?.title ?: playlist.attributes?.name ?: "Playlist",
                                            imageUrl = playlist.attributes?.image?.medium ?: playlist.attributes?.image?.small
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (homeStatsOverview != null) {
                item(key = "deezer_listening_stats") {
                    StatsOverviewCard(
                        summary = homeStatsOverview,
                        onClick = { navController.navigateSafely(Screen.Stats.route) }
                    )
                }
            }
        }
    }
}
