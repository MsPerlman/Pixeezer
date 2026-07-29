package com.lostf1sh.pixelplayeross.data.repository
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import androidx.paging.PagingData
import com.lostf1sh.pixelplayeross.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

@Singleton
class MusicRepositoryImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val lyricsRepository: LyricsRepository,
    private val songRepository: SongRepository
) : MusicRepository {
    override fun getAudioFiles(): Flow<List<Song>> = songRepository.getSongs()
    override fun getPaginatedSongs(sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter): Flow<PagingData<Song>> = songRepository.getPaginatedSongs(sortOption, storageFilter)
    override fun getPaginatedAlbums( sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter , minTracks: Int  ): Flow<PagingData<Album>> = songRepository.getPaginatedAlbums(sortOption, storageFilter, minTracks)
    override fun getPaginatedArtists( sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): Flow<PagingData<Artist>> = songRepository.getPaginatedArtists(sortOption, storageFilter)
    override fun getPaginatedFavoriteSongs( sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): Flow<PagingData<Song>> = songRepository.getPaginatedFavoriteSongs(sortOption, storageFilter)
    override suspend fun getFavoriteSongsOnce( storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter, sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption ): List<Song> = songRepository.getFavoriteSongsOnce(storageFilter, sortOption)
    override suspend fun getFavoriteSongsPage( limit: Int, offset: Int, sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption , storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): List<Song> = emptyList()
    override fun getFavoriteSongCountFlow( storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): Flow<Int> = songRepository.getFavoriteSongCountFlow(storageFilter)
    override fun getAlbumCountFlow(): Flow<Int> = songRepository.getAlbumCountFlow()
    override fun getArtistCountFlow(): Flow<Int> = songRepository.getArtistCountFlow()
    override fun getSongCountFlow(): Flow<Int> = flowOf(0)
    override fun getCloudSongCountFlow(): Flow<Int> = flowOf(0)
    override suspend fun getRandomSongs(limit: Int): List<Song> = emptyList()
    override suspend fun getSongsPage( limit: Int, offset: Int, sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption , storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): List<Song> = emptyList()
    override suspend fun getAlbumsPage( limit: Int, offset: Int, sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption , storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter , minTracks: Int  ): List<Album> = emptyList()
    override suspend fun getArtistsPage( limit: Int, offset: Int, sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption , storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): List<Artist> = emptyList()
    override suspend fun getFirstPlayableSong(): Song? = null
    override fun getAlbums( storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter , minTracks: Int  ): Flow<List<Album>> = flowOf(emptyList())
    override fun getArtists( storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): Flow<List<Artist>> = flowOf(emptyList())
    override suspend fun getAllSongsOnce(): List<Song> = emptyList()
    override fun getDistinctAlbumArtSongs(): Flow<List<Song>> = flowOf(emptyList())
    override fun getHomeMixPreviewSongs(limit: Int): Flow<List<Song>> = flowOf(emptyList())
    override suspend fun getAllAlbumsOnce( storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter , minTracks: Int  ): List<Album> = emptyList()
    override suspend fun getAllArtistsOnce(): List<Artist> = emptyList()
    override fun getAlbumById(id: Long): Flow<Album?> = flowOf(null)
    override fun getSongsForAlbum(albumId: Long): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsForArtist(artistId: Long): Flow<List<Song>> = flowOf(emptyList())
    override fun getSongsByIds(songIds: List<String>): Flow<List<Song>> =
        songRepository.getSongsByIds(songIds.mapNotNull { it.toLongOrNull() })
    override suspend fun getSongByPath(path: String): Song? = null
    override suspend fun getAllUniqueAudioDirectories(): Set<String> = emptySet()
    override fun getAllUniqueAlbumArtUris(): Flow<List<Uri>> = flowOf(emptyList())
    override suspend fun invalidateCachesDependentOnAllowedDirectories() { }
    override fun searchSongs(query: String, titleOnly: Boolean ): Flow<List<Song>> = flowOf(emptyList())
    override fun searchAlbums(query: String, minTracks: Int ): Flow<List<Album>> = flowOf(emptyList())
    override fun searchArtists(query: String): Flow<List<Artist>> = flowOf(emptyList())
    override suspend fun searchPlaylists(query: String): List<Playlist> = emptyList()
    override fun searchAll(query: String, filterType: SearchFilterType): Flow<List<SearchResultItem>> = flowOf(emptyList())
    override suspend fun addSearchHistoryItem(query: String) { }
    override suspend fun getRecentSearchHistory(limit: Int): List<SearchHistoryItem> = emptyList()
    override suspend fun deleteSearchHistoryItemByQuery(query: String) { }
    override suspend fun clearSearchHistory() { }
    override fun getMusicByGenre(genreId: String): Flow<List<Song>> = flowOf(emptyList())
    override suspend fun toggleFavoriteStatus(songId: String): Boolean = false
    override suspend fun setFavoriteStatus(songId: String, isFavorite: Boolean) { songRepository.setFavoriteStatus(songId, isFavorite) }
    override suspend fun updateSongMetadata(songId: Long, title: String, artist: String, artistId: Long, artistsJson: String?, album: String, genre: String?, trackNumber: Int, discNumber: Int?) {
        songRepository.updateSongMetadata(songId, title, artist, artistId, artistsJson, album, genre, trackNumber, discNumber)
    }
    override suspend fun updateSongAlbumId(songId: Long, albumId: Long) {
        songRepository.updateSongAlbumId(songId, albumId)
    }
    override suspend fun updateSongAlbumArt(songId: Long, albumArtUri: String?) {
        songRepository.updateSongAlbumArt(songId, albumArtUri)
    }
    override suspend fun saveSong(song: Song) { songRepository.saveSong(song) }
    override suspend fun getFavoriteSongIdsOnce(): Set<String> = songRepository.getFavoriteSongIdsOnce()
    override fun getFavoriteSongIdsFlow(): Flow<Set<String>> = songRepository.getFavoriteSongIdsFlow()
    override fun getSong(songId: String): Flow<Song?> = flowOf(null)
    override fun getArtistById(artistId: Long): Flow<Artist?> = songRepository.getArtistById(artistId)
    override suspend fun getArtistIdByName(name: String): Long? = songRepository.getArtistIdByName(name)
    override fun getArtistsForSong(songId: Long): Flow<List<Artist>> = flowOf(emptyList())
    override fun getGenres(): Flow<List<com.lostf1sh.pixelplayeross.data.model.Genre>> = flowOf(emptyList())
    override suspend fun getLyrics( song: Song, sourcePreference: LyricsSourcePreference , forceRefresh: Boolean  ): Lyrics? = lyricsRepository.getLyrics(song, sourcePreference, forceRefresh)
    override suspend fun getStoredLyrics(song: Song): Pair<Lyrics, String>? = lyricsRepository.getStoredLyrics(song)
    override suspend fun getLyricsFromRemote(song: Song): Result<Pair<Lyrics, String>> = lyricsRepository.fetchFromRemote(song)
    override suspend fun searchRemoteLyrics(song: Song): Result<Pair<String, List<LyricsSearchResult>>> = lyricsRepository.searchRemote(song)
    override suspend fun searchRemoteLyricsByQuery(title: String, artist: String? ): Result<Pair<String, List<LyricsSearchResult>>> = lyricsRepository.searchRemoteByQuery(title, artist)
    override suspend fun updateLyrics(songId: Long, lyrics: String) = lyricsRepository.updateLyrics(songId, lyrics)
    override suspend fun resetLyrics(songId: Long) = lyricsRepository.resetLyrics(songId)
    override suspend fun resetAllLyrics() = lyricsRepository.resetAllLyrics()
    override fun getMusicFolders( storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter  ): Flow<List<com.lostf1sh.pixelplayeross.data.model.MusicFolder>> = flowOf(emptyList())
    override suspend fun deleteById(id: Long) { }
    override suspend fun getSongIdsSorted( sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter ): List<Long> = emptyList()
    override suspend fun getFavoriteSongIdsSorted( sortOption: com.lostf1sh.pixelplayeross.data.model.SortOption, storageFilter: com.lostf1sh.pixelplayeross.data.model.StorageFilter ): List<Long> = songRepository.getFavoriteSongIdsSorted(sortOption, storageFilter)
    override suspend fun getSongIdByContentUri(contentUri: String): Long? = null
}
