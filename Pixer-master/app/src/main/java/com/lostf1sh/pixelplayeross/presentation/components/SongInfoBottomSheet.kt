package com.lostf1sh.pixelplayeross.presentation.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.components.subcomps.AutoSizingTextToFill
import com.lostf1sh.pixelplayeross.utils.formatDuration
import com.lostf1sh.pixelplayeross.utils.shapes.RoundedStarShape
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lostf1sh.pixelplayeross.data.media.CoverArtUpdate
import com.lostf1sh.pixelplayeross.ui.theme.MontserratFamily
import com.lostf1sh.pixelplayeross.presentation.viewmodel.SongInfoBottomSheetViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.SongInfoBottomSheetViewModel.ToneTarget
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.TransformOrigin
import com.lostf1sh.pixelplayeross.presentation.screens.TabAnimation
import com.lostf1sh.pixelplayeross.ui.theme.RoundedSans
import com.lostf1sh.pixelplayeross.utils.AudioMetaUtils
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun SongInfoBottomSheet(
    song: Song,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onPlaySong: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddNextToQueue: () -> Unit,
    onAddToPlayList: () -> Unit,
    onDeleteFromDevice: (activity: Activity, song: Song, onResult: (Boolean) -> Unit) -> Unit,
    onNavigateToAlbum: () -> Unit,
    onNavigateToArtist: () -> Unit,
    onNavigateToArtistById: (Long) -> Unit = { onNavigateToArtist() },
    onNavigateToGenre: () -> Unit,
    onEditSong: (
        title: String,
        artist: String,
        album: String,
        albumArtist: String,
        composer: String,
        genre: String,
        lyrics: String,
        trackNumber: Int,
        discNumber: Int?,
        replayGainTrackGainDb: String,
        replayGainAlbumGainDb: String,
        coverArtUpdate: CoverArtUpdate?
    ) -> Unit,
    removeFromListTrigger: () -> Unit,
    songInfoViewModel: SongInfoBottomSheetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }
    var showArtistPicker by remember { mutableStateOf(false) }
    var showTonePickerDialog by remember { mutableStateOf(false) }
    var toneConfirmationTarget by remember { mutableStateOf<ToneTarget?>(null) }
    var pendingTonePermissionSong by remember { mutableStateOf<Song?>(null) }
    var pendingTonePermissionTarget by remember { mutableStateOf<ToneTarget?>(null) }
    val audioMeta by songInfoViewModel.audioMeta.collectAsStateWithLifecycle()
    val resolvedArtists by songInfoViewModel.resolvedArtists.collectAsStateWithLifecycle()
    val ringtonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val pendingSong = pendingTonePermissionSong
        val pendingTarget = pendingTonePermissionTarget
        pendingTonePermissionSong = null
        pendingTonePermissionTarget = null
        if (pendingSong == null || pendingTarget == null) {
            return@rememberLauncherForActivityResult
        }
        if (songInfoViewModel.hasSystemWritePermission()) {
            songInfoViewModel.setSongAsTone(pendingSong, pendingTarget) { result ->
                val message = when (result) {
                    is SongInfoBottomSheetViewModel.ToneActionResult.Success -> result.message
                    is SongInfoBottomSheetViewModel.ToneActionResult.Error -> result.message
                    is SongInfoBottomSheetViewModel.ToneActionResult.NeedsSystemWritePermission -> result.message
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.song_info_ringtone_permission_missing),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun requestToneSystemWritePermission(songToSet: Song, target: ToneTarget, message: String) {
        pendingTonePermissionSong = songToSet
        pendingTonePermissionTarget = target
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        try {
            ringtonePermissionLauncher.launch(songInfoViewModel.createSystemWriteSettingsIntent())
        } catch (_: ActivityNotFoundException) {
            try {
                ringtonePermissionLauncher.launch(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Exception) {
                pendingTonePermissionSong = null
                pendingTonePermissionTarget = null
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.song_info_ringtone_failed,
                        e.localizedMessage ?: ""
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun handleToneResult(
        songToSet: Song,
        target: ToneTarget,
        result: SongInfoBottomSheetViewModel.ToneActionResult
    ) {
        when (result) {
            is SongInfoBottomSheetViewModel.ToneActionResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            is SongInfoBottomSheetViewModel.ToneActionResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            is SongInfoBottomSheetViewModel.ToneActionResult.NeedsSystemWritePermission -> {
                requestToneSystemWritePermission(songToSet, target, result.message)
            }
        }
    }

    fun setCurrentSongAsTone(target: ToneTarget) {
        songInfoViewModel.setSongAsTone(song, target) { result ->
            handleToneResult(song, target, result)
        }
    }

    val evenCornerRadiusElems = 26.dp

    val listItemShape = remember {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = 20.dp, smoothnessAsPercentBR = 60, cornerRadiusBR = 20.dp,
            smoothnessAsPercentTL = 60, cornerRadiusTL = 20.dp, smoothnessAsPercentBL = 60,
            cornerRadiusBL = 20.dp, smoothnessAsPercentTR = 60
        )
    }
    val albumArtShape = remember(evenCornerRadiusElems) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = evenCornerRadiusElems, smoothnessAsPercentBR = 60, cornerRadiusBR = evenCornerRadiusElems,
            smoothnessAsPercentTL = 60, cornerRadiusTL = evenCornerRadiusElems, smoothnessAsPercentBL = 60,
            cornerRadiusBL = evenCornerRadiusElems, smoothnessAsPercentTR = 60
        )
    }
    val playButtonShape = remember(evenCornerRadiusElems) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = evenCornerRadiusElems, smoothnessAsPercentBR = 60, cornerRadiusBR = evenCornerRadiusElems,
            smoothnessAsPercentTL = 60, cornerRadiusTL = evenCornerRadiusElems, smoothnessAsPercentBL = 60,
            cornerRadiusBL = evenCornerRadiusElems, smoothnessAsPercentTR = 60
        )
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val favoriteButtonCornerRadius by animateDpAsState(
        targetValue = if (isFavorite) evenCornerRadiusElems else 60.dp,
        animationSpec = tween(durationMillis = 300), label = "FavoriteCornerAnimation"
    )
    val favoriteButtonContainerColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "FavoriteContainerColorAnimation"
    )
    val favoriteButtonContentColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "FavoriteContentColorAnimation"
    )
    val favoriteButtonShape = remember(favoriteButtonCornerRadius) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = favoriteButtonCornerRadius, smoothnessAsPercentBR = 60, cornerRadiusBR = favoriteButtonCornerRadius,
            smoothnessAsPercentTL = 60, cornerRadiusTL = favoriteButtonCornerRadius, smoothnessAsPercentBL = 60,
            cornerRadiusBL = favoriteButtonCornerRadius, smoothnessAsPercentTR = 60
        )
    }
    val infoSegmentContainerShape = remember {
        RoundedCornerShape(20.dp)
    }
    val infoSegmentItemShape = remember {
        RoundedCornerShape(8.dp)
    }

    val audioMetaLabel = remember(audioMeta) {
        val meta = audioMeta ?: return@remember null
        val formatLabel = AudioMetaUtils.mimeTypeToFormat(meta.mimeType)
            .takeIf { it != "-" }
            ?.uppercase(java.util.Locale.getDefault())
        val parts = buildList {
            meta.sampleRate?.takeIf { it > 0 }
                ?.let { add(String.format(java.util.Locale.US, "%.1f kHz", it / 1000.0)) }
            meta.bitrate?.takeIf { it > 0 }
                ?.let { add("${it / 1000} kbps") }
            formatLabel?.let { add(it) }
        }
        parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }
    val songLocationInfo = remember(song.path, song.contentUriString) {
        songInfoViewModel.getSongLocationInfo(song)
    }

    LaunchedEffect(song.id) {
        songInfoViewModel.loadAudioMeta(song)
        songInfoViewModel.loadArtistsForSong(song)
    }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Pin-to-home, only offered under the Deezer skin (shares the activity-scoped LibraryViewModel
    // with the home grid).
    val isDeezerSkin = com.lostf1sh.pixelplayeross.ui.theme.LocalAppSkin.current ==
        com.lostf1sh.pixelplayeross.ui.theme.AppSkin.DEEZER
    val activity = context as? androidx.activity.ComponentActivity
    val libraryViewModel = if (isDeezerSkin && activity != null) {
        androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel>(activity)
    } else null
    val pinnedItems by (libraryViewModel?.pinnedHomeItems
        ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList()))
        .collectAsStateWithLifecycle()
    val isTrackPinned = pinnedItems.any {
        it.type == com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType.TRACK && it.id == song.id
    }

    fun shareSong() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, song.contentUriString.toUri())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, context.getString(R.string.song_info_share_chooser_title))
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.error_share_song_format, e.localizedMessage ?: ""),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!showEditSheet) {
                onDismiss()
            }
        },
        sheetState = sheetState,
    ) {
        // HERE WE APPLY THE FIX: Null out the overscroll factory for everything inside here
        CompositionLocalProvider(
            LocalOverscrollFactory provides null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        // Row for the album art and the title (Always visible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmartImage(
                                model = song.albumArtUriString,
                                contentDescription = stringResource(R.string.widget_album_art),
                                shape = albumArtShape,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                AutoSizingTextToFill(
                                    modifier = Modifier.padding(end = 4.dp),
                                    fontWeight = FontWeight.Light,
                                    text = song.title
                                )
                            }
                            val isEditable = remember(song) { songInfoViewModel.isSongEditable(song) }
                            if (isEditable) {
                                FilledTonalIconButton(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(vertical = 6.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    onClick = { showEditSheet = true },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.cd_edit_song_metadata)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Flat, Spotify-style single scrolling list: actions first, then info rows.
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        item {
                            SongSheetActionRow(Icons.Rounded.PlayArrow, stringResource(R.string.play_playback), onClick = onPlaySong)
                        }
                        item {
                            SongSheetActionRow(Icons.AutoMirrored.Filled.QueueMusic, stringResource(R.string.action_queue_next), onClick = onAddNextToQueue)
                        }
                        item {
                            SongSheetActionRow(Icons.AutoMirrored.Rounded.QueueMusic, stringResource(R.string.action_add_to_queue), onClick = onAddToQueue)
                        }
                        item {
                            SongSheetActionRow(Icons.AutoMirrored.Rounded.PlaylistAdd, stringResource(R.string.shortcut_playlist_short), onClick = onAddToPlayList)
                        }
                        item {
                            SongSheetActionRow(
                                icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                label = stringResource(if (isFavorite) R.string.cd_remove_from_favorites else R.string.cd_add_to_favorites),
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else null,
                                onClick = onToggleFavorite
                            )
                        }
                        item {
                            SongSheetActionRow(Icons.Rounded.Share, stringResource(R.string.cd_share_song_file), onClick = { shareSong() })
                        }

                        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) }

                        item {
                            SongSheetActionRow(Icons.Rounded.Album, song.album, onClick = onNavigateToAlbum)
                        }
                        item {
                            SongSheetActionRow(
                                icon = Icons.Rounded.Person,
                                label = song.displayArtist,
                                onClick = {
                                    if (song.artists.size > 1) showArtistPicker = true else onNavigateToArtist()
                                }
                            )
                        }
                        if (!song.genre.isNullOrEmpty()) {
                            item {
                                SongSheetActionRow(Icons.Rounded.MusicNote, song.genre, onClick = onNavigateToGenre)
                            }
                        }
                        item {
                            SongSheetActionRow(Icons.Rounded.Schedule, formatDuration(song.duration), onClick = null)
                        }
                        if (!audioMetaLabel.isNullOrEmpty()) {
                            item {
                                SongSheetActionRow(Icons.Rounded.Info, audioMetaLabel, onClick = null)
                            }
                        }
                        if (libraryViewModel != null) {
                            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) }
                            item {
                                val pinLimitMessage = stringResource(R.string.pin_limit_reached_message, com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel.MAX_PINNED_HOME_ITEMS)
                                SongSheetActionRow(
                                    icon = androidx.compose.ui.res.painterResource(R.drawable.rounded_push_pin_24),
                                    label = stringResource(if (isTrackPinned) R.string.content_desc_unpin_from_home else R.string.content_desc_pin_to_home),
                                    tint = if (isTrackPinned) MaterialTheme.colorScheme.primary else null,
                                    onClick = {
                                        val didToggle = libraryViewModel.togglePin(
                                            com.lostf1sh.pixelplayeross.data.preferences.PinnedHomeItem(
                                                type = com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType.TRACK,
                                                id = song.id,
                                                label = song.title,
                                                imageUrl = song.albumArtUriString
                                            )
                                        )
                                        if (!didToggle) {
                                            Toast.makeText(context, pinLimitMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                        item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
                    }
                }
            }
        }
    }

    EditSongSheet(
        visible = showEditSheet,
        song = song,
        onDismiss = { showEditSheet = false },
        onSave = { title, artist, album, albumArtist, composer, genre, lyrics, trackNumber, discNumber, replayGainTrackGainDb, replayGainAlbumGainDb, coverArt ->
            onEditSong(
                title,
                artist,
                album,
                albumArtist,
                composer,
                genre,
                lyrics,
                trackNumber,
                discNumber,
                replayGainTrackGainDb,
                replayGainAlbumGainDb,
                coverArt
            )
            showEditSheet = false
        },
    )

    val artistPickerSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showArtistPicker && resolvedArtists.isNotEmpty()) {
        com.lostf1sh.pixelplayeross.presentation.components.player.PlayerArtistPickerBottomSheet(
            song = song,
            artists = resolvedArtists,
            sheetState = artistPickerSheetState,
            onDismiss = { showArtistPicker = false },
            onArtistClick = { artist ->
                showArtistPicker = false
                onNavigateToArtistById(artist.id)
            }
        )
    }

    if (showTonePickerDialog) {
        ToneTargetPickerDialog(
            onDismiss = { showTonePickerDialog = false },
            onTargetSelected = { target ->
                showTonePickerDialog = false
                toneConfirmationTarget = target
            }
        )
    }

    toneConfirmationTarget?.let { target ->
        ToneConfirmationDialog(
            song = song,
            target = target,
            onDismiss = { toneConfirmationTarget = null },
            onConfirm = {
                toneConfirmationTarget = null
                setCurrentSongAsTone(target)
            }
        )
    }
}

/** A flat Spotify-style action row: leading icon + label, whole row tappable. */
@Composable
private fun SongSheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    onClick: (() -> Unit)?,
) {
    SongSheetActionRowScaffold(modifier, onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SongSheetActionRow(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    onClick: (() -> Unit)?,
) {
    SongSheetActionRowScaffold(modifier, onClick) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = tint ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SongSheetActionRowScaffold(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToneTargetPickerDialog(
    onDismiss: () -> Unit,
    onTargetSelected: (ToneTarget) -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTR = 32.dp,
                smoothnessAsPercentBR = 60,
                cornerRadiusBR = 32.dp,
                smoothnessAsPercentTL = 60,
                cornerRadiusTL = 32.dp,
                smoothnessAsPercentBL = 60,
                cornerRadiusBL = 32.dp,
                smoothnessAsPercentTR = 60,
            ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToneDialogIcon(target = null)
                    Text(
                        text = stringResource(R.string.song_info_tone_picker_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = stringResource(R.string.song_info_tone_picker_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    modifier = Modifier.clip(RoundedCornerShape(22.dp)),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    ToneTarget.values().forEach { target ->
                        ToneTargetOption(
                            target = target,
                            onClick = { onTargetSelected(target) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneTargetOption(
    target: ToneTarget,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        leadingContent = {
            ToneDialogIcon(
                target = target,
                modifier = Modifier.size(42.dp),
                iconModifier = Modifier.size(22.dp),
            )
        },
        headlineContent = {
            Text(
                text = stringResource(target.titleResId),
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = {
            Text(stringResource(target.subtitleResId))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToneConfirmationDialog(
    song: Song,
    target: ToneTarget,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTR = 32.dp,
                smoothnessAsPercentBR = 60,
                cornerRadiusBR = 32.dp,
                smoothnessAsPercentTL = 60,
                cornerRadiusTL = 32.dp,
                smoothnessAsPercentBL = 60,
                cornerRadiusBL = 32.dp,
                smoothnessAsPercentTR = 60,
            ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToneDialogIcon(target = target)
                    Text(
                        text = stringResource(R.string.song_info_tone_confirm_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.song_info_tone_confirm_body,
                        song.title,
                        stringResource(target.confirmLabelResId),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    FilledTonalButton(onClick = onConfirm) {
                        Text(stringResource(R.string.song_info_tone_confirm_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun ToneDialogIcon(
    target: ToneTarget?,
    modifier: Modifier = Modifier.size(56.dp),
    iconModifier: Modifier = Modifier.size(28.dp),
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        when (target) {
            ToneTarget.Ringtone -> Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                modifier = iconModifier,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            ToneTarget.Notification -> Icon(
                painter = painterResource(R.drawable.rounded_notifications_active_24),
                contentDescription = null,
                modifier = iconModifier,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            ToneTarget.Alarm -> Icon(
                painter = painterResource(R.drawable.rounded_alarm_24),
                contentDescription = null,
                modifier = iconModifier,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            null -> Icon(
                painter = painterResource(R.drawable.rounded_notifications_active_24),
                contentDescription = null,
                modifier = iconModifier,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun RingtoneActionButton(
    modifier: Modifier,
    showText: Boolean,
    compactText: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    if (showText) {
        FilledTonalButton(
            modifier = modifier,
            colors = colors,
            contentPadding = PaddingValues(horizontal = if (compactText) 12.dp else 18.dp),
            shape = CircleShape,
            onClick = onClick,
        ) {
            Icon(
                modifier = Modifier.size(if (compactText) 20.dp else 24.dp),
                painter = painterResource(R.drawable.rounded_notifications_active_24),
                contentDescription = stringResource(R.string.cd_choose_song_tone),
            )
            Spacer(Modifier.width(if (compactText) 6.dp else 8.dp))
            Text(
                text = stringResource(
                    if (compactText) R.string.song_info_set_as_short else R.string.song_info_choose_tone
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        FilledTonalIconButton(
            modifier = modifier,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            shape = CircleShape,
            onClick = onClick,
        ) {
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                painter = painterResource(R.drawable.rounded_notifications_active_24),
                contentDescription = stringResource(R.string.cd_choose_song_tone),
            )
        }
    }
}

private val ToneTarget.titleResId: Int
    get() = when (this) {
        ToneTarget.Ringtone -> R.string.song_info_tone_ringtone_title
        ToneTarget.Notification -> R.string.song_info_tone_notification_title
        ToneTarget.Alarm -> R.string.song_info_tone_alarm_title
    }

private val ToneTarget.subtitleResId: Int
    get() = when (this) {
        ToneTarget.Ringtone -> R.string.song_info_tone_ringtone_subtitle
        ToneTarget.Notification -> R.string.song_info_tone_notification_subtitle
        ToneTarget.Alarm -> R.string.song_info_tone_alarm_subtitle
    }

private val ToneTarget.confirmLabelResId: Int
    get() = when (this) {
        ToneTarget.Ringtone -> R.string.song_info_tone_ringtone_label
        ToneTarget.Notification -> R.string.song_info_tone_notification_label
        ToneTarget.Alarm -> R.string.song_info_tone_alarm_label
    }

@Composable
private fun SongInfoSegmentedListItem(
    headline: String,
    supporting: String,
    icon: ImageVector,
    iconDescription: String,
    shape: Shape,
    onClick: (() -> Unit)? = null,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .let { baseModifier ->
            if (onClick != null) {
                baseModifier.clickable(onClick = onClick)
            } else {
                baseModifier
            }
        }

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(headline) },
            supportingContent = { Text(supporting) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                )
            }
        )
    }
}
