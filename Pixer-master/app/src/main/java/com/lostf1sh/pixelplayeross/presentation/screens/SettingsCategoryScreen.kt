package com.lostf1sh.pixelplayeross.presentation.screens

import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafely

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.lostf1sh.pixelplayeross.R

import com.lostf1sh.pixelplayeross.data.preferences.AppLanguage
import com.lostf1sh.pixelplayeross.data.preferences.AppThemeMode
import com.lostf1sh.pixelplayeross.data.preferences.CollagePattern
import com.lostf1sh.pixelplayeross.data.preferences.CarouselStyle
import com.lostf1sh.pixelplayeross.data.preferences.DeezerAudioQuality
import com.lostf1sh.pixelplayeross.data.preferences.LaunchTab
import com.lostf1sh.pixelplayeross.data.preferences.LibraryNavigationMode
import com.lostf1sh.pixelplayeross.data.preferences.NavBarStyle
import com.lostf1sh.pixelplayeross.data.preferences.ThemePreference
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.model.LyricsSourcePreference
import com.lostf1sh.pixelplayeross.presentation.components.CollapsibleCommonTopBar
import com.lostf1sh.pixelplayeross.presentation.components.ExpressiveTopBarContent
import com.lostf1sh.pixelplayeross.presentation.components.FileExplorerDialog
import com.lostf1sh.pixelplayeross.presentation.components.MiniPlayerHeight
import com.lostf1sh.pixelplayeross.presentation.model.SettingsCategory
import com.lostf1sh.pixelplayeross.presentation.navigation.Screen
import com.lostf1sh.pixelplayeross.presentation.viewmodel.LyricsRefreshProgress
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.SettingsViewModel
import com.lostf1sh.pixelplayeross.ui.theme.RoundedSans

@Composable
internal fun deezerAudioQualityLabel(quality: DeezerAudioQuality): String = when (quality) {
    DeezerAudioQuality.STANDARD -> stringResource(R.string.deezer_quality_standard_label)
    DeezerAudioQuality.HIGH -> stringResource(R.string.deezer_quality_high_label)
    DeezerAudioQuality.HIFI -> stringResource(R.string.deezer_quality_hifi_label)
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsCategoryScreen(
    categoryId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    statsViewModel: com.lostf1sh.pixelplayeross.presentation.viewmodel.StatsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val category = SettingsCategory.fromId(categoryId) ?: return
    val context = LocalContext.current
    
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val playbackSpeed by settingsViewModel.playbackSpeed.collectAsStateWithLifecycle()
    val currentPath by settingsViewModel.currentPath.collectAsStateWithLifecycle()
    val directoryChildren by settingsViewModel.currentDirectoryChildren.collectAsStateWithLifecycle()
    val availableStorages by settingsViewModel.availableStorages.collectAsStateWithLifecycle()
    val selectedStorageIndex by settingsViewModel.selectedStorageIndex.collectAsStateWithLifecycle()
    val isLoadingDirectories by settingsViewModel.isLoadingDirectories.collectAsStateWithLifecycle()
    val isExplorerPriming by settingsViewModel.isExplorerPriming.collectAsStateWithLifecycle()
    val isExplorerReady by settingsViewModel.isExplorerReady.collectAsStateWithLifecycle()
    val isCurrentDirectoryResolved by settingsViewModel.isCurrentDirectoryResolved.collectAsStateWithLifecycle()

    val paletteRegenerateTargets by playerViewModel.paletteRegenerationTargets.collectAsStateWithLifecycle()
    val explorerRoot = settingsViewModel.explorerRoot()

    var showExplorerSheet by remember { mutableStateOf(false) }
    var refreshRequested by remember { mutableStateOf(false) }
    var syncRequestObservedRunning by remember { mutableStateOf(false) }
    var syncIndicatorLabel by remember { mutableStateOf<String?>(null) }
    var showClearLyricsDialog by remember { mutableStateOf(false) }
    var showRebuildDatabaseWarning by remember { mutableStateOf(false) }
    var showRegenerateDailyMixDialog by remember { mutableStateOf(false) }
    var showRegenerateStatsDialog by remember { mutableStateOf(false) }
    var showRegenerateAllPalettesDialog by remember { mutableStateOf(false) }

    var minSongDurationDraft by remember(uiState.minSongDuration) {
        mutableStateOf(uiState.minSongDuration.toFloat())
    }
    var minTracksPerAlbumDraft by remember(uiState.minTracksPerAlbum) {
        mutableStateOf(uiState.minTracksPerAlbum.toFloat())
    }
    var albumArtCacheLimitDraft by remember(uiState.albumArtCacheLimitMb) {
        mutableStateOf(uiState.albumArtCacheLimitMb.toFloat())
    }






    var showPaletteRegenerateSheet by remember { mutableStateOf(false) }
    var isPaletteRegenerateRunning by remember { mutableStateOf(false) }
    var isPaletteBulkRegenerateRunning by remember { mutableStateOf(false) }
    var paletteBulkCompletedCount by remember { mutableStateOf(0) }
    var paletteBulkTotalCount by remember { mutableStateOf(0) }
    var paletteSongSearchQuery by remember { mutableStateOf("") }
    val paletteRegenerateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAnyPaletteRegenerateRunning = isPaletteRegenerateRunning || isPaletteBulkRegenerateRunning
    val filteredPaletteSongs = remember(paletteRegenerateTargets, paletteSongSearchQuery) {
        val query = paletteSongSearchQuery.trim()
        if (query.isBlank()) {
            paletteRegenerateTargets
        } else {
            paletteRegenerateTargets.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                    song.displayArtist.contains(query, ignoreCase = true) ||
                    song.album.contains(query, ignoreCase = true)
            }
        }
    }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    
    val categoryTitle = stringResource(category.titleRes)
    val isLongTitle = categoryTitle.length > 13
    
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minTopBarHeight = 64.dp + statusBarHeight
    val maxTopBarHeight = if (isLongTitle) 200.dp else 180.dp

    val minTopBarHeightPx = with(density) { minTopBarHeight.toPx() }
    val maxTopBarHeightPx = with(density) { maxTopBarHeight.toPx() }
    
    val titleMaxLines = if (isLongTitle) 2 else 1

    val topBarHeight = remember(maxTopBarHeightPx) { Animatable(maxTopBarHeightPx) }
    var collapseFraction by remember { mutableStateOf(0f) }

    LaunchedEffect(topBarHeight.value, maxTopBarHeightPx) {
        collapseFraction =
                1f -
                        ((topBarHeight.value - minTopBarHeightPx) /
                                        (maxTopBarHeightPx - minTopBarHeightPx))
                                .coerceIn(0f, 1f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrollingDown = delta < 0

                if (!isScrollingDown &&
                                (lazyListState.firstVisibleItemIndex > 0 ||
                                        lazyListState.firstVisibleItemScrollOffset > 0)
                ) {
                    return Offset.Zero
                }

                val previousHeight = topBarHeight.value
                val newHeight =
                        (previousHeight + delta).coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                val consumed = newHeight - previousHeight

                if (consumed.roundToInt() != 0) {
                    coroutineScope.launch { topBarHeight.snapTo(newHeight) }
                }

                val canConsumeScroll = !(isScrollingDown && newHeight == minTopBarHeightPx)
                return if (canConsumeScroll) Offset(0f, consumed) else Offset.Zero
            }
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val shouldExpand = topBarHeight.value > (minTopBarHeightPx + maxTopBarHeightPx) / 2
            val canExpand =
                    lazyListState.firstVisibleItemIndex == 0 &&
                            lazyListState.firstVisibleItemScrollOffset == 0

            val targetValue =
                    if (shouldExpand && canExpand) maxTopBarHeightPx else minTopBarHeightPx

            if (topBarHeight.value != targetValue) {
                coroutineScope.launch {
                    topBarHeight.animateTo(targetValue, spring(stiffness = Spring.StiffnessMedium))
                }
            }
        }
    }

    Box(
        modifier =
            Modifier.nestedScroll(nestedScrollConnection).fillMaxSize()
    ) {
        val currentTopBarHeightDp = with(density) { topBarHeight.value.toDp() }
        
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = currentTopBarHeightDp + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = MiniPlayerHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            )
        ) {
            item {
               Column(
                    modifier = Modifier.background(Color.Transparent)
               ) {
                    when (category) {
                        SettingsCategory.LIBRARY -> {
                            SettingsSubsection(title = stringResource(R.string.setcat_library_structure)) {
                                SettingsItem(
                                    title = stringResource(R.string.setcat_excluded_directories_title),
                                    subtitle = stringResource(R.string.setcat_excluded_directories_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.cd_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = {
                                        showExplorerSheet = true
                                        settingsViewModel.openExplorer()
                                    }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.setcat_artists_title),
                                    subtitle = stringResource(R.string.setcat_artists_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.cd_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigateSafely(Screen.ArtistSettings.route) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_filtering)) {
                                SliderSettingsItem(
                                    label = stringResource(R.string.setcat_min_song_duration),
                                    value = minSongDurationDraft,
                                    valueRange = 0f..120000f,
                                    steps = 23,
                                    onValueChange = { minSongDurationDraft = it },
                                    onValueChangeFinished = {
                                        val selectedDuration = minSongDurationDraft.toInt()
                                        if (selectedDuration != uiState.minSongDuration) {
                                            settingsViewModel.setMinSongDuration(selectedDuration)
                                        }
                                    },
                                    valueText = { value -> "${(value / 1000).toInt()}s" }
                                )
                                SliderSettingsItem(
                                    label = stringResource(R.string.setcat_min_tracks_per_album),
                                    value = minTracksPerAlbumDraft,
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    onValueChange = { minTracksPerAlbumDraft = it },
                                    onValueChangeFinished = {
                                        val selectedTracks = minTracksPerAlbumDraft.toInt()
                                        if (selectedTracks != uiState.minTracksPerAlbum) {
                                            settingsViewModel.setMinTracksPerAlbum(selectedTracks)
                                        }
                                    },
                                    valueText = { value -> "${value.toInt()}" }
                                )
                                SliderSettingsItem(
                                    label = stringResource(R.string.setcat_album_art_cache_limit),
                                    value = albumArtCacheLimitDraft,
                                    valueRange = 50f..1500f,
                                    steps = 28,
                                    onValueChange = { albumArtCacheLimitDraft = it },
                                    onValueChangeFinished = {
                                        val selectedLimit = albumArtCacheLimitDraft.toInt()
                                        if (selectedLimit != uiState.albumArtCacheLimitMb) {
                                            settingsViewModel.setAlbumArtCacheLimitMb(selectedLimit)
                                        }
                                    },
                                    valueText = { value -> "${value.toInt()} MB" }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_sync_scanning)) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_auto_scan_lrc_title),
                                    subtitle = stringResource(R.string.setcat_auto_scan_lrc_subtitle),
                                    checked = uiState.autoScanLrcFiles,
                                    onCheckedChange = { settingsViewModel.setAutoScanLrcFiles(it) },
                                    leadingIcon = { Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.setcat_find_duplicates_title),
                                    subtitle = stringResource(R.string.setcat_find_duplicates_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.cd_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigateSafely(Screen.Duplicates.route) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_online_services)) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_external_lyrics_title),
                                    subtitle = stringResource(R.string.setcat_external_lyrics_subtitle),
                                    checked = uiState.externalLyricsEnabled,
                                    onCheckedChange = { settingsViewModel.setExternalLyricsEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_lyrics_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_external_artist_images_title),
                                    subtitle = stringResource(R.string.setcat_external_artist_images_subtitle),
                                    checked = uiState.externalArtistImagesEnabled,
                                    onCheckedChange = { settingsViewModel.setExternalArtistImagesEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_artist_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.setcat_lyrics_management),
                                addBottomSpace = false
                            ) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_lyrics_source_priority_label),
                                    description = stringResource(R.string.setcat_lyrics_source_priority_desc),
                                    options = mapOf(
                                        LyricsSourcePreference.EMBEDDED_FIRST.name to stringResource(R.string.setcat_lyrics_embedded_first),
                                        LyricsSourcePreference.API_FIRST.name to stringResource(R.string.setcat_lyrics_online_first),
                                        LyricsSourcePreference.LOCAL_FIRST.name to stringResource(R.string.setcat_lyrics_local_first)
                                    ),
                                    selectedKey = uiState.lyricsSourcePreference.name,
                                    onSelectionChanged = { key ->
                                        settingsViewModel.setLyricsSourcePreference(
                                            LyricsSourcePreference.fromName(key)
                                        )
                                    },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_lyrics_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.setcat_reset_imported_lyrics_title),
                                    subtitle = stringResource(R.string.setcat_reset_imported_lyrics_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.ClearAll, null, tint = MaterialTheme.colorScheme.secondary) },
                                    onClick = { showClearLyricsDialog = true }
                                )
                            }

                        }
                        SettingsCategory.APPEARANCE -> {
                            val useSmoothCorners by settingsViewModel.useSmoothCorners.collectAsStateWithLifecycle()
                            val appLanguage by settingsViewModel.appLanguage.collectAsStateWithLifecycle()

                            SettingsSubsection(title = stringResource(R.string.setcat_language_section)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_language_label),
                                    description = stringResource(R.string.setcat_language_desc),
                                    options = mapOf(
                                        AppLanguage.SYSTEM to stringResource(R.string.setcat_language_system),
                                        AppLanguage.FRENCH to stringResource(R.string.setcat_language_french),
                                        AppLanguage.ENGLISH to stringResource(R.string.setcat_language_english)
                                    ),
                                    selectedKey = appLanguage,
                                    onSelectionChanged = { settingsViewModel.setAppLanguage(it) },
                                    leadingIcon = { Icon(Icons.Outlined.Language, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_global_theme)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_app_theme_label),
                                    description = stringResource(R.string.setcat_app_theme_desc),
                                    options = mapOf(
                                        AppThemeMode.LIGHT to stringResource(R.string.setcat_theme_light),
                                        AppThemeMode.DARK to stringResource(R.string.setcat_theme_dark),
                                        AppThemeMode.FOLLOW_SYSTEM to stringResource(R.string.setcat_theme_follow_system),
                                        AppThemeMode.DEEZER to stringResource(R.string.setcat_theme_deezer)
                                    ),
                                    selectedKey = uiState.appThemeMode,
                                    onSelectionChanged = { settingsViewModel.setAppThemeMode(it) },
                                    leadingIcon = { Icon(Icons.Outlined.LightMode, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_smooth_corners_title),
                                    subtitle = stringResource(R.string.setcat_smooth_corners_subtitle),
                                    checked = useSmoothCorners,
                                    onCheckedChange = settingsViewModel::setUseSmoothCorners,
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_rounded_corner_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_now_playing)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_player_theme_label),
                                    description = stringResource(R.string.setcat_player_theme_desc),
                                    options = mapOf(
                                        ThemePreference.ALBUM_ART to stringResource(R.string.setcat_player_theme_album_art),
                                        ThemePreference.DYNAMIC to stringResource(R.string.setcat_player_theme_dynamic)
                                    ),
                                    selectedKey = uiState.playerThemePreference,
                                    onSelectionChanged = { settingsViewModel.setPlayerThemePreference(it) },
                                    leadingIcon = { Icon(Icons.Outlined.PlayCircle, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_show_player_file_info_title),
                                    subtitle = stringResource(R.string.setcat_show_player_file_info_subtitle),
                                    checked = uiState.showPlayerFileInfo,
                                    onCheckedChange = { settingsViewModel.setShowPlayerFileInfo(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_attach_file_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.setcat_album_art_palette_title),
                                    subtitle = stringResource(R.string.setcat_album_art_palette_subtitle, uiState.albumArtPaletteStyle.label),
                                    leadingIcon = { Icon(Icons.Outlined.Style, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigateSafely(Screen.PaletteStyle.route) }
                                )
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_carousel_style_label),
                                    description = stringResource(R.string.setcat_carousel_style_desc),
                                    options = mapOf(
                                        CarouselStyle.NO_PEEK to stringResource(R.string.setcat_carousel_no_peek),
                                        CarouselStyle.ONE_PEEK to stringResource(R.string.setcat_carousel_one_peek)
                                    ),
                                    selectedKey = uiState.carouselStyle,
                                    onSelectionChanged = { settingsViewModel.setCarouselStyle(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_view_carousel_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_navigation_bar)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_navbar_style_label),
                                    description = stringResource(R.string.setcat_navbar_style_desc),
                                    options = mapOf(
                                        NavBarStyle.DEFAULT to stringResource(R.string.setcat_navbar_style_default),
                                        NavBarStyle.FULL_WIDTH to stringResource(R.string.setcat_navbar_style_full_width)
                                    ),
                                    selectedKey = uiState.navBarStyle,
                                    onSelectionChanged = { settingsViewModel.setNavBarStyle(it) },
                                    leadingIcon = { Icon(Icons.Outlined.Style, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_compact_mode_title),
                                    subtitle = stringResource(R.string.setcat_compact_mode_subtitle),
                                    checked = uiState.navBarCompactMode,
                                    onCheckedChange = { settingsViewModel.setNavBarCompactMode(it) },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.rounded_view_week_24),
                                            null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.setcat_navbar_corner_title),
                                    subtitle = stringResource(R.string.setcat_navbar_corner_subtitle),
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_rounded_corner_24), null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigateSafely("nav_bar_corner_radius") }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_lyrics_screen)) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_immersive_lyrics_title),
                                    subtitle = stringResource(R.string.setcat_immersive_lyrics_subtitle),
                                    checked = uiState.immersiveLyricsEnabled,
                                    onCheckedChange = { settingsViewModel.setImmersiveLyricsEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_lyrics_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )

                                if (uiState.immersiveLyricsEnabled) {
                                    ThemeSelectorItem(
                                        label = stringResource(R.string.setcat_auto_hide_delay_label),
                                        description = stringResource(R.string.setcat_auto_hide_delay_desc),
                                        options = mapOf(
                                            "3000" to stringResource(R.string.setcat_lyrics_delay_3s),
                                            "4000" to stringResource(R.string.setcat_lyrics_delay_4s),
                                            "5000" to stringResource(R.string.setcat_lyrics_delay_5s),
                                            "6000" to stringResource(R.string.setcat_lyrics_delay_6s)
                                        ),
                                        selectedKey = uiState.immersiveLyricsTimeout.toString(),
                                        onSelectionChanged = { settingsViewModel.setImmersiveLyricsTimeout(it.toLong()) },
                                        leadingIcon = { Icon(Icons.Rounded.Timer, null, tint = MaterialTheme.colorScheme.secondary) }
                                    )
                                }
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.setcat_app_navigation_section),
                                addBottomSpace = false
                            ) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_default_tab_label),
                                    description = stringResource(R.string.setcat_default_tab_desc),
                                    options = mapOf(
                                        LaunchTab.HOME to stringResource(R.string.tab_home),
                                        LaunchTab.SEARCH to stringResource(R.string.search),
                                        LaunchTab.LIBRARY to stringResource(R.string.tab_library),
                                    ),
                                    selectedKey = uiState.launchTab,
                                    onSelectionChanged = { settingsViewModel.setLaunchTab(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.tab_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_library_navigation_label),
                                    description = stringResource(R.string.setcat_library_navigation_desc),
                                    options = mapOf(
                                        LibraryNavigationMode.TAB_ROW to stringResource(R.string.setcat_library_nav_tab_row),
                                        LibraryNavigationMode.COMPACT_PILL to stringResource(R.string.setcat_library_nav_compact_pill)
                                    ),
                                    selectedKey = uiState.libraryNavigationMode,
                                    onSelectionChanged = { settingsViewModel.setLibraryNavigationMode(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_library_music_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }
                        }
                        SettingsCategory.PLAYBACK -> {
                            SettingsSubsection(title = stringResource(R.string.setcat_background_playback)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_keep_playing_label),
                                    description = stringResource(R.string.setcat_keep_playing_desc),
                                    options = mapOf("true" to stringResource(R.string.label_on), "false" to stringResource(R.string.label_off)),
                                    selectedKey = if (uiState.keepPlayingInBackground) "true" else "false",
                                    onSelectionChanged = { settingsViewModel.setKeepPlayingInBackground(it.toBoolean()) },
                                    leadingIcon = { Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_deezer_streaming_section)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.audio_quality_title),
                                    description = stringResource(R.string.setcat_audio_quality_description),
                                    options = DeezerAudioQuality.entries.associate { it.name to deezerAudioQualityLabel(it) },
                                    selectedKey = uiState.deezerAudioQuality.name,
                                    onSelectionChanged = { settingsViewModel.setDeezerAudioQuality(com.lostf1sh.pixelplayeross.data.preferences.DeezerAudioQuality.valueOf(it)) },
                                    leadingIcon = { Icon(painterResource(R.drawable.outline_high_quality_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_replaygain_section)) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_replaygain_enable_title),
                                    subtitle = stringResource(R.string.setcat_replaygain_enable_subtitle),
                                    checked = uiState.replayGainEnabled,
                                    onCheckedChange = { settingsViewModel.setReplayGainEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_volume_down_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                AnimatedVisibility(
                                    visible = uiState.replayGainEnabled,
                                    enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(animationSpec = spring(stiffness = 400f)),
                                    exit = shrinkVertically(animationSpec = spring(stiffness = 500f)) + fadeOut(animationSpec = spring(stiffness = 500f))
                                ) {
                                    ThemeSelectorItem(
                                        label = stringResource(R.string.setcat_gain_mode_label),
                                        description = stringResource(R.string.setcat_gain_mode_desc),
                                        options = mapOf("track" to stringResource(R.string.setcat_gain_mode_track), "album" to stringResource(R.string.setcat_gain_mode_album)),
                                        selectedKey = if (uiState.replayGainUseAlbumGain) "album" else "track",
                                        onSelectionChanged = { settingsViewModel.setReplayGainUseAlbumGain(it == "album") },
                                        leadingIcon = { Icon(painterResource(R.drawable.rounded_volume_down_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                    )
                                }
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_headphones)) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_headphones_resume_title),
                                    subtitle = stringResource(R.string.setcat_headphones_resume_subtitle),
                                    checked = uiState.resumeOnHeadsetReconnect,
                                    onCheckedChange = { settingsViewModel.setResumeOnHeadsetReconnect(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_headphones_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.setcat_queue_transitions)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.setcat_crossfade_label),
                                    description = stringResource(R.string.setcat_crossfade_desc),
                                    options = mapOf("true" to stringResource(R.string.label_enabled), "false" to stringResource(R.string.label_disabled)),
                                    selectedKey = if (uiState.isCrossfadeEnabled) "true" else "false",
                                    onSelectionChanged = { settingsViewModel.setCrossfadeEnabled(it.toBoolean()) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_align_justify_space_even_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                if (uiState.isCrossfadeEnabled) {
                                    SliderSettingsItem(
                                        label = stringResource(R.string.setcat_crossfade_duration),
                                        value = uiState.crossfadeDuration.toFloat(),
                                        valueRange = 1000f..12000f,
                                        steps= 10,
                                        onValueChange = { settingsViewModel.setCrossfadeDuration(it.toInt()) },
                                        valueText = { value -> "${(value / 1000).toInt()}s" }
                                    )
                                }
                                SliderSettingsItem(
                                    label = stringResource(R.string.setcat_playback_speed),
                                    value = playbackSpeed,
                                    valueRange = 0.5f..2.0f,
                                    steps = 5,
                                    onValueChange = { settingsViewModel.setPlaybackSpeed(it) },
                                    valueText = { value -> String.format(Locale.US, "%.2f×", value) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_hifi_mode_title),
                                    subtitle = if (uiState.hiFiModeDeviceSupported)
                                        stringResource(R.string.setcat_hifi_mode_subtitle_supported)
                                    else
                                        stringResource(R.string.setcat_hifi_mode_subtitle_unsupported),
                                    checked = uiState.hiFiModeEnabled,
                                    onCheckedChange = { settingsViewModel.setHiFiModeEnabled(it) },
                                    enabled = uiState.hiFiModeDeviceSupported,
                                    leadingIcon = { Icon(painterResource(R.drawable.outline_high_quality_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_persistent_shuffle_title),
                                    subtitle = stringResource(R.string.setcat_persistent_shuffle_subtitle),
                                    checked = uiState.persistentShuffleEnabled,
                                    onCheckedChange = { settingsViewModel.setPersistentShuffleEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_shuffle_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_show_queue_history_title),
                                    subtitle = stringResource(R.string.setcat_show_queue_history_subtitle),
                                    checked = uiState.showQueueHistory,
                                    onCheckedChange = { settingsViewModel.setShowQueueHistory(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_queue_music_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                        }
                        SettingsCategory.BEHAVIOR -> {
                            SettingsSubsection(
                                title = stringResource(R.string.setcat_player_gestures)
                            ) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_tap_bg_closes_title),
                                    subtitle = stringResource(R.string.setcat_tap_bg_closes_subtitle),
                                    checked = uiState.tapBackgroundClosesPlayer,
                                    onCheckedChange = { settingsViewModel.setTapBackgroundClosesPlayer(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_touch_app_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }
                            SettingsSubsection(
                                title = stringResource(R.string.setcat_haptics),
                                addBottomSpace = false
                            ) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.setcat_haptic_feedback_title),
                                    subtitle = stringResource(R.string.setcat_haptic_feedback_subtitle),
                                    checked = uiState.hapticsEnabled,
                                    onCheckedChange = { settingsViewModel.setHapticsEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_touch_app_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }
                        }
                        SettingsCategory.DEVELOPER -> {
                            SettingsSubsection(title = stringResource(R.string.setcat_experiments)) {
                                SettingsItem(
                                    title = stringResource(R.string.setcat_experimental_title),
                                    subtitle = stringResource(R.string.setcat_experimental_subtitle),
                                    leadingIcon = { Icon(Icons.Rounded.Science, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.cd_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigateSafely(Screen.Experimental.route) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.setcat_test_setup_title),
                                    subtitle = stringResource(R.string.setcat_test_setup_subtitle),
                                    leadingIcon = { Icon(Icons.Rounded.Science, null, tint = MaterialTheme.colorScheme.tertiary) },
                                    onClick = {
                                        settingsViewModel.resetSetupFlow()
                                    }
                                )
                            }



                            SettingsSubsection(
                                title = stringResource(R.string.setcat_diagnostics),
                                addBottomSpace = false
                            ) {
                                SettingsItem(
                                    title = stringResource(R.string.setcat_trigger_crash_title),
                                    subtitle = stringResource(R.string.setcat_trigger_crash_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { settingsViewModel.triggerTestCrash() }
                                )
                            }
                        }
                        SettingsCategory.ABOUT -> {
                            SettingsSubsection(
                                title = stringResource(R.string.setcat_application),
                                addBottomSpace = false
                            ) {
                                SettingsItem(
                                    title = stringResource(R.string.setcat_about_pixelplayer_title),
                                    subtitle = stringResource(R.string.setcat_about_pixelplayer_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigateSafely("about") }
                                )
                            }
                        }
                        SettingsCategory.EQUALIZER -> {
                        }
                        SettingsCategory.DEVICE_CAPABILITIES -> {
                        }

                    }
               }
            }

            item {
                Spacer(Modifier.height(1.dp))
            }
        }

        CollapsibleCommonTopBar(
            collapseFraction = collapseFraction,
            headerHeight = currentTopBarHeightDp,
            onBackClick = onBackClick,
            title = categoryTitle,
            maxLines = titleMaxLines
        )

        var isTransitioning by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(com.lostf1sh.pixelplayeross.presentation.navigation.TRANSITION_DURATION.toLong())
            isTransitioning = false
        }
        
        if (isTransitioning) {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                   awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
            )
        }
    }


    FileExplorerDialog(
        visible = showExplorerSheet,
        currentPath = currentPath,
        directoryChildren = directoryChildren,
        availableStorages = availableStorages,
        selectedStorageIndex = selectedStorageIndex,
        isLoading = isLoadingDirectories,
        isPriming = isExplorerPriming,
        isReady = isExplorerReady,
        isCurrentDirectoryResolved = isCurrentDirectoryResolved,
        isAtRoot = settingsViewModel.isAtRoot(),
        rootDirectory = explorerRoot,
        onNavigateTo = settingsViewModel::loadDirectory,
        onNavigateUp = settingsViewModel::navigateUp,
        onNavigateHome = { settingsViewModel.loadDirectory(explorerRoot) },
        onToggleAllowed = settingsViewModel::toggleDirectoryAllowed,
        onRefresh = settingsViewModel::refreshExplorer,
        onStorageSelected = settingsViewModel::selectStorage,
        onDone = {
            settingsViewModel.applyPendingDirectoryRuleChanges()
            showExplorerSheet = false
        },
        onDismiss = {
            settingsViewModel.applyPendingDirectoryRuleChanges()
            showExplorerSheet = false
        }
    )

    if (showPaletteRegenerateSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!isAnyPaletteRegenerateRunning) {
                    showPaletteRegenerateSheet = false
                    paletteSongSearchQuery = ""
                }
            },
            sheetState = paletteRegenerateSheetState
        ) {
            PaletteRegenerateSongSheetContent(
                songs = filteredPaletteSongs,
                isRunning = isPaletteRegenerateRunning,
                searchQuery = paletteSongSearchQuery,
                onSearchQueryChange = { paletteSongSearchQuery = it },
                onClearSearch = { paletteSongSearchQuery = "" },
                onSongClick = { song ->
                    if (isAnyPaletteRegenerateRunning) return@PaletteRegenerateSongSheetContent
                    isPaletteRegenerateRunning = true
                    coroutineScope.launch {
                        val success = playerViewModel.forceRegenerateAlbumPaletteForSong(song)
                        isPaletteRegenerateRunning = false
                        if (success) {
                            showPaletteRegenerateSheet = false
                            paletteSongSearchQuery = ""
                            Toast.makeText(
                                context,
                                context.getString(R.string.dialog_palette_regenerated, song.title),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.dialog_palette_regenerate_failed, song.title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }

    if (showRegenerateAllPalettesDialog) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Outlined.Style,
                    null,
                    tint = if (isPaletteBulkRegenerateRunning) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            },
            title = {
                Text(
                    if (isPaletteBulkRegenerateRunning) {
                        stringResource(R.string.dialog_regenerating_palettes_title)
                    } else {
                        stringResource(R.string.dialog_regenerate_palettes_title)
                    }
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isPaletteBulkRegenerateRunning) {
                            stringResource(R.string.dialog_regenerate_palettes_progress_body, paletteBulkTotalCount)
                        } else {
                            stringResource(R.string.dialog_regenerate_palettes_confirm_body, paletteRegenerateTargets.size)
                        }
                    )

                    if (isPaletteBulkRegenerateRunning) {
                        val progress = if (paletteBulkTotalCount > 0) {
                            paletteBulkCompletedCount.toFloat() / paletteBulkTotalCount.toFloat()
                        } else {
                            0f
                        }

                        LinearWavyProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Text(
                            text = stringResource(R.string.dialog_palette_progress_format, paletteBulkCompletedCount, paletteBulkTotalCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            onDismissRequest = {
                if (!isPaletteBulkRegenerateRunning) {
                    showRegenerateAllPalettesDialog = false
                }
            },
            confirmButton = {
                if (isPaletteBulkRegenerateRunning) {
                    TextButton(
                        onClick = {},
                        enabled = false
                    ) {
                        Text(stringResource(R.string.working_ellipsis), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    TextButton(
                        onClick = {
                            isPaletteBulkRegenerateRunning = true
                            paletteBulkCompletedCount = 0
                            paletteBulkTotalCount = paletteRegenerateTargets.size

                            coroutineScope.launch {
                                var successCount = 0
                                paletteRegenerateTargets.forEachIndexed { index, song ->
                                    if (playerViewModel.forceRegenerateAlbumPaletteForSong(song)) {
                                        successCount++
                                    }
                                    paletteBulkCompletedCount = index + 1
                                }

                                isPaletteBulkRegenerateRunning = false
                                showRegenerateAllPalettesDialog = false

                                val totalCount = paletteRegenerateTargets.size
                                Toast.makeText(
                                    context,
                                    if (successCount == totalCount) {
                                        context.getString(R.string.toast_regenerated_palettes_all, successCount)
                                    } else {
                                        context.getString(R.string.toast_regenerated_palettes_partial, successCount, totalCount)
                                    },
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.dialog_regenerate))
                    }
                }
            },
            dismissButton = {
                if (!isPaletteBulkRegenerateRunning) {
                    TextButton(
                        onClick = { showRegenerateAllPalettesDialog = false }
                    ) {
                        Text(stringResource(R.string.cancel), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        )
    }
    
    if (showClearLyricsDialog) {
        AlertDialog(
            icon = { Icon(Icons.Outlined.Warning, null) },
            title = { Text(stringResource(R.string.dialog_reset_lyrics_title)) },
            text = { Text(stringResource(R.string.dialog_cannot_undo)) },
            onDismissRequest = { showClearLyricsDialog = false },
            confirmButton = { TextButton(onClick = { showClearLyricsDialog = false; playerViewModel.resetAllLyrics() }) { Text(stringResource(R.string.confirm), maxLines = 1, overflow = TextOverflow.Ellipsis) } },
            dismissButton = { TextButton(onClick = { showClearLyricsDialog = false }) { Text(stringResource(R.string.cancel), maxLines = 1, overflow = TextOverflow.Ellipsis) } }
        )
    }

    
    if (showRebuildDatabaseWarning) {
        AlertDialog(
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.dialog_rebuild_database_title)) },
            text = { Text(stringResource(R.string.dialog_rebuild_database_message)) },
            onDismissRequest = { showRebuildDatabaseWarning = false },
            confirmButton = { 
                TextButton(
                    onClick = { 
                        showRebuildDatabaseWarning = false
                        refreshRequested = true
                        syncRequestObservedRunning = false
                        syncIndicatorLabel = context.getString(R.string.sync_indicator_rebuilding)
                        Toast.makeText(context, context.getString(R.string.toast_rebuilding_database), Toast.LENGTH_SHORT).show()
                        settingsViewModel.rebuildDatabase() 
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { 
                    Text(stringResource(R.string.rebuild)) 
                } 
            },
            dismissButton = { TextButton(onClick = { showRebuildDatabaseWarning = false }) { Text(stringResource(R.string.cancel), maxLines = 1, overflow = TextOverflow.Ellipsis) } }
        )
    }

    if (showRegenerateDailyMixDialog) {
        AlertDialog(
            icon = { Icon(painterResource(R.drawable.rounded_instant_mix_24), null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.dialog_regenerate_daily_mix_title)) },
            text = { Text(stringResource(R.string.dialog_regenerate_daily_mix_body)) },
            onDismissRequest = { showRegenerateDailyMixDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegenerateDailyMixDialog = false
                        playerViewModel.forceUpdateDailyMix()
                        Toast.makeText(context, context.getString(R.string.toast_daily_mix_regeneration_started), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.dialog_regenerate), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            dismissButton = { TextButton(onClick = { showRegenerateDailyMixDialog = false }) { Text(stringResource(R.string.cancel), maxLines = 1, overflow = TextOverflow.Ellipsis) } }
        )
    }

    if (showRegenerateStatsDialog) {
        AlertDialog(
            icon = { Icon(painterResource(R.drawable.rounded_monitoring_24), null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.dialog_regenerate_stats_title)) },
            text = { Text(stringResource(R.string.dialog_regenerate_stats_body)) },
            onDismissRequest = { showRegenerateStatsDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegenerateStatsDialog = false
                        statsViewModel.forceRegenerateStats()
                        Toast.makeText(context, context.getString(R.string.toast_stats_regeneration_started), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.dialog_regenerate), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            dismissButton = { TextButton(onClick = { showRegenerateStatsDialog = false }) { Text(stringResource(R.string.cancel), maxLines = 1, overflow = TextOverflow.Ellipsis) } }
        )
    }

}

@Composable
private fun PaletteRegenerateSongSheetContent(
    songs: List<Song>,
    isRunning: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.palette_force_regenerate_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.palette_force_regenerate_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isRunning,
            placeholder = { Text(stringResource(R.string.search_songs_by_title_artist_album)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = onClearSearch,
                        enabled = !isRunning
                    ) {
                        Icon(Icons.Outlined.ClearAll, contentDescription = stringResource(R.string.cd_clear_search))
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        if (isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.palette_regenerating_progress),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 460.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (songs.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.search_no_songs_match),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(songs, key = { it.id }) { song ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isRunning) { onSongClick(song) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.displayArtist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (song.album.isNotBlank()) {
                                Text(
                                    text = song.album,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSubsectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsSubsection(
    title: String,
    addBottomSpace: Boolean = true,
    content: @Composable () -> Unit
) {
    SettingsSubsectionHeader(title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
    if (addBottomSpace) {
        Spacer(modifier = Modifier.height(10.dp))
    }
}
