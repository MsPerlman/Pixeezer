package com.lostf1sh.pixelplayeross.data.repository

import com.lostf1sh.pixelplayeross.BuildConfig
import com.lostf1sh.pixelplayeross.data.network.GitHubApiService
import com.lostf1sh.pixelplayeross.data.network.GitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCheckerRepository @Inject constructor(
    private val apiService: GitHubApiService
) {
    suspend fun getLatestReleaseIfNewer(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val latestRelease = apiService.getLatestRelease()
            val currentVersion = BuildConfig.VERSION_NAME
            
            val remoteVersionStr = latestRelease.tagName.removePrefix("v")
            val currentVersionStr = currentVersion.removePrefix("v")
            
            if (isRemoteNewer(remoteVersionStr, currentVersionStr)) {
                latestRelease
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isRemoteNewer(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    suspend fun getChangelogs(): List<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            apiService.getReleases()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
