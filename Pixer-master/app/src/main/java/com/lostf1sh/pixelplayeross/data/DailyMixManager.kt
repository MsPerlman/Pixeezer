package com.lostf1sh.pixelplayeross.data

import android.content.Context
import com.lostf1sh.pixelplayeross.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyMixManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getDailyMix(limit: Int): List<Song> = emptyList()
    suspend fun generateAndSaveDailyMix(limit: Int): List<Song> = emptyList()
    suspend fun getDynamicScore(song: Song, forceRecalculate: Boolean = false): Double = 0.0
    suspend fun clearAllEngagementData() {}
}
