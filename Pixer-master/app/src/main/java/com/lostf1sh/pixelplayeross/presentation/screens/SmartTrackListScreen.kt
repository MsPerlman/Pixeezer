package com.lostf1sh.pixelplayeross.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.components.SongInfoBottomSheet
import com.lostf1sh.pixelplayeross.presentation.components.ExpressiveScrollBar
import com.lostf1sh.pixelplayeross.presentation.components.MiniPlayerHeight
import com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel
import com.lostf1sh.pixelplayeross.utils.formatSongCount
import com.lostf1sh.pixelplayeross.utils.formatTotalDuration
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SmartTrackListScreen(
    listType: String,
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    navController: NavController,
    libraryViewModel: LibraryViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val yourMixes by libraryViewModel.yourMixes.collectAsState()
    val yourDiscovery by libraryViewModel.yourDiscovery.collectAsState()
    val playerStableState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()

    val mix = remember(listType, yourMixes) {
        if (listType != "discovery") yourMixes.find { it.id == listType } else null
    }

    val songs = remember(listType, mix, yourDiscovery) {
        if (listType == "discovery") {
            yourDiscovery?.included?.filter { it.type == "track" }?.map { mapDeezerTrackToSong(it) } ?: emptyList()
        } else {
            mix?.included?.filter { it.type == "track" }?.map { mapDeezerTrackToSong(it) } ?: emptyList()
        }
    }

    val title = remember(listType, mix, yourDiscovery) {
        if (listType == "discovery") {
            "Your Discovery"
        } else {
            if (listType.startsWith("your_mix_")) {
                val num = listType.substringAfterLast("_")
                "Your Mix $num"
            } else {
                mix?.attributes?.title ?: mix?.attributes?.name ?: "Your Mix"
            }
        }
    }
    
    val description = remember(listType, mix, yourDiscovery) {
        if (listType == "discovery") {
            yourDiscovery?.attributes?.title ?: yourDiscovery?.attributes?.description ?: "Based on your recent listening"
        } else {
            mix?.attributes?.title ?: mix?.attributes?.description ?: "A special mix inspired by your listening"
        }
    }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showSongInfoBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent
                ),
                subtitle = {
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val collapsed = scrollBehavior.state.collapsedFraction > 0.5f
                        if (!collapsed) {
                            Text(
                                text = stringResource(
                                    R.string.playlist_song_duration_line,
                                    formatSongCount(songs.size),
                                    formatTotalDuration(songs)
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        modifier = Modifier.padding(start = 10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = onBackClick
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (songs.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()), Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (songs.isNotEmpty()) {
                                playerViewModel.playSongs(songs, songs.first(), title)
                                if (playerStableState.isShuffleEnabled) playerViewModel.toggleShuffle()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        enabled = songs.isNotEmpty(),
                        shape = AbsoluteSmoothCornerShape(
                            cornerRadiusTL = 60.dp,
                            smoothnessAsPercentTR = 60,
                            cornerRadiusTR = 14.dp,
                            smoothnessAsPercentTL = 60,
                            cornerRadiusBL = 60.dp,
                            smoothnessAsPercentBR = 60,
                            cornerRadiusBR = 14.dp,
                            smoothnessAsPercentBL = 60
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.common_play),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            text = stringResource(R.string.playlist_action_play_it),
                            modifier = Modifier.padding(end = 4.dp),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            lineHeight = 20.sp
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            if (songs.isNotEmpty()) {
                                playerViewModel.playSongsShuffled(
                                    songsToPlay = songs,
                                    queueName = title,
                                    playlistId = listType,
                                    startAtZero = true,
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        enabled = songs.isNotEmpty(),
                        shape = AbsoluteSmoothCornerShape(
                            cornerRadiusTL = 14.dp,
                            smoothnessAsPercentTR = 60,
                            cornerRadiusTR = 60.dp,
                            smoothnessAsPercentTL = 60,
                            cornerRadiusBL = 14.dp,
                            smoothnessAsPercentBR = 60,
                            cornerRadiusBR = 60.dp,
                            smoothnessAsPercentBL = 60
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            contentDescription = stringResource(R.string.common_shuffle),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            text = stringResource(R.string.common_shuffle),
                            modifier = Modifier.padding(end = 4.dp),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            lineHeight = 20.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    val showScrollBar = true && (listState.canScrollForward || listState.canScrollBackward)
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                AbsoluteSmoothCornerShape(
                                    cornerRadiusTR = 32.dp,
                                    smoothnessAsPercentTR = 60,
                                    cornerRadiusTL = 32.dp,
                                    smoothnessAsPercentTL = 60,
                                )
                            )
                            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            bottom = MiniPlayerHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                            top = 12.dp,
                            end = 0.dp
                        ).let {
                            PaddingValues(
                                top = it.calculateTopPadding(),
                                bottom = it.calculateBottomPadding(),
                                start = 8.dp,
                                end = if (showScrollBar) 24.dp else 8.dp
                            )
                        }
                    ) {
                        itemsIndexed(songs, key = { index, song -> "${song.id}_$index" }) { _, song ->
                            LibraryPlaybackAwareSongItem(
                                song = song,
                                playerViewModel = playerViewModel,
                                onMoreOptionsClick = {
                                    playerViewModel.selectSongForInfo(song)
                                    showSongInfoBottomSheet = true
                                },
                                onClick = {
                                    playerViewModel.showAndPlaySong(song, songs, title)
                                }
                            )
                        }
                    }
                    
                    if (showScrollBar) {
                        ExpressiveScrollBar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(
                                    top = 12.dp,
                                    bottom = MiniPlayerHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                                ),
                            listState = listState,
                            paddingEnd = 6.dp
                        )
                    }
                }
            }
        }
    }

    if (showSongInfoBottomSheet) {
        val selectedSongForInfo by playerViewModel.selectedSongForInfo.collectAsStateWithLifecycle()
        selectedSongForInfo?.let { song ->
            SongInfoBottomSheet(
                song = song,
                isFavorite = false,
                onToggleFavorite = { },
                onDismiss = { showSongInfoBottomSheet = false },
                onPlaySong = { },
                onAddToQueue = { },
                onAddNextToQueue = { },
                onAddToPlayList = { },
                onDeleteFromDevice = { _, _, _ -> },
                onNavigateToAlbum = { },
                onNavigateToArtist = { },
                onNavigateToGenre = { },
                onEditSong = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
                removeFromListTrigger = { }
            )
        }
    }
}
