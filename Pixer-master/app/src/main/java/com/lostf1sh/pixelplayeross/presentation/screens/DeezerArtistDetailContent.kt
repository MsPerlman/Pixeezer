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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Album
import com.lostf1sh.pixelplayeross.data.model.Artist
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.components.MiniPlayerHeight
import com.lostf1sh.pixelplayeross.presentation.components.SmartImage
import java.util.Locale

private val DeezerRed = Color(0xFFEF3E48)
private val DeezerHeartRed = Color(0xFFF0616E)
private val DeezerCardBg = Color(0xFF181818)

@Composable
fun DeezerArtistDetailContent(
    artistId: Long,
    artistName: String,
    artistImageUrl: String?,
    fans: Int,
    isLiked: Boolean,
    topTracks: List<Song>,
    albums: List<Album>,
    similarArtists: List<Artist>,
    currentSongId: String?,
    isPlaying: Boolean,
    favoriteIds: Set<String>,
    navBarHeight: Dp = 0.dp,
    onBackClick: () -> Unit,
    onToggleArtistLike: () -> Unit,
    onShareArtist: () -> Unit,
    onPlayArtist: () -> Unit,
    onPlayMix: () -> Unit,
    onTrackClick: (Song) -> Unit,
    onTrackFavorite: (Song) -> Unit,
    onTrackMore: (Song) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onSimilarArtistClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 300 }
    }

    var topTracksExpanded by remember { mutableStateOf(false) }
    val latestRelease = remember(albums) { albums.maxByOrNull { it.year } }
    val mixArt = remember(albums, topTracks) {
        (albums.mapNotNull { it.albumArtUriString } + topTracks.mapNotNull { it.albumArtUriString })
            .distinct().take(4)
    }

    val pinContext = androidx.compose.ui.platform.LocalContext.current
    val pinActivity = pinContext as? androidx.activity.ComponentActivity
    val libraryViewModelForPin = if (pinActivity != null) {
        androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel>(pinActivity)
    } else null
    val pinnedItemsForArtist by (libraryViewModelForPin?.pinnedHomeItems
        ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList()))
        .collectAsStateWithLifecycle()
    val isArtistPinned = pinnedItemsForArtist.any {
        it.type == com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType.ARTIST && it.id == artistId.toString()
    }
    val pinLimitMessage = stringResource(R.string.pin_limit_reached_message, com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel.MAX_PINNED_HOME_ITEMS)
    val onTogglePinArtist: () -> Unit = {
        val didToggle = libraryViewModelForPin?.togglePin(
            com.lostf1sh.pixelplayeross.data.preferences.PinnedHomeItem(
                type = com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType.ARTIST,
                id = artistId.toString(),
                label = artistName,
                imageUrl = artistImageUrl
            )
        ) ?: false
        if (!didToggle) {
            android.widget.Toast.makeText(pinContext, pinLimitMessage, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = MiniPlayerHeight + maxOf(navBarHeight, navBarInset) + 16.dp
            )
        ) {
            item(key = "header") {
                DeezerArtistHeader(
                    artistName = artistName,
                    artistImageUrl = artistImageUrl,
                    fans = fans,
                    isLiked = isLiked,
                    isPinned = isArtistPinned,
                    onToggleArtistLike = onToggleArtistLike,
                    onShareArtist = onShareArtist,
                    onTogglePin = onTogglePinArtist,
                    onPlayArtist = onPlayArtist
                )
            }

            if (topTracks.isNotEmpty()) {
                item(key = "top_titres_header") {
                    DeezerSectionHeader(title = stringResource(R.string.artist_top_tracks))
                }
                val visibleTop = topTracks.take(if (topTracksExpanded) 10 else 5)
                itemsIndexed(visibleTop, key = { _, song -> "top_${song.id}" }) { index, song ->
                    DeezerArtistTrackRow(
                        song = song,
                        rank = index + 1,
                        isFavorite = favoriteIds.contains(song.id),
                        isCurrent = song.id == currentSongId,
                        onClick = { onTrackClick(song) },
                        onFavoriteClick = { onTrackFavorite(song) },
                        onMoreClick = { onTrackMore(song) }
                    )
                }
                if (topTracks.size > 5) {
                    item(key = "top_titres_voir_tout") {
                        DeezerOutlinedButton(
                            text = if (topTracksExpanded) stringResource(R.string.see_less) else stringResource(R.string.see_all),
                            onClick = { topTracksExpanded = !topTracksExpanded }
                        )
                    }
                }
            }

            latestRelease?.let { release ->
                item(key = "latest_release") {
                    DeezerSectionHeader(title = stringResource(R.string.artist_latest_release))
                    DeezerLatestReleaseCard(
                        album = release,
                        artistName = artistName,
                        onClick = { onAlbumClick(release.id) }
                    )
                }
            }

            if (albums.isNotEmpty()) {
                item(key = "discographie") {
                    DeezerSectionHeader(title = stringResource(R.string.artist_discography), showChevron = true)
                    DeezerDiscographyRow(
                        albums = albums,
                        artistName = artistName,
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            if (topTracks.isNotEmpty()) {
                item(key = "mix") {
                    val mixTitle = stringResource(R.string.artist_mix_format, artistName)
                    DeezerSectionHeader(title = mixTitle)
                    DeezerMixCard(
                        title = mixTitle,
                        subtitle = buildMixSubtitle(artistName, similarArtists),
                        collageArt = mixArt,
                        onPlayClick = onPlayMix
                    )
                }
            }

            if (similarArtists.isNotEmpty()) {
                item(key = "similar") {
                    DeezerSectionHeader(title = stringResource(R.string.artist_similar_artists), showChevron = true)
                    DeezerSimilarArtistsRow(
                        artists = similarArtists,
                        onArtistClick = onSimilarArtistClick
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        DeezerArtistTopChrome(
            statusBarInset = statusBarInset,
            isScrolled = isScrolled,
            title = artistName,
            onBackClick = onBackClick,
            onPlayClick = onPlayArtist
        )
    }
}

@Composable
private fun DeezerArtistTopChrome(
    statusBarInset: Dp,
    isScrolled: Boolean,
    title: String,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isScrolled) Color.Black else Color.Transparent)
            .padding(top = statusBarInset)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeezerCircleIcon(
            iconRes = R.drawable.rounded_arrow_back_24,
            contentDescription = stringResource(R.string.content_desc_back),
            onClick = onBackClick
        )
        if (isScrolled) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            DeezerRedPlayButton(size = 44.dp, onClick = onPlayClick)
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun DeezerArtistHeader(
    artistName: String,
    artistImageUrl: String?,
    fans: Int,
    isLiked: Boolean,
    isPinned: Boolean,
    onToggleArtistLike: () -> Unit,
    onShareArtist: () -> Unit,
    onTogglePin: () -> Unit,
    onPlayArtist: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f)
        ) {
            SmartImage(
                model = artistImageUrl,
                contentDescription = artistName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    lineHeight = 44.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (fans > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${formatFans(fans)} fans",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeezerCircleIcon(
                painterIcon = if (isLiked) R.drawable.round_favorite_24 else R.drawable.round_favorite_border_24,
                tint = if (isLiked) DeezerHeartRed else Color.White,
                contentDescription = stringResource(R.string.content_desc_like),
                onClick = onToggleArtistLike
            )
            Spacer(Modifier.width(8.dp))
            DeezerCircleIconVector(
                onClick = onShareArtist,
                contentDescription = stringResource(R.string.content_desc_share)
            )
            Spacer(Modifier.width(8.dp))
            DeezerCircleIcon(
                iconRes = R.drawable.rounded_push_pin_24,
                tint = if (isPinned) DeezerRed else Color.White,
                contentDescription = stringResource(if (isPinned) R.string.content_desc_unpin_from_home else R.string.content_desc_pin_to_home),
                onClick = onTogglePin
            )
            Spacer(Modifier.weight(1f))
            DeezerRedPlayButton(size = 64.dp, onClick = onPlayArtist)
        }
    }
}

@Composable
private fun DeezerSectionHeader(
    title: String,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.rounded_arrow_back_24),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = -1f }
            )
        }
    }
}

@Composable
private fun DeezerArtistTrackRow(
    song: Song,
    rank: Int,
    isFavorite: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val titleColor = if (isCurrent) DeezerRed else Color.White
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
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$rank. ${song.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (song.isExplicit) {
                    Spacer(Modifier.width(6.dp))
                    ExplicitBadge()
                }
            }
            Text(
                text = song.album.ifBlank { song.displayArtist },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f),
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
        DeezerCircleIcon(
            iconRes = R.drawable.rounded_more_vert_24,
            contentDescription = stringResource(R.string.content_desc_track_options),
            onClick = onMoreClick
        )
    }
}

@Composable
private fun ExplicitBadge() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.explicit_marker),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun DeezerLatestReleaseCard(
    album: Album,
    artistName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeezerCardBg)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            SmartImage(
                model = album.albumArtUriString,
                contentDescription = album.title,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            PlayGlyphOverlay(size = 44.dp)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.by_artist_format, artistName),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = releaseLine(album),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeezerDiscographyRow(
    albums: List<Album>,
    artistName: String,
    onAlbumClick: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
    ) {
        items(albums, key = { "disco_${it.id}" }) { album ->
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .clickable { onAlbumClick(album.id) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    SmartImage(
                        model = album.albumArtUriString,
                        contentDescription = album.title,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    PlayGlyphOverlay(size = 48.dp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.by_artist_format, artistName),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = releaseLine(album),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DeezerMixCard(
    title: String,
    subtitle: String,
    collageArt: List<String>,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeezerCardBg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            when {
                collageArt.size >= 4 -> {
                    Column(Modifier.fillMaxSize()) {
                        Row(Modifier.fillMaxWidth().weight(1f)) {
                            SmartImage(collageArt[0], null, Modifier.weight(1f).fillMaxSize())
                            SmartImage(collageArt[1], null, Modifier.weight(1f).fillMaxSize())
                        }
                        Row(Modifier.fillMaxWidth().weight(1f)) {
                            SmartImage(collageArt[2], null, Modifier.weight(1f).fillMaxSize())
                            SmartImage(collageArt[3], null, Modifier.weight(1f).fillMaxSize())
                        }
                    }
                }
                else -> SmartImage(collageArt.firstOrNull(), null, Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .clickable(onClick = onPlayClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_play_arrow_filled_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "ÉCOUTER",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DeezerSimilarArtistsRow(
    artists: List<Artist>,
    onArtistClick: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
    ) {
        items(artists, key = { "similar_${it.id}" }) { artist ->
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clickable { onArtistClick(artist.id) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SmartImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DeezerOutlinedButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PlayGlyphOverlay(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_play_arrow_filled_24),
            contentDescription = stringResource(R.string.content_desc_play),
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
private fun DeezerRedPlayButton(size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(DeezerRed)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_play_arrow_filled_24),
            contentDescription = stringResource(R.string.content_desc_play),
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun DeezerCircleIcon(
    iconRes: Int? = null,
    painterIcon: Int? = null,
    tint: Color = Color.White,
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
            painter = painterResource(iconRes ?: painterIcon ?: R.drawable.rounded_more_vert_24),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DeezerCircleIconVector(
    onClick: () -> Unit,
    contentDescription: String?
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Share,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun formatFans(fans: Int): String =
    try {
        java.text.NumberFormat.getInstance(Locale.FRANCE).format(fans)
    } catch (e: Exception) {
        fans.toString()
    }

private fun releaseLine(album: Album): String {
    val type = when {
        album.songCount <= 1 -> "Single"
        album.songCount <= 6 -> "EP"
        else -> "Album"
    }
    return if (album.year > 0) "$type · ${album.year}" else type
}

private fun buildMixSubtitle(artistName: String, similar: List<Artist>): String {
    val names = (listOf(artistName) + similar.map { it.name }).distinct().take(4)
    return "Avec ${names.joinToString(", ")}"
}
