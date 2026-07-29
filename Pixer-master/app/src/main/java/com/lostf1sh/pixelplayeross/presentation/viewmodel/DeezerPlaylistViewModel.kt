package com.lostf1sh.pixelplayeross.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.data.model.SortOption
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailResponse
import com.lostf1sh.pixelplayeross.data.repository.DeezerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeezerPlaylistViewModel @Inject constructor(
    private val deezerRepository: DeezerRepository
) : ViewModel() {

    private val _playlistDetails = MutableStateFlow<DeezerPlaylistDetailResponse?>(null)
    val playlistDetails: StateFlow<DeezerPlaylistDetailResponse?> = _playlistDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow<SortOption>(SortOption.SongDefaultOrder)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = deezerRepository.getPlaylistTracks(playlistId)
            _playlistDetails.value = response
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: SortOption) {
        _sortOption.value = option
    }
}
