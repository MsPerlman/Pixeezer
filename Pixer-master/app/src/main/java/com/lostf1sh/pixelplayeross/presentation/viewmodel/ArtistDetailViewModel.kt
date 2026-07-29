package com.lostf1sh.pixelplayeross.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.Artist
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.repository.ArtistImageRepository
import com.lostf1sh.pixelplayeross.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Holds the full UI state for ArtistDetailScreen.
 *
 * [effectiveImageUrl] is the resolved image to display (custom takes priority over Deezer).
 * It is updated after artist data loads and again whenever the user changes the custom image.
 */
data class ArtistDetailUiState(
    val artist: Artist? = null,
    val songs: List<Song> = emptyList(),
    val albumSections: List<ArtistAlbumSection> = emptyList(),
    val effectiveImageUrl: String? = null,
    val topTracks: List<Song> = emptyList(),
    val deezerAlbums: List<com.lostf1sh.pixelplayeross.data.model.Album> = emptyList(),
    val similarArtists: List<Artist> = emptyList(),
    val isLiked: Boolean = false,
    val fans: Int = 0,
    val albumsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Immutable
data class ArtistAlbumSection(
    val albumId: Long,
    val title: String,
    val year: Int?,
    val albumArtUriString: String?,
    val songs: List<Song>
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository,
    private val artistImageRepository: ArtistImageRepository,
    val themeStateHolder: ThemeStateHolder,
    private val deezerRepository: com.lostf1sh.pixelplayeross.data.repository.DeezerRepository,
    private val playlistPreferencesRepository: com.lostf1sh.pixelplayeross.data.preferences.PlaylistPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    /**
     * Pre-warmed color scheme for the current artist image.
     * This is populated synchronously (from the processor's LRU/DB cache) before [uiState]
     * marks [ArtistDetailUiState.isLoading] = false, so the screen has the correct palette
     * on its very first composition — no flash from system colors.
     *
     * Consumers should read this directly instead of calling [ThemeStateHolder.getAlbumColorSchemeFlow]
     * in order to avoid the initial-null-emission that causes the flash.
     */
    private val _artistColorScheme = MutableStateFlow<ColorSchemePair?>(null)
    val artistColorScheme: StateFlow<ColorSchemePair?> = _artistColorScheme.asStateFlow()

    init {
        savedStateHandle.getStateFlow<String?>("artistId", null)
            .onEach { idString ->
                if (idString != null) {
                    val artistId = idString.toLongOrNull()
                    if (artistId != null) {
                        loadArtistData(artistId)
                    } else {
                        _uiState.update { it.copy(error = context.getString(R.string.invalid_artist_id), isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(error = context.getString(R.string.artist_id_not_found), isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    private var currentLoadJob: Job? = null
    private var loadedArtistId: Long? = null

    private fun loadArtistData(id: Long) {
        loadedArtistId = id
        currentLoadJob?.cancel()
        currentLoadJob = viewModelScope.launch {
            _artistColorScheme.value = null
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    artist = null,
                    effectiveImageUrl = null,
                    songs = emptyList(),
                    albumSections = emptyList(),
                    topTracks = emptyList(),
                    deezerAlbums = emptyList(),
                    similarArtists = emptyList(),
                    isLiked = false,
                    fans = 0
                ) 
            }
            
            // Launch Deezer API fetches in parallel
            launch {
                fetchDeezerData(id)
            }

            try {
                val artistDetailsFlow = musicRepository.getArtistById(id)
                val artistSongsFlow = musicRepository.getSongsForArtist(id)

                combine(artistDetailsFlow, artistSongsFlow) { artist, songs ->
                    Timber.tag("ArtistDebug").d("loadArtistData: id=$id found=${artist != null} songs=${songs.size}")
                    artist to songs
                }
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                error = context.getString(R.string.error_loading_artist, e.localizedMessage ?: ""),
                                isLoading = false
                            )
                        }
                    }
                    .collect { (artist, songs) ->
                        if (artist == null) {
                            // Instead of failing immediately if local artist is null, we check if we're still loading Deezer data or if Deezer artist exists
                            // If it's a pure Deezer artist that hasn't synced yet, we might want to still show it.
                            // But syncLovedArtists should have inserted it already.
                            // However, we just return for now and let the UI show empty state or loading if needed.
                            return@collect
                        }

                        val albumSections = buildAlbumSections(songs)
                        val orderedSongs = albumSections.flatMap { it.songs }

                        val effectiveUrl = artist.imageUrl

                        // 2) Pre-warm the color scheme BEFORE emitting isLoading = false.
                        val newScheme = if (!effectiveUrl.isNullOrBlank()) {
                            try {
                                themeStateHolder.getOrGenerateColorScheme(effectiveUrl)
                            } catch (e: Exception) {
                                Timber.tag("ArtistDebug").w("Color scheme pre-warm failed: ${e.message}")
                                null
                            }
                        } else null

                        _artistColorScheme.value = newScheme
                        _uiState.update {
                            it.copy(
                                artist = artist.copy(
                                    imageUrl = if (artist.customImageUri.isNullOrBlank()) effectiveUrl else artist.imageUrl
                                ),
                                songs = orderedSongs,
                                albumSections = albumSections,
                                effectiveImageUrl = effectiveUrl
                            )
                        }
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = context.getString(R.string.error_loading_artist, e.localizedMessage ?: ""),
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun fetchDeezerData(id: Long) {
        try {
            val artistInfo = deezerRepository.getArtistInfo(id)
            val topTracksResponse = deezerRepository.getArtistTopTracks(id)
            val albumsResponse = deezerRepository.getArtistAlbums(id)
            val similarArtistsResponse = deezerRepository.getSimilarArtists(id)
            val lovedArtistsResponse = deezerRepository.getGcastLovedArtists()

            val isLiked = lovedArtistsResponse?.data?.included?.any { it.id == id.toString() } == true
            val fans = artistInfo?.fanCount ?: 0

            val topTracks = topTracksResponse?.data?.included?.mapIndexed { index, track ->
                Song(
                    id = track.id,
                    title = track.attributes?.title ?: "Unknown Title",
                    artist = track.attributes?.artistName ?: artistInfo?.name ?: "",
                    artistId = id,
                    album = track.attributes?.albumName ?: "Unknown Album",
                    albumId = 0L,
                    path = "",
                    contentUriString = "deezer://track/${track.id}",
                    albumArtUriString = track.attributes?.image?.large,
                    duration = (track.attributes?.duration ?: 0) * 1000L,
                    genre = null,
                    trackNumber = index + 1,
                    discNumber = 1,
                    dateAdded = System.currentTimeMillis(),
                    dateModified = System.currentTimeMillis(),
                    year = 0,
                    mimeType = null,
                    bitrate = null,
                    sampleRate = null,
                    isExplicit = track.attributes?.explicit ?: false
                )
            } ?: emptyList()

            val deezerAlbums = albumsResponse?.data?.included?.mapNotNull { item ->
                item.id?.toLongOrNull()?.let { albumId ->
                    com.lostf1sh.pixelplayeross.data.model.Album(
                        id = albumId,
                        title = item.attributes?.name ?: "Unknown Album",
                        artist = item.attributes?.artist?.name ?: artistInfo?.name ?: "",
                        albumArtUriString = item.attributes?.image?.let { it.full ?: it.large ?: it.medium },
                        songCount = item.attributes?.nbTracks ?: 0,
                        dateAdded = System.currentTimeMillis(),
                        year = item.attributes?.releaseDate?.substring(0, 4)?.toIntOrNull() ?: 0,
                        albumArtist = item.attributes?.artist?.name ?: artistInfo?.name ?: ""
                    )
                }
            } ?: emptyList()

            val similarArtists = similarArtistsResponse?.data?.included?.mapNotNull { item ->
                item.attributes?.let { attr ->
                    Artist(
                        id = attr.id,
                        name = attr.name ?: "Unknown Artist",
                        songCount = 0,
                        imageUrl = attr.pictures?.let { it.full ?: it.large ?: it.medium },
                        customImageUri = null,
                        fanCount = attr.nbFans,
                        albumCount = 0
                    )
                }
            } ?: emptyList()

            _uiState.update { currentState ->
                var currentArtist = currentState.artist
                var effectiveImageUrl = currentState.effectiveImageUrl
                if (currentArtist == null && artistInfo != null) {
                    val fallbackImageUrl = artistInfo.pictureXl ?: artistInfo.pictureBig ?: artistInfo.pictureMedium
                    currentArtist = Artist(
                        id = artistInfo.id,
                        name = artistInfo.name,
                        songCount = artistInfo.albumCount,
                        imageUrl = fallbackImageUrl,
                        customImageUri = null,
                        fanCount = artistInfo.fanCount,
                        albumCount = artistInfo.albumCount
                    )
                    effectiveImageUrl = fallbackImageUrl
                    
                    // Pre-warm color scheme for Deezer artist
                    if (!fallbackImageUrl.isNullOrBlank() && _artistColorScheme.value == null) {
                        try {
                            val newScheme = themeStateHolder.getOrGenerateColorScheme(fallbackImageUrl)
                            _artistColorScheme.value = newScheme
                        } catch (e: Exception) {
                            Timber.tag("ArtistDebug").w("Deezer Color scheme pre-warm failed: ${e.message}")
                        }
                    }
                }

                currentState.copy(
                    artist = currentArtist,
                    topTracks = topTracks,
                    deezerAlbums = deezerAlbums,
                    similarArtists = similarArtists,
                    effectiveImageUrl = currentState.effectiveImageUrl ?: effectiveImageUrl,
                    isLiked = isLiked,
                    fans = fans,
                    albumsCount = artistInfo?.albumCount ?: 0,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Timber.tag("ArtistDebug").e(e, "Exception fetching Deezer data for artist $id")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleArtistLike() {
        val artistId = loadedArtistId ?: return
        val currentLiked = _uiState.value.isLiked
        
        // Optimistic update
        _uiState.update { it.copy(isLiked = !currentLiked) }
        
        viewModelScope.launch {
            val success = if (currentLiked) {
                deezerRepository.unlikeArtist(artistId)
            } else {
                deezerRepository.likeArtist(artistId)
            }
            
            if (!success) {
                // Revert on failure
                _uiState.update { it.copy(isLiked = currentLiked) }
            } else {
                // Sync the database to reflect the change in the library tab immediately
                playlistPreferencesRepository.syncLovedArtists()
            }
        }
    }

    /** Re-attempts loading the artist after a failure (wired to the error-state retry button). */
    fun retry() {
        loadedArtistId?.let { loadArtistData(it) }
    }

    fun removeSongFromAlbumSection(songId: String) {
        _uiState.update { currentState ->
            val updatedAlbumSections = currentState.albumSections.map { section ->
                val updatedSongs = section.songs.filterNot { it.id == songId }
                section.copy(songs = updatedSongs)
            }.filter { it.songs.isNotEmpty() }

            currentState.copy(
                albumSections = updatedAlbumSections,
                songs = currentState.songs.filterNot { it.id == songId }
            )
        }
    }
}

private val songDisplayComparator = compareBy<Song> { it.discNumber ?: 1 }
    .thenBy { if (it.trackNumber > 0) it.trackNumber else Int.MAX_VALUE }
    .thenBy { it.title.lowercase() }

private fun buildAlbumSections(songs: List<Song>): List<ArtistAlbumSection> {
    if (songs.isEmpty()) return emptyList()

    val sections = songs
        .groupBy { it.albumId to it.album }
        .map { (key, albumSongs) ->
            val sortedSongs = albumSongs.sortedWith(songDisplayComparator)
            val albumYear = albumSongs.mapNotNull { song -> song.year.takeIf { it > 0 } }.maxOrNull()
            val albumArtUri = albumSongs.firstNotNullOfOrNull { it.albumArtUriString }
            ArtistAlbumSection(
                albumId = key.first,
                title = (key.second.takeIf { it.isNotBlank() } ?: "Unknown Album"),
                year = albumYear,
                albumArtUriString = albumArtUri,
                songs = sortedSongs
            )
        }

    val (withYear, withoutYear) = sections.partition { it.year != null }
    val withYearSorted = withYear.sortedWith(
        compareByDescending<ArtistAlbumSection> { it.year ?: Int.MIN_VALUE }
            .thenBy { it.title.lowercase() }
    )
    val withoutYearSorted = withoutYear.sortedBy { it.title.lowercase() }

    return withYearSorted + withoutYearSorted
}
