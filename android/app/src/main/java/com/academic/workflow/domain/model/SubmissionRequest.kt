package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SubmissionStage {
    DRAFTING,
    PREPARED_FOR_REVIEW,
    CONFIRMED_BY_USER,
    SUBMITTING,
    SUBMITTED_AND_VERIFIED,
    FAILED
}

@Serializable
data class SubmissionAuditLog(
    val timestamp: String,
    val actor: String, // "AI_AGENT" or "USER_STUDENT" or "PLATFORM_API"
    val action: String,
    val details: String
)

@Serializable
data class PreparedSubmission(
    val submissionId: String,
    val assignmentId: String,
    val assignmentTitle: String,
    val subject: String,
    val destinationPlatform: PlatformSource,
    val documentId: String,
    val documentName: String,
    val documentSizeFormatted: String,
    val documentPreviewSnippet: String,
    val deadline: String,
    val stage: SubmissionStage = SubmissionStage.PREPARED_FOR_REVIEW,
    val idempotencyToken: String,
    val verificationReceipt: String? = null,
    val errorReason: String? = null,
    val auditTrail: List<SubmissionAuditLog> = emptyList()
)
