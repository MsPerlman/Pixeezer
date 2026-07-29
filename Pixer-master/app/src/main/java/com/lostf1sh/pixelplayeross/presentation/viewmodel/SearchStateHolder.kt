package com.lostf1sh.pixelplayeross.presentation.viewmodel

import com.lostf1sh.pixelplayeross.data.model.SearchFilterType
import com.lostf1sh.pixelplayeross.data.model.SearchHistoryItem
import com.lostf1sh.pixelplayeross.data.model.SearchResultItem
import com.lostf1sh.pixelplayeross.data.repository.MusicRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.FlowPreview

/**
 * Manages search state and operations.
 * Extracted from PlayerViewModel to improve modularity.
 *
 * Responsibilities:
 * - Search query execution
 * - Search filter management
 * - Search history CRUD operations
 */
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerApiService
import kotlinx.coroutines.async
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.model.Album
import com.lostf1sh.pixelplayeross.data.model.Artist
import com.lostf1sh.pixelplayeross.data.model.Playlist

import com.lostf1sh.pixelplayeross.data.repository.DeezerRepository

@Singleton
class SearchStateHolder @Inject constructor(
    private val musicRepository: MusicRepository,
    private val deezerRepository: DeezerRepository
) {
    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }

    private data class SearchRequest(
        val query: String,
        val requestId: Long,
    )

    // Search State
    private val _searchResults = MutableStateFlow<ImmutableList<SearchResultItem>>(persistentListOf())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _bestSearchResults = MutableStateFlow<ImmutableList<SearchResultItem>>(persistentListOf())
    val bestSearchResults = _bestSearchResults.asStateFlow()

    private val _recentlySearched = MutableStateFlow<ImmutableList<SearchResultItem>>(persistentListOf())
    val recentlySearched = _recentlySearched.asStateFlow()

    private val _selectedSearchFilter = MutableStateFlow(SearchFilterType.ALL)
    val selectedSearchFilter = _selectedSearchFilter.asStateFlow()

    private val _searchHistory = MutableStateFlow<ImmutableList<SearchHistoryItem>>(persistentListOf())
    val searchHistory = _searchHistory.asStateFlow()

    private val searchRequests = MutableSharedFlow<SearchRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val latestSearchRequestId = AtomicLong(0L)

    private var scope: CoroutineScope? = null
    private var searchJob: Job? = null

    /**
     * Initialize with ViewModel scope.
     */
    fun initialize(scope: CoroutineScope) {
        this.scope = scope
        observeSearchRequests()
        fetchRecentlySearched()
    }

    private fun fetchRecentlySearched() {
        scope?.launch {
            try {
                val res = deezerRepository.getRecentlySearched()
                val items = res?.data?.included?.mapNotNull { mapDeezerSearchItem(it) }
                if (items != null) {
                    _recentlySearched.value = items.toImmutableList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchRequests() {
        searchJob?.cancel()
        searchJob = scope?.launch {
            searchRequests
                .debounce(SEARCH_DEBOUNCE_MS)
                .collectLatest { request ->
                    _isSearching.value = true

                    val normalizedQuery = request.query.trim().lowercase()
                    if (normalizedQuery.isBlank()) {
                        if (request.requestId == latestSearchRequestId.get()) {
                            _searchResults.value = persistentListOf()
                            _bestSearchResults.value = persistentListOf()
                            _isSearching.value = false
                        }
                        return@collectLatest
                    }

                    try {
                        val currentFilter = _selectedSearchFilter.value
                        
                        val bestResultDeferred = scope?.async {
                            try {
                                android.util.Log.d("SearchDebug", "Fetching best result for: $normalizedQuery")
                                val res = deezerRepository.searchBestResult(normalizedQuery)
                                android.util.Log.d("SearchDebug", "Best result raw response: $res")
                                val dataElement = res?.get("data")
                                
                                val rawBestItems = mutableListOf<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerGenericSearchItem>()
                                
                                if (dataElement?.isJsonArray == true && dataElement.asJsonArray.size() > 0) {
                                    val bestItem = com.google.gson.Gson().fromJson(dataElement.asJsonArray.get(0), com.lostf1sh.pixelplayeross.data.network.deezer.DeezerGenericSearchItem::class.java)
                                    rawBestItems.add(bestItem)
                                    bestItem.included?.let { rawBestItems.addAll(it) }
                                } else if (dataElement?.isJsonObject == true) {
                                    val bestItem = com.google.gson.Gson().fromJson(dataElement, com.lostf1sh.pixelplayeross.data.network.deezer.DeezerGenericSearchItem::class.java)
                                    rawBestItems.add(bestItem)
                                    bestItem.included?.let { rawBestItems.addAll(it) }
                                }
                                
                                rawBestItems.mapNotNull { mapDeezerSearchItem(it) }
                            } catch (e: Exception) { null }
                        }


                        val tracksDeferred = scope?.async {
                            if (currentFilter == SearchFilterType.ALL || currentFilter == SearchFilterType.SONGS) {
                                try {
                                    deezerRepository.searchTrack(normalizedQuery)?.data?.mapNotNull { mapDeezerSearchItem(it) } ?: emptyList()
                                } catch (e: Exception) { emptyList() }
                            } else emptyList()
                        }
                        
                        val albumsDeferred = scope?.async {
                            if (currentFilter == SearchFilterType.ALL || currentFilter == SearchFilterType.ALBUMS) {
                                try {
                                    deezerRepository.searchAlbum(normalizedQuery)?.data?.included?.mapNotNull { mapDeezerSearchItem(it) } ?: emptyList()
                                } catch (e: Exception) { emptyList() }
                            } else emptyList()
                        }
                        
                        val artistsDeferred = scope?.async {
                            if (currentFilter == SearchFilterType.ALL || currentFilter == SearchFilterType.ARTISTS) {
                                try {
                                    deezerRepository.searchArtist(normalizedQuery)?.data?.mapNotNull { mapDeezerSearchItem(it) } ?: emptyList()
                                } catch (e: Exception) { emptyList() }
                            } else emptyList()
                        }
                        
                        val playlistsDeferred = scope?.async {
                            if (currentFilter == SearchFilterType.ALL || currentFilter == SearchFilterType.PLAYLISTS) {
                                try {
                                    deezerRepository.searchPlaylist(normalizedQuery)?.data?.mapNotNull { mapDeezerSearchItem(it) } ?: emptyList()
                                } catch (e: Exception) { emptyList() }
                            } else emptyList()
                        }
                        
                        val parsedBestResults = bestResultDeferred?.await() ?: emptyList()
                        val tracks = tracksDeferred?.await() ?: emptyList()
                        val albums = albumsDeferred?.await() ?: emptyList()
                        val artists = artistsDeferred?.await() ?: emptyList()
                        val playlists = playlistsDeferred?.await() ?: emptyList()
                        
                        val combinedResults = (tracks + albums + artists + playlists)

                        if (request.requestId != latestSearchRequestId.get()) {
                            return@collectLatest
                        }

                        _bestSearchResults.value = parsedBestResults.toImmutableList()
                        val immutableResults = combinedResults.toImmutableList()
                        if (_searchResults.value != immutableResults) {
                            _searchResults.value = immutableResults
                        }
                        _isSearching.value = false
                    } catch (_: CancellationException) {
                        // Superseded by a newer query; ignore.
                    } catch (e: Exception) {
                        if (request.requestId == latestSearchRequestId.get()) {
                            Timber.e(e, "Error performing search for query: $normalizedQuery")
                            _searchResults.value = persistentListOf()
                        }
                    }
                }
        }
    }

    fun updateSearchFilter(filterType: SearchFilterType) {
        _selectedSearchFilter.value = filterType
    }

    fun loadSearchHistory(limit: Int = 15) {
        scope?.launch {
            try {
                val history = withContext(Dispatchers.IO) {
                    musicRepository.getRecentSearchHistory(limit)
                }
                _searchHistory.value = history.toImmutableList()
            } catch (e: Exception) {
                Timber.e(e, "Error loading search history")
            }
        }
    }

    fun onSearchQuerySubmitted(query: String) {
        scope?.launch {
            if (query.isNotBlank()) {
                try {
                    withContext(Dispatchers.IO) {
                        musicRepository.addSearchHistoryItem(query)
                    }
                    loadSearchHistory()
                } catch (e: Exception) {
                    Timber.e(e, "Error adding search history item")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        if (query.isNotBlank()) {
            _isSearching.value = true
        } else {
            _isSearching.value = false
            if (_searchResults.value.isNotEmpty()) {
                _searchResults.value = persistentListOf()
            }
            if (_bestSearchResults.value.isNotEmpty()) {
                _bestSearchResults.value = persistentListOf()
            }
        }
        val newRequestId = latestSearchRequestId.incrementAndGet()
        searchRequests.tryEmit(SearchRequest(query, newRequestId))
    }

    fun performSearch(query: String) {
        val normalizedQuery = query.trim()

        val requestId = latestSearchRequestId.incrementAndGet()

        if (normalizedQuery.isBlank()) {
            _isSearching.value = false
            if (_searchResults.value.isNotEmpty()) {
                _searchResults.value = persistentListOf()
            }
            if (_bestSearchResults.value.isNotEmpty()) {
                _bestSearchResults.value = persistentListOf()
            }
        } else {
            _isSearching.value = true
        }

        searchRequests.tryEmit(SearchRequest(normalizedQuery, requestId))
    }

    private fun mapDeezerSearchItem(item: com.lostf1sh.pixelplayeross.data.network.deezer.DeezerGenericSearchItem): SearchResultItem? {
        val attr = item.attributes ?: return null
        return when (item.type) {
            "track" -> {
                val song = Song(
                    id = item.id,
                    title = attr.title ?: "Unknown",
                    artist = attr.artistName ?: "Unknown",
                    artistId = attr.artistId ?: 0L,
                    album = attr.albumName ?: "Unknown",
                    albumId = attr.albumId ?: 0L,
                    duration = (attr.duration ?: 0) * 1000L,
                    contentUriString = "deezer://track/${item.id}",
                    path = "deezer://track/${item.id}",
                    mimeType = "audio/mpeg",
                    albumArtUriString = attr.image?.medium ?: attr.image?.large ?: attr.image?.small,
                    highResAlbumArtUriString = attr.image?.large ?: attr.image?.full,
                    bitrate = 320,
                    sampleRate = 44100,
                    isExplicit = attr.explicitLyrics == true || attr.explicitContentLyrics == 1 || attr.explicit == true
                )
                SearchResultItem.SongItem(song)
            }
            "album" -> {
                val album = Album(
                    id = item.id.toLongOrNull() ?: 0L,
                    title = attr.title ?: attr.name ?: "Unknown",
                    artist = attr.artistName ?: attr.artist?.name ?: "Unknown",
                    songCount = attr.nbTracks ?: 0,
                    albumArtUriString = attr.image?.medium ?: attr.image?.large ?: attr.image?.small,
                    year = 0,
                    dateAdded = System.currentTimeMillis()
                )
                SearchResultItem.AlbumItem(album)
            }
            "artist" -> {
                val artist = Artist(
                    id = item.id.toLongOrNull() ?: 0L,
                    name = attr.name ?: "Unknown",
                    songCount = 0,
                    albumCount = 0,
                    fanCount = attr.nbFans?.toInt() ?: 0,
                    imageUrl = attr.pictures?.medium ?: attr.pictures?.large ?: attr.pictures?.small
                )
                SearchResultItem.ArtistItem(artist)
            }
            "playlist" -> {
                val playlist = Playlist(
                    id = "deezer_${item.id}",
                    name = attr.title ?: attr.name ?: "Unknown",
                    songIds = emptyList(),
                    nbTracks = attr.nbTracks,
                    fans = attr.fans,
                    isPublic = attr.isPublic,
                    creatorName = attr.creator?.name,
                    source = "DEEZER",
                    coverImageUri = attr.image?.large ?: attr.image?.medium ?: attr.image?.small ?: attr.pictures?.large ?: attr.pictures?.medium ?: attr.pictures?.small,
                    createdAt = System.currentTimeMillis()
                )
                SearchResultItem.PlaylistItem(playlist)
            }
            else -> null
        }
    }

    fun deleteSearchHistoryItem(query: String) {
        scope?.launch {
            try {
                withContext(Dispatchers.IO) {
                    musicRepository.deleteSearchHistoryItemByQuery(query)
                }
                loadSearchHistory()
            } catch (e: Exception) {
                Timber.e(e, "Error deleting search history item")
            }
        }
    }

    fun clearSearchHistory() {
        scope?.launch {
            try {
                withContext(Dispatchers.IO) {
                    musicRepository.clearSearchHistory()
                }
                _searchHistory.value = persistentListOf()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing search history")
            }
        }
    }

    fun onCleared() {
        searchJob?.cancel()
        scope = null
    }
}
