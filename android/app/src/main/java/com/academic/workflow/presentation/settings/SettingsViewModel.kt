package com.academic.workflow.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.academic.workflow.core.network.IntegrationHealthItem
import com.academic.workflow.data.preferences.UserPreferencesManager
import com.academic.workflow.data.repository.AuthRepository
import com.academic.workflow.data.repository.IntegrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val attendanceTarget: Float = 75f,
    val apiBaseUrl: String = UserPreferencesManager.DEFAULT_BASE_URL,
    val isGoogleConnected: Boolean = true,
    val isMicrosoftConnected: Boolean = true,
    val userEmail: String? = null,
    val integrations: List<IntegrationHealthItem> = emptyList(),
    val isSyncing: Boolean = false
)

class SettingsViewModel(
    private val preferencesManager: UserPreferencesManager,
    private val authRepository: AuthRepository,
    private val integrationRepository: IntegrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val darkMode = preferencesManager.isDarkMode.first()
            val target = preferencesManager.attendanceTarget.first()
            val url = preferencesManager.apiBaseUrl.first()
            val email = preferencesManager.userEmail.first()

            _uiState.value = _uiState.value.copy(
                isDarkMode = darkMode,
                attendanceTarget = target,
                apiBaseUrl = url,
                userEmail = email
            )
        }

        // Load Integration Health Dashboard
        viewModelScope.launch {
            integrationRepository.getIntegrationHealth().collect { dashboard ->
                _uiState.value = _uiState.value.copy(
                    integrations = dashboard.items,
                    isGoogleConnected = dashboard.items.any { it.category == "GOOGLE" && it.isConnected },
                    isMicrosoftConnected = dashboard.items.any { it.category == "MICROSOFT" && it.isConnected }
                )
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
        }
    }

    fun setAttendanceTarget(target: Float) {
        _uiState.value = _uiState.value.copy(attendanceTarget = target)
        viewModelScope.launch {
            preferencesManager.setAttendanceTarget(target)
        }
    }

    fun setApiBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(apiBaseUrl = url)
        viewModelScope.launch {
            preferencesManager.setApiBaseUrl(url)
        }
    }

    fun syncAllIntegrations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            integrationRepository.syncPlatform("all")
            integrationRepository.getIntegrationHealth().collect { dashboard ->
                _uiState.value = _uiState.value.copy(
                    integrations = dashboard.items,
                    isSyncing = false
                )
            }
        }
    }

    fun toggleGoogleConnection() {
        val newState = !_uiState.value.isGoogleConnected
        _uiState.value = _uiState.value.copy(isGoogleConnected = newState)
        if (!newState) {
            viewModelScope.launch {
                authRepository.disconnectProvider("google").collect {}
                integrationRepository.disconnectPlatform("google")
            }
        }
    }

    fun toggleMicrosoftConnection() {
        val newState = !_uiState.value.isMicrosoftConnected
        _uiState.value = _uiState.value.copy(isMicrosoftConnected = newState)
        if (!newState) {
            viewModelScope.launch {
                authRepository.disconnectProvider("microsoft").collect {}
                integrationRepository.disconnectPlatform("microsoft")
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
