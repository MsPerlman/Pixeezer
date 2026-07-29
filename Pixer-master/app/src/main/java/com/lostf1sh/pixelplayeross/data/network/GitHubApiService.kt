package com.lostf1sh.pixelplayeross.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("body") val body: String? = null,
    @SerializedName("published_at") val publishedAt: String? = null
)

interface GitHubApiService {
    @GET("repos/Minuga-RC/Pixer/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease

    @GET("repos/Minuga-RC/Pixer/releases")
    suspend fun getReleases(): List<GitHubRelease>
}
