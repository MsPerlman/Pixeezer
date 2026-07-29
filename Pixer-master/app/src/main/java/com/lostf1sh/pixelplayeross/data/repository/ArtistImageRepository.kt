package com.lostf1sh.pixelplayeross.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistImageRepository @Inject constructor() {
    // Stubbed for Deezer migration
    suspend fun getEffectiveArtistImageUrl(artistId: Long, artistName: String): String? = null
    suspend fun setCustomArtistImage(context: android.content.Context, artistId: Long, sourceUri: android.net.Uri): String? = null
    suspend fun clearCustomArtistImage(context: android.content.Context, artistId: Long) {}
    suspend fun getArtistImageUrl(artistName: String, artistId: Long): String? = null
    fun clearCache() {}
}
