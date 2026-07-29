package com.lostf1sh.pixelplayeross.data.network.deezer

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class GwEnvelope(
    @SerializedName("error") val error: JsonElement? = null,
    @SerializedName("results") val results: JsonObject? = null
)

data class GwUserData(
    @SerializedName("SESSION_ID") val sessionId: String? = null,
    @SerializedName("USER") val user: GwUserDataUser? = null
)

data class GwUserDataUser(
    @SerializedName("USER_ID") val userId: Long? = null
)

data class GwFavoriteId(
    @SerializedName("SNG_ID") val sngId: String,
    @SerializedName("DATE_FAVORITE") val dateFavorite: Long = 0
)

data class GwFavoriteIdsResult(
    @SerializedName("data") val data: List<GwFavoriteId> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class GwSong(
    @SerializedName("SNG_ID") val sngId: String,
    @SerializedName("SNG_TITLE") val title: String? = null,
    @SerializedName("ART_ID") val artId: String? = null,
    @SerializedName("ART_NAME") val artistName: String? = null,
    @SerializedName("ALB_ID") val albId: String? = null,
    @SerializedName("ALB_TITLE") val albumTitle: String? = null,
    @SerializedName("ALB_PICTURE") val albumPicture: String? = null,
    @SerializedName("DURATION") val duration: String? = null,
    @SerializedName("EXPLICIT_TRACK_CONTENT") val explicitTrackContent: GwExplicitContent? = null
)

data class GwExplicitContent(
    @SerializedName("EXPLICIT_LYRICS_STATUS") val explicitLyricsStatus: Int = 0
)

data class GwSongListResult(
    @SerializedName("data") val data: List<GwSong> = emptyList()
)

data class GwAlbumFavorite(
    @SerializedName("ALB_ID") val albId: String? = null,
    @SerializedName("ALB_TITLE") val albumTitle: String? = null,
    @SerializedName("ALB_PICTURE") val albumPicture: String? = null,
    @SerializedName("ART_ID") val artId: String? = null,
    @SerializedName("ART_NAME") val artistName: String? = null,
    @SerializedName("NB_SONG") val nbSongs: Int = 0,
    @SerializedName("PHYSICAL_RELEASE_DATE") val releaseDate: String? = null
)

data class GwAlbumFavoritesResult(
    @SerializedName("data") val data: List<GwAlbumFavorite> = emptyList()
)

data class GwArtistFavorite(
    @SerializedName("ART_ID") val artId: String,
    @SerializedName("ART_NAME") val artistName: String? = null,
    @SerializedName("ART_PICTURE") val artPicture: String? = null,
    @SerializedName("NB_FAN") val nbFans: Int = 0
)

data class GwArtistFavoritesResult(
    @SerializedName("data") val data: List<GwArtistFavorite> = emptyList()
)

data class GwPlaylistFavorite(
    @SerializedName("PLAYLIST_ID") val playlistId: String,
    @SerializedName("TITLE") val title: String? = null,
    @SerializedName("PLAYLIST_PICTURE") val playlistPicture: String? = null,
    @SerializedName("NB_SONG") val nbSongs: Int = 0,
    @SerializedName("PARENT_USERNAME") val creatorName: String? = null,
    @SerializedName("STATUS") val status: Int = 0
)

data class GwPlaylistFavoritesResult(
    @SerializedName("data") val data: List<GwPlaylistFavorite> = emptyList()
)

data class GwPageResult(
    @SerializedName("sections") val sections: List<GwPageSection>? = null
)

data class GwPageSection(
    @SerializedName("title") val title: String? = null,
    @SerializedName("items") val items: List<GwPageItem>? = null
)

data class GwPageItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("data") val data: GwSong? = null
)
