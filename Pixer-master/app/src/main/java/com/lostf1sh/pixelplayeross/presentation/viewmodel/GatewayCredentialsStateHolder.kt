package com.lostf1sh.pixelplayeross.presentation.viewmodel

import com.lostf1sh.pixelplayeross.data.preferences.UserPreferencesRepository
import com.lostf1sh.pixelplayeross.data.repository.DeezerGatewayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GatewayCredentialsUiState(
    val arlInput: String = "",
    val isConfigured: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: Boolean? = null
)

class GatewayCredentialsStateHolder(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val deezerGatewayRepository: DeezerGatewayRepository,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(GatewayCredentialsUiState())
    val uiState: StateFlow<GatewayCredentialsUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            _uiState.update { it.copy(isConfigured = deezerGatewayRepository.isConfigured()) }
        }
    }

    fun updateArl(value: String) = _uiState.update { it.copy(arlInput = value, testResult = null) }

    fun saveAndTest() {
        val state = _uiState.value
        val arl = state.arlInput.trim()
        if (arl.isEmpty()) return

        scope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null) }
            userPreferencesRepository.saveDeezerGatewayCredentials(arl)
            val ok = deezerGatewayRepository.testCredentials()
            if (!ok) {
                userPreferencesRepository.clearDeezerGatewayCredentials()
            }
            _uiState.update { it.copy(isTesting = false, testResult = ok, isConfigured = ok) }
        }
    }

    fun clear() {
        scope.launch {
            userPreferencesRepository.clearDeezerGatewayCredentials()
            _uiState.update {
                it.copy(
                    arlInput = "",
                    isConfigured = false,
                    testResult = null
                )
            }
        }
    }
}
