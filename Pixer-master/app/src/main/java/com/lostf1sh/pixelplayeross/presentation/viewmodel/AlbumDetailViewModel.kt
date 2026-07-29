package com.lostf1sh.pixelplayeross.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.data.model.Album
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.repository.MusicRepository
import com.lostf1sh.pixelplayeross.data.repository.DeezerRepository
import com.lostf1sh.pixelplayeross.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailUiState(
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLiked: Boolean = false,
    val fans: Int = 0
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository,
    private val deezerRepository: DeezerRepository,
    private val playlistPreferencesRepository: com.lostf1sh.pixelplayeross.data.preferences.PlaylistPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    private var loadedAlbumId: Long? = null

    init {
        val albumIdString: String? = savedStateHandle.get("albumId")
        if (albumIdString != null) {
            val albumId = albumIdString.toLongOrNull()
            if (albumId != null) {
                loadedAlbumId = albumId
                loadAlbumData(albumId)
            } else {
                _uiState.update { it.copy(error = context.getString(R.string.invalid_album_id), isLoading = false) }
            }
        } else {
            _uiState.update { it.copy(error = context.getString(R.string.album_id_not_found), isLoading = false) }
        }
    }

    private fun loadAlbumData(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Try fetching from Deezer first if we have network, or if it's a known Deezer ID
            val deezerAlbum = deezerRepository.getAlbumInfo(id)

            if (deezerAlbum?.data != null) {
                val data = deezerAlbum.data
                val coverXl = data.attributes?.image?.coverXl() ?: data.attributes?.image?.large ?: data.attributes?.image?.medium
                
                val album = Album(
                    id = id,
                    title = data.attributes?.name ?: "",
                    artist = data.attributes?.artist?.name ?: "",
                    albumArtUriString = coverXl,
                    songCount = data.attributes?.nbTracks ?: 0,
                    dateAdded = System.currentTimeMillis(),
                    year = data.attributes?.releaseDate?.take(4)?.toIntOrNull() ?: 0
                )
                
                val songs = data.included?.mapIndexed { index, track ->
                    Song(
                        id = "deezer_${track.id}",
                        title = track.attributes?.title ?: "",
                        artist = track.attributes?.artistName ?: "",
                        artistId = 0L,
                        album = track.attributes?.albumName ?: "",
                        albumId = id,
                        path = "",
                        contentUriString = "deezer://track/${track.id}",
                        albumArtUriString = coverXl,
                        duration = (track.attributes?.duration ?: 0) * 1000L,
                        genre = null,
                        trackNumber = index + 1,
                        discNumber = 1,
                        dateAdded = System.currentTimeMillis(),
                        year = album.year,
                        mimeType = null,
                        bitrate = null,
                        sampleRate = null,
                        isExplicit = track.attributes?.explicit ?: false
                    )
                } ?: emptyList()
                
                // Fetch loved albums to see if this is liked
                val lovedAlbumsResponse = deezerRepository.getGcastLovedAlbums(limit = 1000)
                val isLiked = lovedAlbumsResponse?.data?.included?.any { it.id == id.toString() } == true
                val fans = data.attributes?.fans ?: 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        album = album,
                        songs = songs,
                        isLiked = isLiked,
                        fans = fans
                    )
                }
                return@launch
            }

            try {
                val albumDetailsFlow = musicRepository.getAlbumById(id)
                val albumSongsFlow = musicRepository.getSongsForAlbum(id)

                combine(albumDetailsFlow, albumSongsFlow) { album, songs ->
                    if (album != null) {
                        AlbumDetailUiState(
                            album = album,
                            songs = songs.sortedWith(
                                compareBy<Song> { it.discNumber ?: 1 }
                                    .thenBy { if (it.trackNumber > 0) it.trackNumber else Int.MAX_VALUE }
                                    .thenBy { it.title.lowercase() }
                            ),
                            isLoading = false
                        )
                    } else {
                        AlbumDetailUiState(
                            error = context.getString(R.string.album_not_found),
                            isLoading = false
                        )
                    }
                }
                    .catch { e ->
                        emit(
                            AlbumDetailUiState(
                                error = context.getString(R.string.error_loading_album, e.localizedMessage ?: ""),
                                isLoading = false
                            )
                        )
                    }
                    .collect { newState ->
                        _uiState.value = newState
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = context.getString(R.string.error_loading_album, e.localizedMessage ?: ""),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun com.lostf1sh.pixelplayeross.data.network.deezer.DeezerImage.coverXl(): String? {
        return this.full ?: this.large
    }

    fun toggleAlbumLike() {
        val albumId = loadedAlbumId ?: return
        val currentLiked = _uiState.value.isLiked
        
        // Optimistic update
        _uiState.update { it.copy(isLiked = !currentLiked) }
        
        viewModelScope.launch {
            val success = if (currentLiked) {
                deezerRepository.unlikeAlbum(albumId)
            } else {
                deezerRepository.likeAlbum(albumId)
            }
            
            if (!success) {
                // Revert on failure
                _uiState.update { it.copy(isLiked = currentLiked) }
            } else {
                // Sync the database to reflect the change in the library tab immediately
                playlistPreferencesRepository.syncLovedAlbums()
            }
        }
    }

    /** Re-attempts loading the album after a failure (wired to the error-state retry button). */
    fun retry() {
        loadedAlbumId?.let { loadAlbumData(it) }
    }

    fun update(songs: List<Song>) {
        _uiState.update {
            it.copy(
                isLoading = false,
                songs = songs
            )
        }
    }
}
