@file:kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.size.Size
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import androidx.compose.foundation.shape.RoundedCornerShape
import com.lostf1sh.pixelplayeross.ui.theme.AppSkin
import com.lostf1sh.pixelplayeross.ui.theme.LocalAppSkin

internal val LocalMaterialTheme = staticCompositionLocalOf<ColorScheme> { error("No ColorScheme provided") }

val MiniPlayerHeight = 64.dp
const val ANIMATION_DURATION_MS = 255
val MiniPlayerBottomSpacer = 8.dp

@Composable
fun getNavigationBarHeight(): Dp {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    return sanitizeNavigationBarBottomInset(insets.calculateBottomPadding())
}

@Composable
internal fun MiniPlayerContentInternal(
    song: Song,
    isPlaying: Boolean,
    isOutputConnecting: Boolean,
    isPreparingPlayback: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    canScroll: Boolean = true,
    onToggleFavorite: () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current
    val controlsEnabled = !isOutputConnecting && !isPreparingPlayback
    val isDeezerSkin = LocalAppSkin.current == AppSkin.DEEZER
    val miniPlayerGlassBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        listOf(
            LocalMaterialTheme.current.primaryContainer.copy(alpha = 0.92f),
            LocalMaterialTheme.current.primaryContainer.copy(alpha = 0.78f)
        )
    )

    val previousInteraction = remember { MutableInteractionSource() }
    val playPauseInteraction = remember { MutableInteractionSource() }
    val nextInteraction = remember { MutableInteractionSource() }
    val favoriteInteraction = remember { MutableInteractionSource() }
    val miniPlayerIndication = remember { ripple(bounded = false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .let {
                if (isDeezerSkin) {
                    it.clip(RoundedCornerShape(32.dp))
                        .background(miniPlayerGlassBrush)
                } else {
                    it
                }
            }
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isDeezerSkin) {
            val albumArtModel = song.albumArtUriString?.takeIf { it.isNotBlank() }
            Box(contentAlignment = Alignment.Center) {
                key(song.id) {
                    SmartImage(
                        model = albumArtModel,
                        contentDescription = stringResource(R.string.cd_album_art_for_title, song.title),
                        shape = CircleShape,
                        targetSize = Size(150, 150),
                        modifier = Modifier.size(44.dp)
                    )
                }
                if (isOutputConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = LocalMaterialTheme.current.onPrimaryContainer
                    )
                } else if (isPreparingPlayback) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = playPauseInteraction,
                        indication = miniPlayerIndication,
                        enabled = controlsEnabled
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                    tint = LocalMaterialTheme.current.onPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val textColor = LocalMaterialTheme.current.onPrimaryContainer
            val titleStyle = MaterialTheme.typography.titleSmall.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                color = textColor
            )
            val artistStyle = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                color = textColor.copy(alpha = 0.7f)
            )

            AutoScrollingText(
                text = when {
                    isOutputConnecting -> stringResource(R.string.player_connecting_to_device)
                    isPreparingPlayback -> stringResource(R.string.player_preparing_playback)
                    else -> song.title
                },
                style = titleStyle,
                gradientEdgeColor = LocalMaterialTheme.current.primaryContainer,
                canScroll = canScroll
            )
            AutoScrollingText(
                text = if (isPreparingPlayback) stringResource(R.string.player_loading_audio) else song.displayArtist,
                style = artistStyle,
                gradientEdgeColor = LocalMaterialTheme.current.primaryContainer,
                canScroll = canScroll
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        if (!isDeezerSkin) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LocalMaterialTheme.current.onPrimary)
                    .clickable(
                        interactionSource = previousInteraction,
                        indication = miniPlayerIndication,
                        enabled = controlsEnabled
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPrevious()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = stringResource(R.string.previous_track),
                    tint = LocalMaterialTheme.current.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = favoriteInteraction,
                        indication = miniPlayerIndication,
                        enabled = controlsEnabled
                    ) { onToggleFavorite() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (song.isFavorite) R.drawable.round_favorite_24 else R.drawable.rounded_favorite_24),
                    contentDescription = stringResource(R.string.content_desc_favorite),
                    tint = LocalMaterialTheme.current.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (!isDeezerSkin) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LocalMaterialTheme.current.primary)
                    .clickable(
                        interactionSource = playPauseInteraction,
                        indication = miniPlayerIndication,
                        enabled = controlsEnabled
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                    tint = LocalMaterialTheme.current.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .let { if (isDeezerSkin) it else it.background(LocalMaterialTheme.current.onPrimary) }
                .clickable(
                    interactionSource = nextInteraction,
                    indication = miniPlayerIndication,
                    enabled = controlsEnabled
                ) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.next_track),
                tint = if (isDeezerSkin) LocalMaterialTheme.current.onPrimaryContainer else LocalMaterialTheme.current.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
