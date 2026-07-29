package com.lostf1sh.pixelplayeross.data.network.deezer

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Deezer API.
 * Used primarily for fetching artist artwork.
 */
interface DeezerApiService {

    /**
     * Search for an artist by name.
     * @param query Artist name to search for
     * @param limit Maximum number of results to return
     * @return Search response containing list of matching artists
     */
    @GET("platform/generic/search/artist")
    suspend fun searchArtist(
        @Query("q") query: String,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerGenericSearchResponse

    @GET("platform/generic/search/track")
    suspend fun searchTrack(
        @Query("q") query: String,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerGenericSearchResponse

    @GET("platform/generic/search/playlist")
    suspend fun searchPlaylist(
        @Query("q") query: String,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerGenericSearchResponse

    @GET("platform/generic/search/album")
    suspend fun searchAlbum(
        @Query("q") query: String,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerGenericSearchResponseObject

    @GET("platform/generic/search/best-results")
    suspend fun searchBestResult(
        @Query("q") query: String,
        @retrofit2.http.Header("authorization") auth: String
    ): com.google.gson.JsonObject

    @GET("platform/generic/user/{user_id}/multiflow/default")
    suspend fun getMultiFlow(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerMultiFlowResponse

    @GET("platform/gcast/user/{user_id}/recommended-playlists")
    suspend fun getRecommendedPlaylists(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerRecommendedPlaylistsResponse

    @GET("platform/gcast/user/{user_id}/playlists?include=lyrics")
    suspend fun getUserPlaylists(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerRecommendedPlaylistsResponse

    @retrofit2.http.GET("platform/gcast/user/{user_id}/loved-tracks?include=lyrics")
    suspend fun getGcastLovedTracks(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("nb_items") limit: Int = 50
    ): DeezerPlaylistDetailResponse

    @retrofit2.http.POST("platform/generic/track/{track_id}/like")
    suspend fun likeTrack(
        @retrofit2.http.Path("track_id") trackId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @retrofit2.http.DELETE("platform/generic/track/{track_id}/like")
    suspend fun unlikeTrack(
        @retrofit2.http.Path("track_id") trackId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @GET("platform/gcast/track/{track_id}/streamUrls")
    suspend fun getStreamUrls(
        @retrofit2.http.Path("track_id") trackId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerStreamUrlsResponse

    @GET("user/me")
    suspend fun getUserMe(
        @Query("access_token") auth: String
    ): com.lostf1sh.pixelplayeross.data.network.deezer.DeezerUserMeResponse

    @GET("platform/generic/user/{user_id}/multiflow")
    suspend fun getMultiFlowConfigs(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerMultiFlowConfigsResponse

    @GET
    suspend fun getMultiFlowTracks(
        @retrofit2.http.Url url: String,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerMultiFlowResponse

    @GET("platform/gcast/user/{user_id}/search/recently-searched")
    suspend fun getRecentlySearched(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerGenericSearchResponseObject

    @GET("platform/generic/playlist/{playlist_id}")
    suspend fun getPlaylistTracks(
        @retrofit2.http.Path("playlist_id") playlistId: String,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("nb_items") limit: Int = 50,
        @retrofit2.http.Query("include") include: String = "lyrics"
    ): DeezerPlaylistDetailResponse

    @GET("platform/generic/lyrics/{track_id}")
    suspend fun getLyrics(
        @retrofit2.http.Path("track_id") trackId: String,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerLyricsResponse

    @GET("user/{user_id}/tracks")
    suspend fun getLovedTracks(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("limit") limit: Int,
        @retrofit2.http.Query("index") index: Int
    ): DeezerPlaylistDetailResponse

    @retrofit2.http.POST("user/me/tracks")
    suspend fun addTrackToFavorites(
        @retrofit2.http.Query("access_token") token: String,
        @retrofit2.http.Query("track_id") trackId: Long
    ): retrofit2.Response<Boolean>

    @retrofit2.http.DELETE("user/me/tracks")
    suspend fun removeTrackFromFavorites(
        @retrofit2.http.Query("access_token") token: String,
        @retrofit2.http.Query("track_id") trackId: Long
    ): retrofit2.Response<Boolean>
    @retrofit2.http.POST("platform/generic/playlist/{playlist_id}/like")
    suspend fun likePlaylist(
        @retrofit2.http.Path("playlist_id") playlistId: String,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @retrofit2.http.DELETE("platform/generic/playlist/{playlist_id}/like")
    suspend fun unlikePlaylist(
        @retrofit2.http.Path("playlist_id") playlistId: String,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @GET("track/{track_id}")
    suspend fun getTrackInfo(
        @retrofit2.http.Path("track_id") trackId: String,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<DeezerTrackInfoResponse>
    @retrofit2.http.GET("platform/generic/album/{album_id}")
    suspend fun getAlbumInfo(
        @retrofit2.http.Path("album_id") albumId: Long,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("include") include: String = "lyrics"
    ): DeezerAlbumMetadataResponse

    @retrofit2.http.POST("platform/generic/album/{album_id}/like")
    suspend fun likeAlbum(
        @retrofit2.http.Path("album_id") albumId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @retrofit2.http.DELETE("platform/generic/album/{album_id}/like")
    suspend fun unlikeAlbum(
        @retrofit2.http.Path("album_id") albumId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @retrofit2.http.GET("platform/gcast/user/{user_id}/loved-albums")
    suspend fun getGcastLovedAlbums(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("start") start: Int = 0,
        @retrofit2.http.Query("limit") limit: Int = 50
    ): DeezerLovedAlbumsResponse

    // --- Artist Endpoints ---

    @GET("artist/{artist_id}")
    suspend fun getArtistInfo(
        @retrofit2.http.Path("artist_id") artistId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerArtist

    @GET("platform/generic/artist/{artist_id}/albums")
    suspend fun getArtistAlbums(
        @retrofit2.http.Path("artist_id") artistId: Long,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("include") include: String = "lyrics"
    ): DeezerLovedAlbumsResponse

    @GET("platform/generic/artist/{artist_id}/similar-artists")
    suspend fun getSimilarArtists(
        @retrofit2.http.Path("artist_id") artistId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerArtistsListResponse

    @GET("platform/generic/artist/{artist_id}/top-tracks")
    suspend fun getArtistTopTracks(
        @retrofit2.http.Path("artist_id") artistId: Long,
        @retrofit2.http.Header("authorization") auth: String,
        @retrofit2.http.Query("include") include: String = "lyrics"
    ): DeezerPlaylistDetailResponse

    @retrofit2.http.POST("platform/generic/artist/{artist_id}/like")
    suspend fun likeArtist(
        @retrofit2.http.Path("artist_id") artistId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @retrofit2.http.DELETE("platform/generic/artist/{artist_id}/like")
    suspend fun unlikeArtist(
        @retrofit2.http.Path("artist_id") artistId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): retrofit2.Response<Unit>

    @GET("platform/gcast/user/{user_id}/loved-artists")
    suspend fun getGcastLovedArtists(
        @retrofit2.http.Path("user_id") userId: Long,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerArtistsListResponse

    // --- SmartTrackList Endpoints ---

    @GET("platform/generic/smarttracklist/inspired-by-{index}?include=lyrics")
    suspend fun getInspiredByMix(
        @retrofit2.http.Path("index") index: Int,
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerPlaylistDetailResponse

    @GET("platform/generic/smarttracklist/discovery?include=lyrics")
    suspend fun getDiscoveryMix(
        @retrofit2.http.Header("authorization") auth: String
    ): DeezerPlaylistDetailResponse
}
