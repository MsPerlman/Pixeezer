package com.lostf1sh.pixelplayeross.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerTrack
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylist
import com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import com.lostf1sh.pixelplayeross.ui.theme.rememberAppTileShape
import androidx.compose.ui.draw.clip

@Composable
fun LibraryHomeTab(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp
) {
    val flowConfigsState by libraryViewModel.deezerFlowConfigs.collectAsState()
    val playlists by libraryViewModel.deezerRecommendedPlaylists.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomBarHeight)
    ) {
        val flowConfigs = flowConfigsState?.data?.included ?: emptyList()
        if (flowConfigs.isNotEmpty()) {
            Text(
                text = stringResource(R.string.home_flow_short),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(flowConfigs.size) { index ->
                    Text(text = flowConfigs[index].attributes?.title ?: "Flow")
                }
            }
        }

        val recommended = playlists?.data?.included ?: emptyList()
        if (recommended.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_recommended_playlists),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recommended) { playlist ->
                    DeezerPlaylistItem(playlist = playlist, onClick = {
                    })
                }
            }
        }
    }
}

@Composable
fun DeezerTrackItem(track: DeezerTrack, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = track.attributes?.image?.medium ?: track.attributes?.image?.small,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(AbsoluteSmoothCornerShape(16.dp, 60))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.attributes?.title ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.attributes?.artistName ?: "Unknown",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DeezerPlaylistItem(playlist: DeezerPlaylist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = playlist.attributes?.image?.medium ?: playlist.attributes?.image?.small,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(rememberAppTileShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = playlist.attributes?.name ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun mapDeezerTrackToSong(track: DeezerTrack): Song {
    return Song(
        id = track.id,
        title = track.attributes?.title ?: "Unknown",
        artist = track.attributes?.artistName ?: "Unknown",
        artistId = 0L,
        album = track.attributes?.albumName ?: "Unknown",
        albumId = 0L,
        duration = (track.attributes?.duration ?: 0) * 1000L,
        contentUriString = "deezer://track/${track.id}",
        path = "deezer://track/${track.id}",
        mimeType = "audio/mpeg",
        albumArtUriString = track.attributes?.image?.medium ?: track.attributes?.image?.large,
        highResAlbumArtUriString = track.attributes?.image?.large ?: track.attributes?.image?.full,
        bitrate = 320,
        sampleRate = 44100,
        isExplicit = track.attributes?.explicitLyrics == true || track.attributes?.explicitContentLyrics == 1 || track.attributes?.explicit == true
    )
}
