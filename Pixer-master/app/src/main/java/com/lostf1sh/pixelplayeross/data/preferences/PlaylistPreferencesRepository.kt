package com.lostf1sh.pixelplayeross.data.preferences

import com.lostf1sh.pixelplayeross.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

import com.lostf1sh.pixelplayeross.data.database.LocalPlaylistDao
import com.lostf1sh.pixelplayeross.data.database.PlaylistEntity
import com.lostf1sh.pixelplayeross.data.database.toPlaylist
import com.lostf1sh.pixelplayeross.data.repository.DeezerRepository
import kotlinx.coroutines.flow.map

import com.lostf1sh.pixelplayeross.data.database.MusicDao
import com.lostf1sh.pixelplayeross.data.database.FavoritesDao
import com.lostf1sh.pixelplayeross.data.database.SongEntity
import com.lostf1sh.pixelplayeross.data.database.FavoritesEntity
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerPlaylistDetailResponse
import com.lostf1sh.pixelplayeross.data.network.deezer.toAlbumEntity
import com.lostf1sh.pixelplayeross.data.network.deezer.toArtistEntity
import com.lostf1sh.pixelplayeross.data.network.deezer.toPlaylistEntity
import com.lostf1sh.pixelplayeross.data.network.deezer.toSongEntity
import com.lostf1sh.pixelplayeross.data.repository.DeezerGatewayRepository

@Singleton
class PlaylistPreferencesRepository @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val localPlaylistDao: LocalPlaylistDao,
    private val deezerRepository: DeezerRepository,
    private val deezerGatewayRepository: DeezerGatewayRepository,
    private val musicDao: MusicDao,
    private val favoritesDao: FavoritesDao
) {
    val userPlaylistsFlow: Flow<List<Playlist>> = localPlaylistDao.observePlaylistsWithSongs().map { entities ->
        entities.map { it.playlist.toPlaylist(it.songs.map { s -> s.songId }) }
    }
    val playlistSongOrderModesFlow: Flow<Map<String, String>> = userPreferencesRepository.playlistSongOrderModesFlow
    val playlistsSortOptionFlow: Flow<String> = userPreferencesRepository.playlistsSortOptionFlow

    val isSyncingLibraryFlow = kotlinx.coroutines.flow.MutableStateFlow(false)

    suspend fun syncUserPlaylists() {
        try {
            val response = deezerRepository.getUserPlaylists()
            val deezerPlaylists = response?.data?.included ?: return

            val entities = deezerPlaylists.map { deezerPlaylist ->
                PlaylistEntity(
                    id = "deezer_${deezerPlaylist.id}",
                    name = deezerPlaylist.attributes?.name ?: "Unknown Playlist",
                    coverImageUri = deezerPlaylist.attributes?.image?.medium,
                    source = "DEEZER",
                    nbTracks = deezerPlaylist.attributes?.nbTracks,
                    fans = deezerPlaylist.attributes?.fans,
                    isPublic = deezerPlaylist.attributes?.isPublic,
                    creatorName = deezerPlaylist.attributes?.creator?.name
                )
            }

            val newIds = entities.map { it.id }.toSet()
            val existingDeezerIds = localPlaylistDao.getPlaylistIdsBySource("DEEZER")

            entities.forEach { entity ->
                localPlaylistDao.upsertPlaylist(entity)
            }

            existingDeezerIds.forEach { existingId ->
                if (!newIds.contains(existingId)) {
                    localPlaylistDao.deletePlaylist(existingId)
                    localPlaylistDao.clearPlaylistSongs(existingId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncFavoritePlaylists() {
        if (!deezerGatewayRepository.isConfigured()) return
        try {
            val gwPlaylists = deezerGatewayRepository.getFavoritePlaylists()
            val entities = gwPlaylists.mapNotNull { it.toPlaylistEntity() }

            entities.forEach { localPlaylistDao.upsertPlaylist(it) }

            val newIds = entities.map { it.id }.toSet()
            val existingIds = localPlaylistDao.getPlaylistIdsBySource("DEEZER_FAVORITE")
            existingIds.forEach { existingId ->
                if (!newIds.contains(existingId)) {
                    localPlaylistDao.deletePlaylist(existingId)
                    localPlaylistDao.clearPlaylistSongs(existingId)
                }
            }
            android.util.Log.d("SyncDebug", "syncFavoritePlaylists() synced ${entities.size} playlists")
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncFavoritePlaylists() error: ${e.message}", e)
        }
    }

    suspend fun syncLovedTracks() {
        android.util.Log.d("SyncDebug", "syncLovedTracks() started")
        if (deezerGatewayRepository.isConfigured() && syncLovedTracksViaGateway()) {
            return
        }
        try {
            var currentPage = 1
            val limit = 50
            val allLovedSongs = mutableListOf<SongEntity>()
            val allFavorites = mutableListOf<FavoritesEntity>()

            while (true) {
                android.util.Log.d("SyncDebug", "syncLovedTracks() fetching page $currentPage")
                val response = deezerRepository.getGcastLovedTracks(page = currentPage, limit = limit)
                val deezerTracks = response?.data?.included

                android.util.Log.d("SyncDebug", "syncLovedTracks() response tracks: ${deezerTracks?.size}")

                if (deezerTracks.isNullOrEmpty()) {
                    android.util.Log.d("SyncDebug", "syncLovedTracks() no more tracks, breaking loop")
                    break
                }

                deezerTracks.forEach { track ->
                    val trackIdStr = track.id
                    val songId = trackIdStr.toLongOrNull() ?: return@forEach
                    val song = SongEntity(
                        id = songId,
                        title = track.attributes?.title ?: "Unknown Title",
                        artistName = track.attributes?.artistName ?: "Unknown Artist",
                        artistId = 0L,
                        albumName = track.attributes?.albumName ?: "Unknown Album",
                        albumId = 0L,
                        contentUriString = "deezer://track/$songId",
                        albumArtUriString = track.attributes?.image?.medium ?: track.attributes?.image?.large,
                        duration = (track.attributes?.duration ?: 0) * 1000L,
                        genre = null,
                        trackNumber = 0,
                        discNumber = 0,
                        filePath = "",
                        parentDirectoryPath = "",
                        sourceType = com.lostf1sh.pixelplayeross.data.database.SourceType.DEEZER,
                        isExplicit = track.attributes?.explicit ?: false
                    )
                    allLovedSongs.add(song)
                    allFavorites.add(FavoritesEntity(songId = songId, isFavorite = true, timestamp = System.currentTimeMillis() - (allFavorites.size * 1000L)))
                }

                if (deezerTracks.size < limit) {
                    break
                }
                currentPage++
            }

            android.util.Log.d("SyncDebug", "syncLovedTracks() fetching done. Total: ${allLovedSongs.size}")
            if (allLovedSongs.isNotEmpty()) {
                android.util.Log.d("SyncDebug", "syncLovedTracks() inserting into database...")

                musicDao.insertArtistsIgnoreConflicts(listOf(
                    com.lostf1sh.pixelplayeross.data.database.ArtistEntity(
                        id = 0L,
                        name = "Unknown Artist",
                        trackCount = 0
                    )
                ))
                musicDao.insertAlbumsIgnoreConflicts(listOf(
                    com.lostf1sh.pixelplayeross.data.database.AlbumEntity(
                        id = 0L,
                        title = "Unknown Album",
                        artistName = "Unknown Artist",
                        artistId = 0L,
                        albumArtUriString = null,
                        songCount = 0,
                        dateAdded = System.currentTimeMillis(),
                        year = 0
                    )
                ))

                musicDao.insertSongsIgnoreConflicts(allLovedSongs)
                favoritesDao.insertAll(allFavorites)
                android.util.Log.d("SyncDebug", "syncLovedTracks() inserted successfully.")
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncLovedTracks() error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private suspend fun syncLovedTracksViaGateway(): Boolean {
        return try {
            val favoriteIds = deezerGatewayRepository.getAllFavoriteTrackIds()
            if (favoriteIds.isEmpty()) return false
            val dateBySngId = favoriteIds.associate { it.sngId to it.dateFavorite }

            val songs = deezerGatewayRepository.hydrateSongs(favoriteIds.map { it.sngId })
            if (songs.isEmpty()) return false

            val allLovedSongs = songs.map { it.toSongEntity() }
            val allFavorites = songs.mapNotNull { gwSong ->
                val songId = gwSong.sngId.toLongOrNull() ?: return@mapNotNull null
                FavoritesEntity(songId = songId, isFavorite = true, timestamp = (dateBySngId[gwSong.sngId] ?: 0L) * 1000L)
            }

            musicDao.insertArtistsIgnoreConflicts(listOf(
                com.lostf1sh.pixelplayeross.data.database.ArtistEntity(id = 0L, name = "Unknown Artist", trackCount = 0)
            ))
            musicDao.insertAlbumsIgnoreConflicts(listOf(
                com.lostf1sh.pixelplayeross.data.database.AlbumEntity(
                    id = 0L, title = "Unknown Album", artistName = "Unknown Artist", artistId = 0L,
                    albumArtUriString = null, songCount = 0, dateAdded = System.currentTimeMillis(), year = 0
                )
            ))

            musicDao.insertSongsIgnoreConflicts(allLovedSongs)
            favoritesDao.insertAll(allFavorites)
            android.util.Log.d("SyncDebug", "syncLovedTracksViaGateway() inserted ${allLovedSongs.size} tracks (of ${favoriteIds.size} favorited)")
            true
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncLovedTracksViaGateway() error: ${e.message}", e)
            false
        }
    }

    suspend fun syncLovedAlbums() {
        android.util.Log.d("SyncDebug", "syncLovedAlbums() started")
        if (deezerGatewayRepository.isConfigured() && syncLovedAlbumsViaGateway()) {
            return
        }
        try {
            var currentStart = 0
            val limit = 50
            val allLovedAlbums = mutableListOf<com.lostf1sh.pixelplayeross.data.database.AlbumEntity>()
            val seenAlbumIds = mutableSetOf<Long>()

            while (true) {
                android.util.Log.d("SyncDebug", "syncLovedAlbums() fetching start $currentStart")
                val response = deezerRepository.getGcastLovedAlbums(start = currentStart, limit = limit)
                val deezerAlbums = response?.data?.included

                if (deezerAlbums.isNullOrEmpty()) {
                    break
                }

                var newAlbumsInPage = 0
                deezerAlbums.forEach { albumItem ->
                    val albumIdStr = albumItem.id
                    val albumId = albumIdStr?.toLongOrNull() ?: return@forEach
                    if (!seenAlbumIds.add(albumId)) return@forEach
                    newAlbumsInPage++
                    val albumEntity = com.lostf1sh.pixelplayeross.data.database.AlbumEntity(
                        id = albumId,
                        title = albumItem.attributes?.name ?: "Unknown Album",
                        artistName = albumItem.attributes?.artist?.name ?: "Unknown Artist",
                        artistId = albumItem.attributes?.artist?.id ?: 0L,
                        albumArtUriString = albumItem.attributes?.image?.let { it.full ?: it.large ?: it.medium },
                        songCount = albumItem.attributes?.nbTracks ?: 0,
                        dateAdded = System.currentTimeMillis(),
                        year = albumItem.attributes?.releaseDate?.take(4)?.toIntOrNull() ?: 0
                    )
                    allLovedAlbums.add(albumEntity)
                }

                if (newAlbumsInPage == 0) {
                    android.util.Log.d("SyncDebug", "syncLovedAlbums() page at start $currentStart returned no new albums, stopping")
                    break
                }

                if (deezerAlbums.size < limit) {
                    break
                }
                currentStart += limit
            }

            if (allLovedAlbums.isNotEmpty()) {
                android.util.Log.d("SyncDebug", "syncLovedAlbums() inserting ${allLovedAlbums.size} into database...")
                val artistsToInsert = allLovedAlbums.map {
                    com.lostf1sh.pixelplayeross.data.database.ArtistEntity(
                        id = it.artistId,
                        name = it.artistName,
                        trackCount = 0
                    )
                }.distinctBy { it.id }
                musicDao.insertArtists(artistsToInsert)

                musicDao.insertAlbums(allLovedAlbums)

                val lovedAlbumIds = allLovedAlbums.map { it.id }
                musicDao.deleteUnlovedAlbums(lovedAlbumIds)

                android.util.Log.d("SyncDebug", "syncLovedAlbums() inserted successfully.")
            } else {
                musicDao.deleteUnlovedAlbums(emptyList())
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncLovedAlbums() error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private suspend fun syncLovedAlbumsViaGateway(): Boolean {
        return try {
            val gwAlbums = deezerGatewayRepository.getFavoriteAlbums()
            if (gwAlbums.isEmpty()) return false

            val allLovedAlbums = gwAlbums.mapNotNull { it.toAlbumEntity() }
            if (allLovedAlbums.isEmpty()) return false

            val artistsToInsert = allLovedAlbums.map {
                com.lostf1sh.pixelplayeross.data.database.ArtistEntity(id = it.artistId, name = it.artistName, trackCount = 0)
            }.distinctBy { it.id }
            musicDao.insertArtists(artistsToInsert)
            musicDao.insertAlbums(allLovedAlbums)
            musicDao.deleteUnlovedAlbums(allLovedAlbums.map { it.id })

            android.util.Log.d("SyncDebug", "syncLovedAlbumsViaGateway() inserted ${allLovedAlbums.size} albums")
            true
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncLovedAlbumsViaGateway() error: ${e.message}", e)
            false
        }
    }

    suspend fun syncLovedArtists() {
        if (deezerGatewayRepository.isConfigured() && syncLovedArtistsViaGateway()) {
            return
        }
        try {
            val response = deezerRepository.getGcastLovedArtists()
            if (response != null && response.data != null && response.data.included != null) {
                val lovedArtists = response.data.included.mapNotNull { item ->
                    item.attributes?.let { attr ->
                        com.lostf1sh.pixelplayeross.data.database.ArtistEntity(
                            id = attr.id,
                            name = attr.name ?: "Unknown Artist",
                            trackCount = 0,
                            imageUrl = attr.pictures?.medium,
                            fanCount = attr.nbFans
                        )
                    }
                }

                if (lovedArtists.isNotEmpty()) {
                    android.util.Log.d("SyncDebug", "syncLovedArtists() inserting ${lovedArtists.size} into database...")
                    musicDao.insertArtists(lovedArtists)

                    val lovedArtistIds = lovedArtists.map { it.id }
                    musicDao.deleteUnlovedArtists(lovedArtistIds)

                    android.util.Log.d("SyncDebug", "syncLovedArtists() inserted successfully.")
                } else {
                    musicDao.deleteUnlovedArtists(emptyList())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncLovedArtists() error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private suspend fun syncLovedArtistsViaGateway(): Boolean {
        return try {
            val gwArtists = deezerGatewayRepository.getFavoriteArtists()
            if (gwArtists.isEmpty()) return false

            val lovedArtists = gwArtists.mapNotNull { it.toArtistEntity() }
            if (lovedArtists.isEmpty()) return false

            musicDao.insertArtists(lovedArtists)
            musicDao.deleteUnlovedArtists(lovedArtists.map { it.id })

            android.util.Log.d("SyncDebug", "syncLovedArtistsViaGateway() inserted ${lovedArtists.size} artists")
            true
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "syncLovedArtistsViaGateway() error: ${e.message}", e)
            false
        }
    }

    suspend fun optimisticLikePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            coverImageUri = playlist.coverImageUri,
            source = playlist.source,
            nbTracks = playlist.nbTracks,
            fans = playlist.fans,
            isPublic = playlist.isPublic,
            creatorName = playlist.creatorName
        )
        localPlaylistDao.upsertPlaylist(entity)
    }

    suspend fun optimisticUnlikePlaylist(playlistId: String) {
        localPlaylistDao.deletePlaylist(playlistId)
        localPlaylistDao.clearPlaylistSongs(playlistId)
    }

    suspend fun createPlaylist(
        name: String,
        songIds: List<String> = emptyList(),
        isQueueGenerated: Boolean = false,
        coverImageUri: String? = null,
        coverColorArgb: Int? = null,
        coverIconName: String? = null,
        coverShapeType: String? = null,
        coverShapeDetail1: Float? = null,
        coverShapeDetail2: Float? = null,
        coverShapeDetail3: Float? = null,
        coverShapeDetail4: Float? = null,
        customId: String? = null,
        source: String = "LOCAL"
    ): Playlist {
        throw NotImplementedError("Stubbed for Deezer")
    }

    suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>) {}
    suspend fun removeSongsFromPlaylist(playlistId: String, songIds: List<String>) {}
    suspend fun renamePlaylist(playlistId: String, newName: String) {}
    suspend fun deletePlaylist(playlistId: String) {}
    suspend fun updatePlaylistOrder(playlistId: String, newOrder: List<String>) {}
    suspend fun getPlaylistsOnce(): List<Playlist> = emptyList()
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {}
    suspend fun reorderSongsInPlaylist(playlistId: String, songIds: List<String>) {}
    suspend fun removeSongFromAllPlaylists(songId: String) {}
    suspend fun setPlaylistSongOrderMode(playlistId: String, mode: String) {}
    suspend fun updatePlaylist(playlist: Playlist) {}
    suspend fun addOrRemoveSongFromPlaylists(songId: String, playlistIds: List<String>): List<String> = emptyList()
    suspend fun setPlaylistsSortOption(sortOption: String) {}
    suspend fun updatePlaylistCover(
        playlistId: String,
        coverImageUri: String? = null,
        coverColorArgb: Int? = null,
        coverIconName: String? = null,
        coverShapeType: String? = null,
        d1: Float? = null,
        d2: Float? = null,
        d3: Float? = null,
        d4: Float? = null
    ) {}
    suspend fun createSmartPlaylist(
        name: String,
        smartRuleKey: String,
        coverColorArgb: Int? = null,
        coverIconName: String? = null,
        coverShapeType: String? = null,
        d1: Float? = null,
        d2: Float? = null,
        d3: Float? = null,
        d4: Float? = null
    ): Playlist {
        throw NotImplementedError("Stubbed")
    }
}
