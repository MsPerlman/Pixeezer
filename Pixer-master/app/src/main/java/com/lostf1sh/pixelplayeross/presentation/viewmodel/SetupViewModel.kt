package com.lostf1sh.pixelplayeross.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostf1sh.pixelplayeross.R

import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerAuthApi
import com.lostf1sh.pixelplayeross.data.preferences.AppThemeMode
import com.lostf1sh.pixelplayeross.data.preferences.ThemePreferencesRepository
import com.lostf1sh.pixelplayeross.data.preferences.UserPreferencesRepository
import com.lostf1sh.pixelplayeross.data.repository.DeezerGatewayRepository
import com.lostf1sh.pixelplayeross.data.repository.MusicRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File

data class SetupUiState(
    val mediaPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val isLoadingDirectories: Boolean = false,
    val blockedDirectories: Set<String> = emptySet(),
    val libraryNavigationMode: String = "tab_row",
    val navBarStyle: String = "default",
    val navBarCornerRadius: Int = 28,
    val externalLyricsEnabled: Boolean = false,
    val externalArtistImagesEnabled: Boolean = false,
    val alarmsPermissionGranted: Boolean = false,
    val appThemeMode: String = AppThemeMode.DARK,
    val deezerAuthUrl: String? = null,
    val deezerAuthCode: String? = null,
    val deezerLoginStatus: DeezerLoginStatus = DeezerLoginStatus.IDLE,
    val deezerLoggedIn: Boolean = false,
) {
    val allPermissionsGranted: Boolean
        get() {
            val notificationsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationsPermissionGranted else true
            return notificationsOk
        }
}

enum class DeezerLoginStatus {
    IDLE, REQUESTING, WAITING_FOR_USER, POLLING, SUCCESS, ERROR
}

sealed interface SetupEvent {
    data class Message(val value: String) : SetupEvent

}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val musicRepository: MusicRepository,
    private val deezerAuthApi: DeezerAuthApi,
    deezerGatewayRepository: DeezerGatewayRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<SetupEvent>()
    val events = _events.asSharedFlow()

    private val gatewayCredentialsStateHolder =
        GatewayCredentialsStateHolder(userPreferencesRepository, deezerGatewayRepository, viewModelScope)
    val gatewayUiState: kotlinx.coroutines.flow.StateFlow<GatewayCredentialsUiState> = gatewayCredentialsStateHolder.uiState
    fun updateGatewayArlInput(value: String) = gatewayCredentialsStateHolder.updateArl(value)
    fun saveAndTestGatewayCredentials() = gatewayCredentialsStateHolder.saveAndTest()
    fun clearGatewayCredentials() = gatewayCredentialsStateHolder.clear()



    private val fileExplorerStateHolder = FileExplorerStateHolder(userPreferencesRepository, viewModelScope, context)

    val currentPath = fileExplorerStateHolder.currentPath
    val currentDirectoryChildren = fileExplorerStateHolder.currentDirectoryChildren
    val blockedDirectories = fileExplorerStateHolder.blockedDirectories
    val availableStorages = fileExplorerStateHolder.availableStorages
    val selectedStorageIndex = fileExplorerStateHolder.selectedStorageIndex
    val isLoadingDirectories = fileExplorerStateHolder.isLoading
    val isExplorerPriming = fileExplorerStateHolder.isPrimingExplorer
    val isExplorerReady = fileExplorerStateHolder.isExplorerReady
    val isCurrentDirectoryResolved = fileExplorerStateHolder.isCurrentDirectoryResolved
    private var hasPendingDirectoryRuleChanges = false
    private var latestDirectoryRuleUpdateJob: Job? = null

    init {
        viewModelScope.launch {
            if (!userPreferencesRepository.initialSetupDoneFlow.first()) {
                themePreferencesRepository.initializeAppThemeMode(AppThemeMode.DARK)
            }
        }

        viewModelScope.launch {
            combine<Any?, SetupPrefsUpdate>(
                userPreferencesRepository.blockedDirectoriesFlow,
                userPreferencesRepository.libraryNavigationModeFlow,
                userPreferencesRepository.navBarStyleFlow,
                userPreferencesRepository.navBarCornerRadiusFlow,
                userPreferencesRepository.externalLyricsEnabledFlow,
                userPreferencesRepository.externalArtistImagesEnabledFlow,
                themePreferencesRepository.appThemeModeFlow
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val blockedDirectories = values[0] as Set<String>
                SetupPrefsUpdate(
                    blocked = blockedDirectories,
                    mode = values[1] as String,
                    style = values[2] as String,
                    radius = values[3] as Int,
                    externalLyricsEnabled = values[4] as Boolean,
                    externalArtistImagesEnabled = values[5] as Boolean,
                    appThemeMode = values[6] as String
                )
            }.collect { update ->
                _uiState.update { state ->
                    state.copy(
                        blockedDirectories = update.blocked,
                        libraryNavigationMode = update.mode,
                        navBarStyle = update.style,
                        navBarCornerRadius = update.radius,
                        externalLyricsEnabled = update.externalLyricsEnabled,
                        externalArtistImagesEnabled = update.externalArtistImagesEnabled,
                        appThemeMode = update.appThemeMode
                    )
                }
            }
        }

        viewModelScope.launch {
            fileExplorerStateHolder.isLoading.collect { loading ->
                _uiState.update { it.copy(isLoadingDirectories = loading) }
            }
        }
    }

    private data class SetupPrefsUpdate(
        val blocked: Set<String>,
        val mode: String,
        val style: String,
        val radius: Int,
        val externalLyricsEnabled: Boolean,
        val externalArtistImagesEnabled: Boolean,
        val appThemeMode: String
    )

    fun checkPermissions(context: Context) {
        val mediaPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        val notificationsPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val alarmsPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        _uiState.update {
            it.copy(
                mediaPermissionGranted = mediaPermissionGranted,
                notificationsPermissionGranted = notificationsPermissionGranted,
                alarmsPermissionGranted = alarmsPermissionGranted
            )
        }
    }

    fun loadMusicDirectories() {
        viewModelScope.launch {
            if (!userPreferencesRepository.initialSetupDoneFlow.first()) {
            }

            userPreferencesRepository.blockedDirectoriesFlow.first().let { blocked ->
                _uiState.update { it.copy(blockedDirectories = blocked) }
            }
            fileExplorerStateHolder.primeExplorerRoot()?.join()
        }
    }

    fun toggleDirectoryAllowed(file: File) {
        hasPendingDirectoryRuleChanges = true
        latestDirectoryRuleUpdateJob = viewModelScope.launch {
            fileExplorerStateHolder.toggleDirectoryAllowed(file)
        }
    }

    fun applyPendingDirectoryRuleChanges() {
        if (!hasPendingDirectoryRuleChanges) return
        hasPendingDirectoryRuleChanges = false
        viewModelScope.launch {
            latestDirectoryRuleUpdateJob?.join()
        }
    }

    fun loadDirectory(file: File) {
        fileExplorerStateHolder.loadDirectory(file)
    }

    fun selectStorage(index: Int) {
        fileExplorerStateHolder.selectStorage(index)
    }

    fun refreshAvailableStorages() {
        fileExplorerStateHolder.refreshAvailableStorages()
    }

    fun refreshCurrentDirectory() {
        fileExplorerStateHolder.refreshCurrentDirectory()
    }

    fun primeExplorer() {
        fileExplorerStateHolder.primeExplorerRoot()
    }

    fun openExplorer() {
        fileExplorerStateHolder.openExplorerRoot()
    }

    fun navigateUp() {
        fileExplorerStateHolder.navigateUp()
    }

    fun isAtRoot(): Boolean = fileExplorerStateHolder.isAtRoot()

    fun explorerRoot(): File = fileExplorerStateHolder.rootDirectory()

    fun setLibraryNavigationMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLibraryNavigationMode(mode)
        }
    }

    fun setNavBarStyle(style: String) {
        viewModelScope.launch {
            userPreferencesRepository.setNavBarStyle(style)
        }
    }

    fun setNavBarCornerRadius(radius: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setNavBarCornerRadius(radius)
        }
    }

    fun setExternalLyricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setExternalLyricsEnabled(enabled)
        }
    }

    fun setExternalArtistImagesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setExternalArtistImagesEnabled(enabled)
        }
    }

    fun setAppThemeMode(mode: String) {
        viewModelScope.launch {
            themePreferencesRepository.setAppThemeMode(mode)
        }
    }

    fun setSetupComplete() {
        viewModelScope.launch {
            completeSetup(syncAfter = true)
        }
    }



    private suspend fun completeSetup(syncAfter: Boolean) {
        userPreferencesRepository.setInitialSetupDone(true)
    }

    companion object {
        private const val DEEZER_APP_ID = "447462"
    }

    private var pollingJob: Job? = null

    fun startDeezerLogin() {
        pollingJob?.cancel()
        _uiState.update { it.copy(deezerLoginStatus = DeezerLoginStatus.REQUESTING) }

        pollingJob = viewModelScope.launch {
            try {
                val response = deezerAuthApi.getSmartLoginCode(DEEZER_APP_ID)
                val data = response.data ?: run {
                    _uiState.update { it.copy(deezerLoginStatus = DeezerLoginStatus.ERROR) }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        deezerAuthUrl = data.url,
                        deezerAuthCode = data.smartLoginCode,
                        deezerLoginStatus = DeezerLoginStatus.WAITING_FOR_USER
                    )
                }

                val intervalMs = (data.pollingInterval * 1000).toLong().coerceAtLeast(2000L)

                _uiState.update { it.copy(deezerLoginStatus = DeezerLoginStatus.POLLING) }
                while (true) {
                    delay(intervalMs)
                    try {
                        val poll = deezerAuthApi.pollSmartLogin(data.smartLoginCode, DEEZER_APP_ID)
                        val accessToken = poll.data?.accessToken
                        val userId = poll.data?.userId
                        if (accessToken != null && userId != null) {
                            userPreferencesRepository.saveDeezerAuth(accessToken, userId)
                            _uiState.update {
                                it.copy(
                                    deezerLoginStatus = DeezerLoginStatus.SUCCESS,
                                    deezerLoggedIn = true
                                )
                            }
                            return@launch
                        }
                    } catch (_: Exception) {
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(deezerLoginStatus = DeezerLoginStatus.ERROR) }
            }
        }
    }

    fun cancelDeezerLogin() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.update {
            it.copy(
                deezerLoginStatus = DeezerLoginStatus.IDLE,
                deezerAuthUrl = null,
                deezerAuthCode = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
