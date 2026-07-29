package com.lostf1sh.pixelplayeross.data.repository

import androidx.paging.PagingData
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.model.Album
import com.lostf1sh.pixelplayeross.data.model.Artist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongs(): Flow<List<Song>>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artistId: Long): Flow<List<Song>>
    suspend fun searchSongs(query: String): List<Song>
    fun getSongById(songId: Long): Flow<Song?>
    fun getSongsByIds(songIds: List<Long>): Flow<List<Song>>
    fun getArtistById(artistId: Long): Flow<Artist?>
    suspend fun getArtistIdByName(name: String): Long?
    fun getPaginatedArtists(
        sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption,
        storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter
    ): Flow<PagingData<Artist>>
    fun getPaginatedSongs(sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter): Flow<PagingData<Song>>
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPaginatedSongs(): Flow<PagingData<Song>>
    fun getPaginatedFavoriteSongs(
        sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption,
        storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter
    ): Flow<PagingData<Song>>
    fun getPaginatedAlbums(
        sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption,
        storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter,
        minTracks: Int
    ): Flow<PagingData<Album>>
    suspend fun getFavoriteSongsOnce(
        storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter,
        sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption = com.lostf1sh.pixelplayeross.data.model.SortOption.LikedSongDateLiked
    ): List<Song>
    /** Reactive count of albums in the library. */
    fun getAlbumCountFlow(): Flow<Int>

    /** Reactive count of artists in the library. */
    fun getArtistCountFlow(): Flow<Int>

    fun getFavoriteSongCountFlow(
        storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter
    ): Flow<Int>
    fun getFavoriteSongIdsFlow(): Flow<Set<String>>
    suspend fun getFavoriteSongIdsOnce(): Set<String>
    suspend fun getFavoriteSongIdsSorted(
        sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption,
        storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter
    ): List<Long>
    suspend fun setFavoriteStatus(songId: String, isFavorite: Boolean)
    
    suspend fun updateSongMetadata(
        songId: Long,
        title: String,
        artist: String,
        artistId: Long,
        artistsJson: String?,
        album: String,
        genre: String?,
        trackNumber: Int,
        discNumber: Int?
    )
    suspend fun updateSongAlbumId(songId: Long, albumId: Long)
    suspend fun updateSongAlbumArt(songId: Long, albumArtUri: String?)
    
    suspend fun saveSong(song: Song)
}
