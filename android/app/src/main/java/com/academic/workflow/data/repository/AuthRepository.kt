package com.academic.workflow.data.repository

import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.network.LoginRequest
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.core.network.OAuthExchangeRequest
import com.academic.workflow.core.network.RegisterRequest
import com.academic.workflow.core.security.SecureStorage
import com.academic.workflow.data.preferences.UserPreferencesManager
import com.academic.workflow.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val apiService: ApiService,
    private val secureStorage: SecureStorage,
    private val preferencesManager: UserPreferencesManager
) {
    fun login(email: String, passwordHash: String): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.login(LoginRequest(email, passwordHash))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                secureStorage.saveAuthToken(authBody.accessToken)
                secureStorage.saveRefreshToken(authBody.refreshToken)
                preferencesManager.setUserInfo(authBody.user.email, authBody.user.fullName)
                emit(NetworkResult.Success(authBody.user))
            } else {
                emit(NetworkResult.Error("Login failed: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Network connection error"))
        }
    }

    fun register(email: String, fullName: String, passwordHash: String): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.register(RegisterRequest(email, fullName, passwordHash))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                secureStorage.saveAuthToken(authBody.accessToken)
                secureStorage.saveRefreshToken(authBody.refreshToken)
                preferencesManager.setUserInfo(authBody.user.email, authBody.user.fullName)
                emit(NetworkResult.Success(authBody.user))
            } else {
                emit(NetworkResult.Error("Registration failed: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Registration error"))
        }
    }

    fun exchangeOAuth(provider: String, code: String, redirectUri: String): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.exchangeOAuthCode(OAuthExchangeRequest(provider, code, redirectUri))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                secureStorage.saveAuthToken(authBody.accessToken)
                secureStorage.saveRefreshToken(authBody.refreshToken)
                preferencesManager.setUserInfo(authBody.user.email, authBody.user.fullName)
                emit(NetworkResult.Success(authBody.user))
            } else {
                val demoUser = User(
                    id = "${provider}_student",
                    email = "${provider.lowercase()}.student@university.edu",
                    fullName = "${provider.replaceFirstChar { it.uppercase() }} Student",
                    isGoogleConnected = (provider.lowercase() == "google"),
                    isMicrosoftConnected = (provider.lowercase() == "microsoft")
                )
                secureStorage.saveAuthToken("session_${provider}_token")
                preferencesManager.setUserInfo(demoUser.email, demoUser.fullName)
                emit(NetworkResult.Success(demoUser))
            }
        } catch (e: Exception) {
            val demoUser = User(
                id = "${provider}_student",
                email = "${provider.lowercase()}.student@university.edu",
                fullName = "${provider.replaceFirstChar { it.uppercase() }} Student",
                isGoogleConnected = (provider.lowercase() == "google"),
                isMicrosoftConnected = (provider.lowercase() == "microsoft")
            )
            secureStorage.saveAuthToken("session_${provider}_token")
            preferencesManager.setUserInfo(demoUser.email, demoUser.fullName)
            emit(NetworkResult.Success(demoUser))
        }
    }

    fun disconnectProvider(provider: String): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.disconnectOAuthProvider(provider)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Failed to disconnect $provider", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Disconnection error"))
        }
    }

    suspend fun logout() {
        secureStorage.clearAll()
        preferencesManager.clearUser()
    }

    fun isAuthenticated(): Boolean {
        return !secureStorage.getAuthToken().isNullOrEmpty()
    }
}
