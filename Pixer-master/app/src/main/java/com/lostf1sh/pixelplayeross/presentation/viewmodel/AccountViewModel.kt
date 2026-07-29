package com.lostf1sh.pixelplayeross.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerUserMeResponse
import com.lostf1sh.pixelplayeross.data.preferences.UserPreferencesRepository
import com.lostf1sh.pixelplayeross.data.repository.DeezerGatewayRepository
import com.lostf1sh.pixelplayeross.data.repository.DeezerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val isLoading: Boolean = true,
    val user: DeezerUserMeResponse? = null,
    val error: String? = null
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val deezerRepository: DeezerRepository,
    deezerGatewayRepository: DeezerGatewayRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState

    private val gatewayCredentialsStateHolder =
        GatewayCredentialsStateHolder(userPreferencesRepository, deezerGatewayRepository, viewModelScope)
    val gatewayUiState: StateFlow<GatewayCredentialsUiState> = gatewayCredentialsStateHolder.uiState
    fun updateGatewayArlInput(value: String) = gatewayCredentialsStateHolder.updateArl(value)
    fun saveAndTestGatewayCredentials() = gatewayCredentialsStateHolder.saveAndTest()
    fun clearGatewayCredentials() = gatewayCredentialsStateHolder.clear()

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = deezerRepository.getUserMe()
            result.fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, user = response) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: e.toString()) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearDeezerAuth()
        }
    }
}
