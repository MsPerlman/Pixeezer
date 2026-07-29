package com.lostf1sh.pixelplayeross.data.media
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class MetadataEditError { FILE_NOT_FOUND, NO_WRITE_PERMISSION, INVALID_INPUT, UNSUPPORTED_FORMAT, TAGLIB_ERROR, TIMEOUT, FILE_CORRUPTED, IO_ERROR, UNKNOWN }
data class CoverArtUpdate(val bytes: ByteArray? = null, val mimeType: String? = null, val isDeletion: Boolean = false)

@Singleton
class SongMetadataEditor @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun editSongMetadata(
        songId: Long, newTitle: String, newArtist: String, newAlbum: String,
        newAlbumArtist: String?, newComposer: String?, newGenre: String, newLyrics: String,
        newTrackNumber: Int, newDiscNumber: Int?, newReplayGainTrackGainDb: String?,
        newReplayGainAlbumGainDb: String?, coverArtUpdate: CoverArtUpdate?
    ): Result {
        return Result(success = true, updatedAlbumArtUri = null, error = null, errorMessage = null)
    }
    
    data class Result(
        val success: Boolean,
        val updatedAlbumArtUri: String? = null,
        val error: MetadataEditError? = null,
        val errorMessage: String? = null
    )
}
