package com.lostf1sh.pixelplayeross.presentation.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.size.Size
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.LibraryTabId
import com.lostf1sh.pixelplayeross.data.model.Playlist
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.components.SmartImage

@Composable
fun DeezerLibraryHub(
    playlists: List<Playlist>,
    albumCount: Int,
    artistCount: Int,
    likedCount: Int,
    firstAlbumArtUrl: String?,
    firstArtistArtUrl: String?,
    likedPreviews: List<Song>,
    onPlaylistClick: (Playlist) -> Unit,
    onOpenCategory: (LibraryTabId) -> Unit,
    onOpenLiked: () -> Unit,
    onPreviewPlay: (Song) -> Unit,
    onPreviewMore: (Song) -> Unit,
    onOpenSettings: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding
    ) {
        item(key = "hub_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.presentation_batch_d_library_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_settings_24),
                        contentDescription = stringResource(R.string.presentation_batch_d_open_settings_cd),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item(key = "hub_liked_card") {
            DeezerLikedTracksCard(
                likedCount = likedCount,
                previews = likedPreviews,
                onOpenLiked = onOpenLiked,
                onPreviewPlay = onPreviewPlay,
                onPreviewMore = onPreviewMore
            )
        }

        item(key = "hub_playlists_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCategory(LibraryTabId.PLAYLISTS) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_favorite_24),
                    contentDescription = null,
                    tint = Color(0xFFE8397B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dash_title_playlists),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = playlists.size.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(R.drawable.rounded_chevron_right_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (playlists.isNotEmpty()) {
            item(key = "hub_playlists_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        Column(
                            modifier = Modifier
                                .width(128.dp)
                                .clickable { onPlaylistClick(playlist) }
                        ) {
                            SmartImage(
                                model = playlist.coverImageUri,
                                contentDescription = playlist.name,
                                contentScale = ContentScale.Crop,
                                targetSize = Size(300, 300),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(128.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            playlist.creatorName?.takeIf { it.isNotBlank() }?.let { creator ->
                                Text(
                                    text = stringResource(R.string.by_creator_format, creator),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "hub_spacer") { Spacer(Modifier.height(28.dp)) }

        item(key = "hub_albums") {
            DeezerLibraryCategoryRow(
                label = "Albums",
                count = albumCount,
                imageUrl = firstAlbumArtUrl,
                thumbnailShape = RoundedCornerShape(6.dp),
                fallbackIconRes = R.drawable.rounded_album_24,
                onClick = { onOpenCategory(LibraryTabId.ALBUMS) }
            )
        }
        item(key = "hub_artists") {
            DeezerLibraryCategoryRow(
                label = "Artistes",
                count = artistCount,
                imageUrl = firstArtistArtUrl,
                thumbnailShape = CircleShape,
                fallbackIconRes = R.drawable.rounded_artist_24,
                onClick = { onOpenCategory(LibraryTabId.ARTISTS) },
                showDivider = false
            )
        }
    }
}

@Composable
private fun DeezerLikedTracksCard(
    likedCount: Int,
    previews: List<Song>,
    onOpenLiked: () -> Unit,
    onPreviewPlay: (Song) -> Unit,
    onPreviewMore: (Song) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161616))
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenLiked)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7A3BF5), Color(0xFFB07BFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_favorite_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.library_hub_loved_tracks),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$likedCount titres",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenLiked),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_play_arrow_24),
                    contentDescription = stringResource(R.string.content_desc_play_favorites),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        if (previews.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = Color.White.copy(alpha = 0.08f)
            )
            previews.forEach { song ->
                DeezerLikedPreviewRow(
                    song = song,
                    onClick = { onPreviewPlay(song) },
                    onMore = { onPreviewMore(song) }
                )
            }
        }
    }
}

@Composable
private fun DeezerLikedPreviewRow(
    song: Song,
    onClick: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmartImage(
            model = song.albumArtUriString,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            targetSize = Size(128, 128),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onMore),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_more_vert_24),
                contentDescription = stringResource(R.string.cd_options),
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DeezerLibraryCategoryRow(
    label: String,
    count: Int,
    imageUrl: String?,
    thumbnailShape: Shape,
    fallbackIconRes: Int,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl != null) {
                SmartImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    targetSize = Size(128, 128),
                    shape = thumbnailShape,
                    modifier = Modifier.size(52.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(thumbnailShape)
                        .background(Color(0xFF1C1C1C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(fallbackIconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                painter = painterResource(R.drawable.rounded_chevron_right_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
