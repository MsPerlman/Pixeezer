package com.lostf1sh.pixelplayeross.presentation.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import java.util.concurrent.TimeUnit

@Composable
fun DeezerFullPlayerBody(
    song: Song,
    queueSourceName: String,
    albumCoverSection: @Composable (Modifier) -> Unit,
    currentPositionProvider: () -> Long,
    totalDuration: Long,
    isPlayingProvider: () -> Boolean,
    isFavoriteProvider: () -> Boolean,
    isShuffleEnabledProvider: () -> Boolean,
    repeatModeProvider: () -> Int,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCollapse: () -> Unit,
    onShowQueueClicked: () -> Unit,
    hasLyrics: Boolean,
    onLyricsClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onStartMixClick: () -> Unit,
    onOutputSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(top = statusBarInset, bottom = navBarInset)
    ) {
        val sideInset = 20.dp
        val rowModifier = Modifier.fillMaxWidth().padding(horizontal = sideInset)
        val bodyWidth = this@BoxWithConstraints.maxWidth
        val bodyHeight = this@BoxWithConstraints.maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            DeezerPlayerTopBar(
                queueSourceName = queueSourceName,
                onCollapse = onCollapse,
                onOverflowClick = onOverflowClick,
                modifier = rowModifier
            )

            Spacer(Modifier.weight(1f))

            DeezerPlayerCover(
                albumCoverSection = albumCoverSection,
                availableWidth = bodyWidth,
                availableHeight = bodyHeight,
                hasLyrics = hasLyrics,
                onLyricsClick = onLyricsClick
            )

            Spacer(Modifier.weight(1f))

            DeezerPlayerActionRow(
                isFavoriteProvider = isFavoriteProvider,
                onShareClick = onShareClick,
                onAddClick = onAddClick,
                onFavoriteToggle = onFavoriteToggle,
                modifier = rowModifier
            )

            Spacer(Modifier.height(16.dp))

            DeezerPlayerSeekBar(
                currentPositionProvider = currentPositionProvider,
                totalDuration = totalDuration,
                onSeek = onSeek,
                modifier = rowModifier
            )

            Spacer(Modifier.height(18.dp))

            DeezerPlayerTitleBlock(song = song, modifier = rowModifier)

            Spacer(Modifier.weight(1f))

            DeezerTransportRow(
                isPlayingProvider = isPlayingProvider,
                isShuffleEnabledProvider = isShuffleEnabledProvider,
                repeatModeProvider = repeatModeProvider,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onShuffleToggle = onShuffleToggle,
                onRepeatToggle = onRepeatToggle,
                modifier = rowModifier
            )

            Spacer(Modifier.weight(1f))

            DeezerPlayerBottomRow(
                onOutputSettingsClick = onOutputSettingsClick,
                onStartMixClick = onStartMixClick,
                onShowQueueClicked = onShowQueueClicked,
                modifier = rowModifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun DeezerPlayerTopBar(
    queueSourceName: String,
    onCollapse: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_keyboard_arrow_down_24,
            contentDescription = stringResource(R.string.content_desc_collapse),
            size = 28.dp,
            onClick = onCollapse
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.player_now_playing),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp
            )
            if (queueSourceName.isNotBlank()) {
                Text(
                    text = queueSourceName,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_more_vert_24,
            contentDescription = stringResource(R.string.content_desc_more_options),
            onClick = onOverflowClick
        )
    }
}

@Composable
private fun DeezerPlayerCover(
    albumCoverSection: @Composable (Modifier) -> Unit,
    availableWidth: Dp,
    availableHeight: Dp,
    hasLyrics: Boolean,
    onLyricsClick: () -> Unit
) {
    val carouselWidth = minOf(availableWidth - 16.dp, availableHeight * 0.5f)
    val coverSide = carouselWidth * 0.6f - 16.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        albumCoverSection(Modifier.width(carouselWidth))

        if (hasLyrics) {
            Box(modifier = Modifier.size(coverSide)) {
                DeezerLyricsChip(
                    onClick = onLyricsClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun DeezerLyricsChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_lyrics_24),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.lyrics),
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DeezerPlayerActionRow(
    isFavoriteProvider: () -> Boolean,
    onShareClick: () -> Unit,
    onAddClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFavorite = isFavoriteProvider()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onShareClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = stringResource(R.string.content_desc_share),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.content_desc_add),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onFavoriteToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) R.drawable.round_favorite_24 else R.drawable.round_favorite_border_24
                ),
                contentDescription = stringResource(R.string.content_desc_favorite),
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun DeezerPlayerSeekBar(
    currentPositionProvider: () -> Long,
    totalDuration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var scrubPosition by remember { mutableStateOf<Float?>(null) }
    val position = currentPositionProvider()
    val duration = totalDuration.coerceAtLeast(1L)
    val sliderValue = scrubPosition ?: (position.toFloat() / duration).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatPlayerTime((sliderValue * duration).toLong()),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = formatPlayerTime(totalDuration),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { scrubPosition = it },
            onValueChangeFinished = {
                scrubPosition?.let { onSeek((it * duration).toLong()) }
                scrubPosition = null
            },
            thumb = {
                Box(
                    Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            },
            track = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(sliderValue)
                            .background(Color.White)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(20.dp)
        )
    }
}

@Composable
private fun DeezerPlayerTitleBlock(song: Song, modifier: Modifier = Modifier) {
    val subtitle = if (song.album.isNotBlank()) {
        "${song.displayArtist} - ${song.album}"
    } else {
        song.displayArtist
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (song.isExplicit) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.dp, Color.White, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.explicit_marker),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun DeezerTransportRow(
    isPlayingProvider: () -> Boolean,
    isShuffleEnabledProvider: () -> Boolean,
    repeatModeProvider: () -> Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying = isPlayingProvider()
    val isShuffleEnabled = isShuffleEnabledProvider()
    val repeatMode = repeatModeProvider()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DeezerPlayerIcon(
            iconRes = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.rounded_repeat_one_24
                else -> R.drawable.rounded_repeat_24
            },
            contentDescription = stringResource(R.string.content_desc_repeat),
            tint = if (repeatMode != Player.REPEAT_MODE_OFF) Color.White else Color.White.copy(alpha = 0.55f),
            size = 26.dp,
            onClick = onRepeatToggle
        )
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_skip_previous_filled_24,
            contentDescription = stringResource(R.string.content_desc_previous),
            size = 42.dp,
            onClick = onPrevious
        )
        DeezerPlayerIcon(
            iconRes = if (isPlaying) R.drawable.rounded_pause_filled_24 else R.drawable.rounded_play_arrow_filled_24,
            contentDescription = if (isPlaying) stringResource(R.string.content_desc_pause) else stringResource(R.string.content_desc_play),
            size = 56.dp,
            boxSize = 68.dp,
            onClick = onPlayPause
        )
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_skip_next_filled_24,
            contentDescription = stringResource(R.string.content_desc_next),
            size = 42.dp,
            onClick = onNext
        )
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_shuffle_24,
            contentDescription = stringResource(R.string.content_desc_shuffle),
            tint = if (isShuffleEnabled) Color.White else Color.White.copy(alpha = 0.55f),
            size = 26.dp,
            onClick = onShuffleToggle
        )
    }
}

@Composable
private fun DeezerPlayerBottomRow(
    onOutputSettingsClick: () -> Unit,
    onStartMixClick: () -> Unit,
    onShowQueueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_speaker_24,
            contentDescription = stringResource(R.string.content_desc_audio_output),
            onClick = onOutputSettingsClick
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.player_start_mix),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(onClick = onStartMixClick)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Spacer(Modifier.weight(1f))
        DeezerPlayerIcon(
            iconRes = R.drawable.rounded_queue_music_24,
            contentDescription = stringResource(R.string.content_desc_queue),
            onClick = onShowQueueClicked
        )
    }
}

@Composable
private fun DeezerPlayerIcon(
    iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = Color.White,
    size: Dp = 24.dp,
    boxSize: Dp = 44.dp
) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}

private fun formatPlayerTime(millis: Long): String {
    val safeMillis = millis.coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(safeMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(safeMillis) % 60
    return "%d:%02d".format(minutes, seconds)
}
