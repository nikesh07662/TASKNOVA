package com.academic.workflow.core.di

import android.content.Context
import com.academic.workflow.core.network.ApiClient
import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.security.SecureStorage
import com.academic.workflow.data.local.AppDatabase
import com.academic.workflow.data.preferences.UserPreferencesManager
import com.academic.workflow.data.repository.ActivityRepository
import com.academic.workflow.data.repository.AiRepository
import com.academic.workflow.data.repository.AssignmentRepository
import com.academic.workflow.data.repository.AttendanceRepository
import com.academic.workflow.data.repository.AuthRepository
import com.academic.workflow.data.repository.CloudRepository
import com.academic.workflow.data.repository.IntegrationRepository
import com.academic.workflow.data.repository.SubmissionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AppContainer(private val context: Context) {
    val secureStorage: SecureStorage by lazy {
        SecureStorage(context)
    }

    val preferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(context)
    }

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val apiService: ApiService by lazy {
        val baseUrl = runBlocking { preferencesManager.apiBaseUrl.first() }
        ApiClient.create(baseUrl, secureStorage)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, secureStorage, preferencesManager)
    }

    val assignmentRepository: AssignmentRepository by lazy {
        AssignmentRepository(apiService, database.assignmentDao())
    }

    val aiRepository: AiRepository by lazy {
        AiRepository(apiService)
    }

    val submissionRepository: SubmissionRepository by lazy {
        SubmissionRepository(apiService)
    }

    val cloudRepository: CloudRepository by lazy {
        CloudRepository(apiService, database.cloudFileDao())
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(apiService, database.attendanceDao())
    }

    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(apiService, database.activityDao())
    }

    val integrationRepository: IntegrationRepository by lazy {
        IntegrationRepository(apiService)
    }
}
