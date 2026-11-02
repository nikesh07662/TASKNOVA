package com.academic.workflow.core.network

import com.academic.workflow.domain.model.ActivityItem
import com.academic.workflow.domain.model.Assignment
import com.academic.workflow.domain.model.AttendanceRecord
import com.academic.workflow.domain.model.AttendanceSummary
import com.academic.workflow.domain.model.ChatMessage
import com.academic.workflow.domain.model.CloudFile
import com.academic.workflow.domain.model.PreparedSubmission
import com.academic.workflow.domain.model.User
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class LoginRequest(val email: String, val passwordHash: String)

@Serializable
data class RegisterRequest(val email: String, val fullName: String, val passwordHash: String)

@Serializable
data class AuthResponse(val accessToken: String, val refreshToken: String, val user: User)

@Serializable
data class OAuthExchangeRequest(val provider: String, val authCode: String, val redirectUri: String)

@Serializable
data class ChatApiRequest(
    val prompt: String,
    val model: String = "gpt-4o",
    val conversationId: String? = null,
    val attachedFileIds: List<String> = emptyList()
)

@Serializable
data class DocumentGenerateRequest(
    val assignmentId: String? = null,
    val title: String,
    val subject: String,
    val documentType: String, // "ASSIGNMENT", "LAB_REPORT", "CODE", "SUMMARY"
    val instructions: String,
    val referenceFileIds: List<String> = emptyList()
)

@Serializable
data class DocumentGenerateResponse(
    val documentId: String,
    val title: String,
    val markdownContent: String,
    val pdfDownloadUrl: String?,
    val driveUploaded: Boolean,
    val oneDriveUploaded: Boolean
)

@Serializable
data class PrepareSubmissionRequest(
    val assignmentId: String,
    val selectedDocumentId: String
)

@Serializable
data class ConfirmSubmissionRequest(
    val submissionId: String,
    val idempotencyToken: String,
    val studentConfirmationCommand: String
)

@Serializable
data class SubmissionResultResponse(
    val submissionId: String,
    val success: Boolean,
    val receiptId: String,
    val verificationTimestamp: String,
    val message: String
)

@Serializable
data class AttendanceUpdateRequest(
    val subjectId: String,
    val attendedIncrement: Int = 0,
    val missedIncrement: Int = 0
)

@Serializable
data class IntegrationHealthItem(
    val id: String,
    val name: String,
    val category: String,
    val status: String,
    val statusDisplay: String? = null,
    val isConnected: Boolean = false,
    val requiresAdminConsent: Boolean = false,
    val adminConsentNote: String? = null,
    val lastSyncedAt: String? = null,
    val accountEmail: String? = null,
    val scopesGranted: List<String> = emptyList(),
    val message: String = ""
)

@Serializable
data class IntegrationDashboardResponse(
    val totalIntegrations: Int = 10,
    val activeConnectedCount: Int = 0,
    val needsAttentionCount: Int = 0,
    val items: List<IntegrationHealthItem> = emptyList()
)

interface ApiService {
    // Auth
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/oauth-exchange")
    suspend fun exchangeOAuthCode(@Body request: OAuthExchangeRequest): Response<AuthResponse>

    @POST("api/v1/auth/disconnect/{provider}")
    suspend fun disconnectOAuthProvider(@Path("provider") provider: String): Response<User>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<User>

    // Assignments
    @GET("api/v1/assignments")
    suspend fun getAssignments(): Response<List<Assignment>>

    @POST("api/v1/assignments/sync")
    suspend fun syncAssignments(): Response<List<Assignment>>

    @GET("api/v1/assignments/{id}")
    suspend fun getAssignmentDetails(@Path("id") id: String): Response<Assignment>

    // AI Assistant
    @POST("api/v1/ai/chat")
    suspend fun sendChatMessage(@Body request: ChatApiRequest): Response<ChatMessage>

    @POST("api/v1/ai/generate-document")
    suspend fun generateDocument(@Body request: DocumentGenerateRequest): Response<DocumentGenerateResponse>

    @Multipart
    @POST("api/v1/ai/import-export")
    suspend fun importChatHistory(
        @Part("provider") provider: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    // Command-Authorized Submission Engine
    @POST("api/v1/submissions/prepare")
    suspend fun prepareSubmission(@Body request: PrepareSubmissionRequest): Response<PreparedSubmission>

    @POST("api/v1/submissions/confirm")
    suspend fun confirmAndSubmit(@Body request: ConfirmSubmissionRequest): Response<SubmissionResultResponse>

    // Cloud Integrations
    @GET("api/v1/cloud/files")
    suspend fun getCloudFiles(@Query("provider") provider: String? = null): Response<List<CloudFile>>

    @Multipart
    @POST("api/v1/cloud/upload")
    suspend fun uploadFile(
        @Part("provider") provider: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<CloudFile>

    // Attendance
    @GET("api/v1/attendance")
    suspend fun getAttendanceSummary(): Response<AttendanceSummary>

    @POST("api/v1/attendance/update")
    suspend fun updateAttendance(@Body request: AttendanceUpdateRequest): Response<AttendanceRecord>

    // Activity
    @GET("api/v1/activity/feed")
    suspend fun getActivityFeed(): Response<List<ActivityItem>>

    // Integration Health Dashboard
    @GET("api/v1/integrations/health")
    suspend fun getIntegrationHealth(): Response<IntegrationDashboardResponse>

    @POST("api/v1/integrations/{platform}/sync")
    suspend fun syncIntegrationPlatform(@Path("platform") platform: String): Response<Map<String, String>>

    @POST("api/v1/integrations/{platform}/disconnect")
    suspend fun disconnectIntegrationPlatform(@Path("platform") platform: String): Response<Map<String, String>>
}
