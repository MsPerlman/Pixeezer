package com.lostf1sh.pixelplayeross.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Playlist
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.components.MiniPlayerHeight
import com.lostf1sh.pixelplayeross.presentation.components.SmartImage

private val DeezerRed = Color(0xFFEF3E48)
private val DeezerHeartRed = Color(0xFFF0616E)

@Composable
fun DeezerPlaylistDetailContent(
    playlist: Playlist,
    songs: List<Song>,
    favoriteIds: Set<String>,
    canAddSongs: Boolean,
    onBackClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSortClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit
) {
    val listState = rememberLazyListState()
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 200 }
    }

    val collageArt = remember(songs) {
        songs.mapNotNull { it.albumArtUriString }.distinct().take(4)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = MiniPlayerHeight + navBarInset + 16.dp
            )
        ) {
            item(key = "header") {
                DeezerPlaylistHeader(
                    playlist = playlist,
                    collageArt = collageArt,
                    onShuffleClick = onShuffleClick,
                    onSortClick = onSortClick
                )
            }

            if (canAddSongs) {
                item(key = "add_songs") {
                    DeezerAddSongsRow(onClick = onAddSongsClick)
                }
            }

            items(songs, key = { it.id }) { song ->
                DeezerPlaylistTrackRow(
                    song = song,
                    isFavorite = favoriteIds.contains(song.id),
                    onClick = { onPlaySong(song) },
                    onFavoriteClick = { onToggleFavorite(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
        }

        DeezerPlaylistTopChrome(
            statusBarInset = statusBarInset,
            isScrolled = isScrolled,
            onBackClick = onBackClick,
            onOptionsClick = onOptionsClick,
            onShuffleClick = onShuffleClick
        )
    }
}

@Composable
private fun DeezerPlaylistTopChrome(
    statusBarInset: androidx.compose.ui.unit.Dp,
    isScrolled: Boolean,
    onBackClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isScrolled) Color.Black else Color.Transparent)
            .padding(top = statusBarInset)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeezerChromeIcon(
            iconRes = R.drawable.rounded_arrow_back_24,
            contentDescription = stringResource(R.string.content_desc_back),
            onClick = onBackClick
        )
        Spacer(Modifier.weight(1f))
        DeezerChromeIcon(
            iconRes = R.drawable.rounded_more_vert_24,
            contentDescription = stringResource(R.string.playlist_options_title),
            onClick = onOptionsClick
        )
        if (isScrolled) {
            Spacer(Modifier.width(4.dp))
            DeezerShuffleButton(size = 44.dp, onClick = onShuffleClick)
        }
    }
}

@Composable
private fun DeezerPlaylistHeader(
    playlist: Playlist,
    collageArt: List<String>,
    onShuffleClick: () -> Unit,
    onSortClick: () -> Unit
) {
    Column {
        DeezerCoverCollage(
            coverImageUri = playlist.coverImageUri,
            collageArt = collageArt
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        playlist.creatorName?.takeIf { it.isNotBlank() }?.let { creator ->
            Text(
                text = creator,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeezerChromeIcon(
                iconRes = R.drawable.rounded_filter_list_24,
                contentDescription = stringResource(R.string.content_desc_sort_tracks),
                onClick = onSortClick
            )
            Spacer(Modifier.weight(1f))
            DeezerShuffleButton(size = 56.dp, onClick = onShuffleClick)
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DeezerCoverCollage(
    coverImageUri: String?,
    collageArt: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        when {
            collageArt.size >= 4 -> {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        CollageCell(collageArt[0], Modifier.weight(1f).fillMaxSize())
                        CollageCell(collageArt[1], Modifier.weight(1f).fillMaxSize())
                    }
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        CollageCell(collageArt[2], Modifier.weight(1f).fillMaxSize())
                        CollageCell(collageArt[3], Modifier.weight(1f).fillMaxSize())
                    }
                }
            }
            else -> CollageCell(coverImageUri ?: collageArt.firstOrNull(), Modifier.fillMaxSize())
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun CollageCell(model: Any?, modifier: Modifier) {
    SmartImage(
        model = model,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
private fun DeezerAddSongsRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.presentation_batch_b_add_songs),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun DeezerPlaylistTrackRow(
    song: Song,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmartImage(
            model = song.albumArtUriString,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.displayArtist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onFavoriteClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) R.drawable.round_favorite_24 else R.drawable.round_favorite_border_24
                ),
                contentDescription = stringResource(R.string.content_desc_favorite),
                tint = if (isFavorite) DeezerHeartRed else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        DeezerChromeIcon(
            iconRes = R.drawable.rounded_more_vert_24,
            contentDescription = stringResource(R.string.content_desc_track_options),
            onClick = onMoreClick
        )
    }
}

@Composable
private fun DeezerShuffleButton(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(DeezerRed)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_shuffle_24),
            contentDescription = stringResource(R.string.content_desc_shuffle),
            tint = Color.White,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}

@Composable
private fun DeezerChromeIcon(
    iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
