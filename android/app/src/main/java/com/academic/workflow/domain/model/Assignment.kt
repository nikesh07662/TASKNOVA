package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlatformSource {
    GOOGLE_CLASSROOM,
    MICROSOFT_TEAMS,
    MANUAL
}

@Serializable
enum class AssignmentStatus {
    PENDING,
    IN_PROGRESS,
    PREPARED,
    SUBMITTED,
    OVERDUE
}

@Serializable
data class AttachedFile(
    val id: String,
    val name: String,
    val downloadUrl: String,
    val mimeType: String = "application/pdf"
)

@Serializable
data class Assignment(
    val id: String,
    val title: String,
    val subject: String,
    val instructions: String,
    val deadline: String, // ISO-8601 string
    val platform: PlatformSource,
    val status: AssignmentStatus,
    val sourcePlatformId: String? = null,
    val attachedFiles: List<AttachedFile> = emptyList(),
    val matchedDocumentId: String? = null,
    val matchedDocumentName: String? = null,
    val isDuplicate: Boolean = false,
    val isReadinessChecked: Boolean = false,
    val missingFilesWarning: String? = null
)
