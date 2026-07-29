package com.lostf1sh.pixelplayeross.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerGatewayApiService
import com.lostf1sh.pixelplayeross.data.network.deezer.GwAlbumFavorite
import com.lostf1sh.pixelplayeross.data.network.deezer.GwAlbumFavoritesResult
import com.lostf1sh.pixelplayeross.data.network.deezer.GwArtistFavorite
import com.lostf1sh.pixelplayeross.data.network.deezer.GwArtistFavoritesResult
import com.lostf1sh.pixelplayeross.data.network.deezer.GwFavoriteId
import com.lostf1sh.pixelplayeross.data.network.deezer.GwFavoriteIdsResult
import com.lostf1sh.pixelplayeross.data.network.deezer.GwPageResult
import com.lostf1sh.pixelplayeross.data.network.deezer.GwPlaylistFavorite
import com.lostf1sh.pixelplayeross.data.network.deezer.GwPlaylistFavoritesResult
import com.lostf1sh.pixelplayeross.data.network.deezer.GwSong
import com.lostf1sh.pixelplayeross.data.network.deezer.GwSongListResult
import com.lostf1sh.pixelplayeross.data.network.deezer.GwUserData
import com.lostf1sh.pixelplayeross.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeezerGatewayRepository @Inject constructor(
    private val api: DeezerGatewayApiService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()

    private val apiKey = "4VCYIJUCDLOUELGD1V8WBVYBNVDYOXEWSLLZDONGBBDFVXTZJRXPR29JRLQFO6ZE"

    private data class Session(val sid: String, val userId: Long)

    private val sessionMutex = Mutex()
    private var cachedSession: Session? = null

    private suspend fun arl(): String? =
        userPreferencesRepository.deezerGatewayArlFlow.firstOrNull()?.takeIf { it.isNotBlank() }

    suspend fun isConfigured(): Boolean = arl() != null

    /**
     * Bootstraps a fresh gateway session from just the arl cookie, the same way Deezer's own
     * web player (and every unofficial client) does: a single deezer.getUserData call against
     * the web gateway hands back a session id and the account's numeric user id, with no need
     * for anything captured out of the native mobile app.
     */
    private suspend fun bootstrapSession(arl: String): Session? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.deezer.com/ajax/gw-light.php?method=deezer.getUserData&input=3&api_version=1.0&api_token=")
                .header("Cookie", "arl=$arl")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val envelope = gson.fromJson(response.body?.string(), com.lostf1sh.pixelplayeross.data.network.deezer.GwEnvelope::class.java)
                val userData = envelope?.results?.let { gson.fromJson(it, GwUserData::class.java) } ?: return@withContext null
                val sessionId = userData.sessionId ?: return@withContext null
                val userId = userData.user?.userId ?: return@withContext null
                Session(sid = sessionId, userId = userId)
            }
        } catch (e: Exception) {
            Timber.e(e, "DeezerGateway bootstrapSession exception")
            null
        }
    }

    private suspend fun ensureSession(arl: String, forceRefresh: Boolean = false): Session? {
        if (!forceRefresh) cachedSession?.let { return it }
        return sessionMutex.withLock {
            if (!forceRefresh) cachedSession?.let { return@withLock it }
            bootstrapSession(arl)?.also { cachedSession = it }
        }
    }

    private suspend fun currentUserId(): Long? {
        val arlValue = arl() ?: return null
        return ensureSession(arlValue)?.userId
    }

    private fun hasError(envelopeError: com.google.gson.JsonElement?): Boolean {
        if (envelopeError == null) return false
        return when {
            envelopeError.isJsonObject -> envelopeError.asJsonObject.size() > 0
            envelopeError.isJsonArray -> envelopeError.asJsonArray.size() > 0
            else -> false
        }
    }

    private suspend fun call(
        method: String,
        body: Map<String, Any?> = emptyMap(),
        gatewayInput: String? = null,
        isRetry: Boolean = false
    ): JsonObject? {
        val arlValue = arl() ?: return null
        val session = ensureSession(arlValue, forceRefresh = isRetry) ?: return null
        return try {
            val response = api.call(
                method = method,
                apiKey = apiKey,
                sid = session.sid,
                arl = arlValue,
                network = "",
                gatewayInput = gatewayInput,
                body = body
            )
            if (hasError(response.error)) {
                if (!isRetry) {
                    return call(method, body, gatewayInput, isRetry = true)
                }
                Timber.e("DeezerGateway $method error: ${response.error}")
                null
            } else {
                response.results
            }
        } catch (e: Exception) {
            Timber.e(e, "DeezerGateway $method exception")
            null
        }
    }

    suspend fun testCredentials(): Boolean {
        val arlValue = arl() ?: return false
        val session = ensureSession(arlValue, forceRefresh = true) ?: return false
        val result = call("song.getFavoriteIds", mapOf("user_id" to session.userId.toString(), "start" to "0", "nb" to "1"))
            ?: return false
        return result.has("total")
    }

    suspend fun getAllFavoriteTrackIds(): List<GwFavoriteId> {
        val userId = currentUserId() ?: return emptyList()
        val result = call("song.getFavoriteIds", mapOf("user_id" to userId.toString(), "start" to "0", "nb" to "20000"))
            ?: return emptyList()
        return gson.fromJson(result, GwFavoriteIdsResult::class.java)?.data ?: emptyList()
    }

    suspend fun hydrateSongs(sngIds: List<String>): List<GwSong> {
        if (sngIds.isEmpty()) return emptyList()
        val songs = mutableListOf<GwSong>()
        sngIds.chunked(100).forEach { chunk ->
            val result = call("song_getListData", mapOf("SNG_IDS" to chunk)) ?: return@forEach
            songs.addAll(gson.fromJson(result, GwSongListResult::class.java)?.data ?: emptyList())
        }
        return songs
    }

    suspend fun getFavoriteAlbums(): List<GwAlbumFavorite> {
        val userId = currentUserId() ?: return emptyList()
        val result = call("album.getFavorites", mapOf("user_id" to userId.toString(), "start" to "0", "nb" to "4000"))
            ?: return emptyList()
        return gson.fromJson(result, GwAlbumFavoritesResult::class.java)?.data ?: emptyList()
    }

    suspend fun getFavoriteArtists(): List<GwArtistFavorite> {
        val userId = currentUserId() ?: return emptyList()
        val result = call("artist.getFavorites", mapOf("user_id" to userId.toString(), "start" to "0", "nb" to "4000"))
            ?: return emptyList()
        return gson.fromJson(result, GwArtistFavoritesResult::class.java)?.data ?: emptyList()
    }

    suspend fun getPlaylistSongs(playlistId: String): List<GwSong> {
        val result = call("playlist.getSongs", mapOf("playlist_id" to playlistId, "start" to "0", "nb" to "10000"))
            ?: return emptyList()
        return gson.fromJson(result, GwSongListResult::class.java)?.data ?: emptyList()
    }

    suspend fun getFavoritePlaylists(): List<GwPlaylistFavorite> {
        val userId = currentUserId() ?: return emptyList()
        val result = call("playlist.getFavorites", mapOf("user_id" to userId.toString(), "start" to "0", "nb" to "4000"))
            ?: return emptyList()
        return gson.fromJson(result, GwPlaylistFavoritesResult::class.java)?.data ?: emptyList()
    }

    suspend fun getHomeMixSeeds(): List<GwSong> {
        val gatewayInput = """{"VERSION":"2.5","LANG":"fr","timezone_offset":"2","FROM_ONBOARDING":false,"SUPPORT":{"deeplink-list":["deeplink"],"ads":["native"],"large-card":["artist","external-link","video-link","album","radio","livestream","show","app","playlist","track","generic"],"horizontal-grid":["smarttracklist","artist","livestream","show","track","generic","external-link","video-link","album","radio","playlist","flow","channel"],"grid-preview-one":["smarttracklist","artist","livestream","show","track","generic","page","external-link","video-link","album","radio","playlist","flow","channel"],"list":["artist","episode","album","radio","playlist","track","generic","channel"],"mini-banner":["external-link"],"horizontal-list":["track"],"highlight":["artist","album","radio","livestream","playlist","generic"],"slideshow":["artist","external-link","video-link","album","radio","livestream","show","playlist","channel"],"event-card":["live-event"],"small-horizontal-grid":["artist","external-link","livestream","album","radio","show","playlist","generic","flow","channel"],"grid":["smarttracklist","artist","livestream","show","track","generic","page","external-link","video-link","album","radio","playlist","flow","channel"],"message":["conversion","call_onboarding","braze","quick-access"],"long-card-horizontal-grid":["video-link","album","livestream","radio","show","playlist","generic"],"item-highlight":["radio"]},"OPTIONS":[],"page":"home"}"""
        val result = call("app_page_get", gatewayInput = gatewayInput) ?: return emptyList()
        val page = gson.fromJson(result, GwPageResult::class.java) ?: return emptyList()
        val section = page.sections?.firstOrNull { it.title?.startsWith("Mixes inspir") == true } ?: return emptyList()
        return section.items?.mapNotNull { it.data } ?: emptyList()
    }

    suspend fun getMixTracklist(seedSngId: String): List<GwSong> {
        val result = call("song.getSearchTrackMix", mapOf("sng_id" to seedSngId, "start_with_input_track" to true))
            ?: return emptyList()
        return gson.fromJson(result, GwSongListResult::class.java)?.data ?: emptyList()
    }
}
