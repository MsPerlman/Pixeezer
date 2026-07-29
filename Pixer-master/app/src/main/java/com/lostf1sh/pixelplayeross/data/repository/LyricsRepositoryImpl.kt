package com.lostf1sh.pixelplayeross.data.repository

import com.lostf1sh.pixelplayeross.data.model.Lyrics
import com.lostf1sh.pixelplayeross.data.model.LyricsSourcePreference
import com.lostf1sh.pixelplayeross.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val deezerRepository: DeezerRepository
) : LyricsRepository {
    override suspend fun getStoredLyrics(song: Song): Pair<Lyrics, String>? = null

    override suspend fun getLyrics(
        song: Song,
        sourcePreference: LyricsSourcePreference,
        forceRefresh: Boolean
    ): Lyrics? {
        if (song.contentUriString.startsWith("deezer://")) {
            val trackId = song.id
            val response = deezerRepository.getLyrics(trackId)
            val items = response?.data?.attributes?.items
            
            if (items != null && items.isNotEmpty()) {
                val plainLyricsList = mutableListOf<String>()
                val syncedLyricsList = mutableListOf<com.lostf1sh.pixelplayeross.data.model.SyncedLine>()
                
                items.forEach { item ->
                    val line = item.line ?: ""
                    plainLyricsList.add(line)
                    
                    val millisecondsStr = item.milliseconds
                    if (!millisecondsStr.isNullOrBlank()) {
                        val time = millisecondsStr.toIntOrNull() ?: 0
                        syncedLyricsList.add(com.lostf1sh.pixelplayeross.data.model.SyncedLine(time = time, line = line))
                    }
                }
                
                return Lyrics(
                    plain = plainLyricsList.ifEmpty { null },
                    synced = syncedLyricsList.ifEmpty { null },
                    areFromRemote = true
                )
            }
        }
        return null
    }

    override suspend fun fetchFromRemote(song: Song): Result<Pair<Lyrics, String>> {
        val lyrics = getLyrics(song, LyricsSourcePreference.API_FIRST, true)
        return if (lyrics != null) {
            Result.success(lyrics to (lyrics.plain?.joinToString("\n") ?: ""))
        } else {
            Result.failure(NoLyricsFoundException("No lyrics found on Deezer"))
        }
    }

    override suspend fun searchRemote(song: Song): Result<Pair<String, List<LyricsSearchResult>>> = 
        Result.failure(Exception("Not implemented"))

    override suspend fun searchRemoteByQuery(title: String, artist: String?): Result<Pair<String, List<LyricsSearchResult>>> = 
        Result.failure(Exception("Not implemented"))

    override suspend fun updateLyrics(songId: Long, lyricsContent: String) { }

    override suspend fun resetLyrics(songId: Long) { }

    override suspend fun resetAllLyrics() { }

    override fun clearCache() { }

    override suspend fun scanAndAssignLocalLrcFiles(
        songs: List<Song>,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): Int = 0
}