package com.lostf1sh.pixelplayeross.data.repository

import com.lostf1sh.pixelplayeross.data.model.TransitionResolution
import com.lostf1sh.pixelplayeross.data.model.TransitionRule
import com.lostf1sh.pixelplayeross.data.model.TransitionSettings
import com.lostf1sh.pixelplayeross.data.model.TransitionSource
import com.lostf1sh.pixelplayeross.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitionRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : TransitionRepository {
    override fun resolveTransitionSettings(
        playlistId: String,
        fromTrackId: String,
        toTrackId: String
    ): Flow<TransitionResolution> {
        return userPreferencesRepository.crossfadeDurationFlow.map { durationMs ->
            TransitionResolution(
                settings = TransitionSettings(durationMs = durationMs),
                source = TransitionSource.GLOBAL_DEFAULT
            )
        }
    }

    override fun getAllRulesForPlaylist(playlistId: String): Flow<List<TransitionRule>> = flowOf(emptyList())

    override fun getPlaylistDefaultRule(playlistId: String): Flow<TransitionRule?> = flowOf(null)

    override suspend fun saveRule(rule: TransitionRule) { }

    override suspend fun deleteRule(ruleId: Long) { }

    override suspend fun deletePlaylistDefaultRule(playlistId: String) { }

    override fun getGlobalSettings(): Flow<TransitionSettings> {
        return userPreferencesRepository.crossfadeDurationFlow.map { durationMs ->
            TransitionSettings(durationMs = durationMs)
        }
    }

    override suspend fun saveGlobalSettings(settings: TransitionSettings) { 
        userPreferencesRepository.setCrossfadeDuration(settings.durationMs)
    }
}