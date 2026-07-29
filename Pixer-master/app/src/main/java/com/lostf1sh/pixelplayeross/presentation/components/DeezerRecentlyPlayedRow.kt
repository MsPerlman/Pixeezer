package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.size.Size
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.model.RecentlyPlayedSongUiModel

@Composable
fun DeezerRecentlyPlayedRow(
    songs: List<RecentlyPlayedSongUiModel>,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (songs.isEmpty()) return
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.presentation_batch_b_recently_played_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs, key = { it.song.id }) { entry ->
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onSongClick(entry.song) }
                ) {
                    SmartImage(
                        model = entry.song.albumArtUriString,
                        contentDescription = entry.song.title,
                        contentScale = ContentScale.Crop,
                        targetSize = Size(256, 256),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entry.song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
