package com.lostf1sh.pixelplayeross.data.repository

import android.content.Context
import com.lostf1sh.pixelplayeross.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikedSongsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val cacheFile by lazy { File(context.filesDir, "liked_songs_cache.json") }
    
    private val _likedSongsFlow = MutableStateFlow<List<Song>>(emptyList())
    val likedSongsFlow: StateFlow<List<Song>> = _likedSongsFlow

    init {
        loadFromCache()
    }

    private fun loadFromCache() {
        if (!cacheFile.exists()) return
        try {
            val content = cacheFile.readText()
            val songs = json.decodeFromString<List<Song>>(content)
            _likedSongsFlow.value = songs
        } catch (e: Exception) {
            Timber.e(e, "Failed to read liked songs cache")
        }
    }

    suspend fun saveCache(songs: List<Song>) = withContext(Dispatchers.IO) {
        try {
            val content = json.encodeToString(songs)
            cacheFile.writeText(content)
            _likedSongsFlow.value = songs
        } catch (e: Exception) {
            Timber.e(e, "Failed to save liked songs cache")
        }
    }

    suspend fun addSongToCache(song: Song) = withContext(Dispatchers.IO) {
        val current = _likedSongsFlow.value.toMutableList()
        if (!current.any { it.id == song.id }) {
            current.add(song.copy(isFavorite = true))
            saveCache(current)
        }
    }

    suspend fun removeSongFromCache(songId: String) = withContext(Dispatchers.IO) {
        val current = _likedSongsFlow.value.toMutableList()
        if (current.removeAll { it.id == songId }) {
            saveCache(current)
        }
    }
}
