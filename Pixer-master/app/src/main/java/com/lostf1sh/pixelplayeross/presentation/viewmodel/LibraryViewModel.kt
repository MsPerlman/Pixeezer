package com.lostf1sh.pixelplayeross.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.lifecycle.ViewModel
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerUserMeResponse
import com.lostf1sh.pixelplayeross.data.network.deezer.toDeezerTrack
import com.lostf1sh.pixelplayeross.data.preferences.PinnedHomeItem
import com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType
import com.lostf1sh.pixelplayeross.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryStateHolder: LibraryStateHolder,
    private val deezerRepository: com.lostf1sh.pixelplayeross.data.repository.DeezerRepository,
    private val deezerGatewayRepository: com.lostf1sh.pixelplayeross.data.repository.DeezerGatewayRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val songsPagingFlow = libraryStateHolder.songsPagingFlow.cachedIn(viewModelScope)

    val albumsPagingFlow = libraryStateHolder.albumsPagingFlow.cachedIn(viewModelScope)

    val artistsPagingFlow = libraryStateHolder.artistsPagingFlow.cachedIn(viewModelScope)

    val favoritesPagingFlow = libraryStateHolder.favoritesPagingFlow.cachedIn(viewModelScope)

    val favoriteSongCountFlow = libraryStateHolder.favoriteSongCountFlow

    val albumCountFlow = libraryStateHolder.albumCountFlow

    val artistCountFlow = libraryStateHolder.artistCountFlow

    val isLoadingLibrary = libraryStateHolder.isLoadingLibrary

    private val _deezerFlowConfigs = kotlinx.coroutines.flow.MutableStateFlow<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerMultiFlowConfigsResponse?>(null)
    val deezerFlowConfigs: kotlinx.coroutines.flow.StateFlow<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerMultiFlowConfigsResponse?> = _deezerFlowConfigs

    private val _deezerRecommendedPlaylists = kotlinx.coroutines.flow.MutableStateFlow<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerRecommendedPlaylistsResponse?>(null)
    val deezerRecommendedPlaylists: kotlinx.coroutines.flow.StateFlow<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerRecommendedPlaylistsResponse?> = _deezerRecommendedPlaylists

    private val _yourMixes = kotlinx.coroutines.flow.MutableStateFlow<List<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData>>(emptyList())
    val yourMixes: kotlinx.coroutines.flow.StateFlow<List<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData>> = _yourMixes

    val yourMixSongs: kotlinx.coroutines.flow.StateFlow<List<com.lostf1sh.pixelplayeross.data.model.Song>> = _yourMixes
        .map { mixes ->
            mixes.flatMap { mix ->
                mix.included.filter { it.type == "track" }.map { com.lostf1sh.pixelplayeross.presentation.screens.mapDeezerTrackToSong(it) }
            }.distinctBy { it.id }.take(50)
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    private val _yourDiscovery = kotlinx.coroutines.flow.MutableStateFlow<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData?>(null)
    val yourDiscovery: kotlinx.coroutines.flow.StateFlow<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData?> = _yourDiscovery

    private val _userProfile = kotlinx.coroutines.flow.MutableStateFlow<DeezerUserMeResponse?>(null)
    val userProfile: kotlinx.coroutines.flow.StateFlow<DeezerUserMeResponse?> = _userProfile

    val pinnedHomeItems: kotlinx.coroutines.flow.StateFlow<List<PinnedHomeItem>> = userPreferencesRepository.pinnedHomeItemsFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    init {
        val flowConfigsJob = viewModelScope.launch {
            val flowConfigs = deezerRepository.getMultiFlowConfigs()
            _deezerFlowConfigs.value = flowConfigs
        }
        val recommendedJob = viewModelScope.launch {
            val playlists = deezerRepository.getRecommendedPlaylists()
            _deezerRecommendedPlaylists.value = playlists
        }
        val yourMixesJob = viewModelScope.launch {
            val gatewaySeeds = if (deezerGatewayRepository.isConfigured()) {
                deezerGatewayRepository.getHomeMixSeeds()
            } else {
                emptyList()
            }
            if (gatewaySeeds.isNotEmpty()) {
                val mixes = gatewaySeeds.mapIndexedNotNull { index, seed ->
                    val tracklist = deezerGatewayRepository.getMixTracklist(seed.sngId)
                    if (tracklist.isEmpty()) return@mapIndexedNotNull null
                    com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData(
                        id = "your_mix_${index + 1}",
                        attributes = com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistAttributes(
                            name = listOfNotNull(seed.title, seed.artistName).joinToString(" — ").ifBlank { "Mix" }
                        ),
                        included = tracklist.map { it.toDeezerTrack() }
                    )
                }
                _yourMixes.value = mixes
            } else {
                val mixes = mutableListOf<com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailData>()
                for (i in 1..12) {
                    val mix = deezerRepository.getInspiredByMix(i)?.data
                    if (mix != null) {
                        mixes.add(mix.copy(id = "your_mix_$i"))
                    }
                }
                _yourMixes.value = mixes
            }
        }
        val discoveryJob = viewModelScope.launch {
            val discovery = deezerRepository.getDiscoveryMix()?.data
            _yourDiscovery.value = discovery
        }
        viewModelScope.launch {
            _userProfile.value = deezerRepository.getUserMe().getOrNull()
        }
        viewModelScope.launch {
            joinAll(flowConfigsJob, recommendedJob, yourMixesJob, discoveryJob)
            seedPinnedHomeItemsIfNeeded()
            userPreferencesRepository.removePinnedHomeItemsOfType(PinnedItemType.MIX)
        }
    }

    private suspend fun seedPinnedHomeItemsIfNeeded() {
        val defaults = mutableListOf<PinnedHomeItem>()
        _deezerFlowConfigs.value?.data?.included?.firstOrNull()?.let { config ->
            defaults += PinnedHomeItem(
                type = PinnedItemType.FLOW,
                id = config.id,
                label = config.attributes?.title ?: "Flow",
                imageUrl = config.attributes?.images?.square?.medium ?: config.attributes?.images?.square?.small
            )
        }
        _yourDiscovery.value?.let { discovery ->
            defaults += PinnedHomeItem(
                type = PinnedItemType.DISCOVERY,
                id = "discovery",
                label = discovery.attributes?.title ?: discovery.attributes?.name ?: "Discovery",
                imageUrl = discovery.attributes?.image?.medium ?: discovery.attributes?.image?.small,
                fallbackTrackArtUrls = discovery.included.mapNotNull { it.attributes?.image?.small }.take(4)
            )
        }
        _deezerRecommendedPlaylists.value?.data?.included?.firstOrNull()?.let { playlist ->
            defaults += PinnedHomeItem(
                type = PinnedItemType.PLAYLIST,
                id = playlist.id,
                label = playlist.attributes?.title ?: playlist.attributes?.name ?: "Playlist",
                imageUrl = playlist.attributes?.image?.medium ?: playlist.attributes?.image?.small
            )
        }
        if (defaults.isNotEmpty()) {
            userPreferencesRepository.seedPinnedHomeItemsIfNeeded(defaults)
        }
    }

    fun togglePin(item: PinnedHomeItem): Boolean {
        val current = pinnedHomeItems.value
        val alreadyPinned = current.any { it.type == item.type && it.id == item.id }
        if (!alreadyPinned && current.size >= MAX_PINNED_HOME_ITEMS) {
            return false
        }
        viewModelScope.launch {
            userPreferencesRepository.togglePinnedHomeItem(item)
        }
        return true
    }

    companion object {
        const val MAX_PINNED_HOME_ITEMS = 15
    }

    suspend fun getMultiFlowTracks(url: String): com.lostf1sh.pixelplayeross.data.network.deezer.DeezerMultiFlowResponse? {
        return deezerRepository.getMultiFlowTracks(url)
    }
}
