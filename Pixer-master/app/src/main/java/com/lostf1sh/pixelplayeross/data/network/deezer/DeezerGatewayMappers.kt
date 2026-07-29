package com.lostf1sh.pixelplayeross.data.network.deezer

import com.lostf1sh.pixelplayeross.data.database.AlbumEntity
import com.lostf1sh.pixelplayeross.data.database.ArtistEntity
import com.lostf1sh.pixelplayeross.data.database.PlaylistEntity
import com.lostf1sh.pixelplayeross.data.database.SongEntity
import com.lostf1sh.pixelplayeross.data.database.SourceType
import com.lostf1sh.pixelplayeross.data.model.Song

private fun coverUrl(md5: String?): String? =
    md5?.takeIf { it.isNotBlank() }?.let { "https://e-cdns-images.dzcdn.net/images/cover/$it/1000x1000-000000-80-0-0.jpg" }

private fun artistPictureUrl(md5: String?): String? =
    md5?.takeIf { it.isNotBlank() }?.let { "https://e-cdns-images.dzcdn.net/images/artist/$it/1000x1000-000000-80-0-0.jpg" }

fun GwSong.toDeezerTrack(): DeezerTrack {
    val cover = coverUrl(albumPicture)
    return DeezerTrack(
        id = sngId,
        type = "track",
        attributes = DeezerTrackAttributes(
            title = title,
            artistName = artistName,
            albumName = albumTitle,
            duration = duration?.toIntOrNull() ?: 0,
            image = cover?.let { DeezerImage(small = it, medium = it, large = it, full = it) },
            explicitLyrics = (explicitTrackContent?.explicitLyricsStatus ?: 0) != 0
        )
    )
}

fun GwSong.toSongEntity(): SongEntity {
    val songId = sngId.toLongOrNull() ?: 0L
    return SongEntity(
        id = songId,
        title = title ?: "Unknown Title",
        artistName = artistName ?: "Unknown Artist",
        artistId = 0L,
        albumName = albumTitle ?: "Unknown Album",
        albumId = 0L,
        contentUriString = "deezer://track/$songId",
        albumArtUriString = coverUrl(albumPicture),
        duration = (duration?.toLongOrNull() ?: 0L) * 1000L,
        genre = null,
        trackNumber = 0,
        filePath = "",
        parentDirectoryPath = "",
        sourceType = SourceType.DEEZER,
        isExplicit = (explicitTrackContent?.explicitLyricsStatus ?: 0) != 0
    )
}

fun GwSong.toSong(): Song {
    val songId = sngId
    return Song(
        id = songId,
        title = title ?: "Unknown Title",
        artist = artistName ?: "Unknown Artist",
        artistId = artId?.toLongOrNull() ?: 0L,
        album = albumTitle ?: "Unknown Album",
        albumId = albId?.toLongOrNull() ?: 0L,
        path = "",
        contentUriString = "deezer://track/$songId",
        albumArtUriString = coverUrl(albumPicture),
        duration = (duration?.toLongOrNull() ?: 0L) * 1000L,
        mimeType = "audio/mpeg",
        bitrate = null,
        sampleRate = null,
        isExplicit = (explicitTrackContent?.explicitLyricsStatus ?: 0) != 0
    )
}

fun GwAlbumFavorite.toAlbumEntity(): AlbumEntity? {
    val id = albId?.toLongOrNull() ?: return null
    return AlbumEntity(
        id = id,
        title = albumTitle ?: "Unknown Album",
        artistName = artistName ?: "Unknown Artist",
        artistId = artId?.toLongOrNull() ?: 0L,
        albumArtUriString = coverUrl(albumPicture),
        songCount = nbSongs,
        dateAdded = System.currentTimeMillis(),
        year = releaseDate?.take(4)?.toIntOrNull() ?: 0
    )
}

fun GwArtistFavorite.toArtistEntity(): ArtistEntity? {
    val id = artId.toLongOrNull() ?: return null
    return ArtistEntity(
        id = id,
        name = artistName ?: "Unknown Artist",
        trackCount = 0,
        imageUrl = artistPictureUrl(artPicture),
        fanCount = nbFans
    )
}

fun GwPlaylistFavorite.toPlaylistEntity(): PlaylistEntity? {
    return PlaylistEntity(
        id = "deezer_fav_$playlistId",
        name = title ?: "Unknown Playlist",
        coverImageUri = coverUrl(playlistPicture),
        source = "DEEZER_FAVORITE",
        nbTracks = nbSongs,
        fans = null,
        isPublic = status != 0,
        creatorName = creatorName
    )
}
