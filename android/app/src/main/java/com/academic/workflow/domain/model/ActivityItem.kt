package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityCategory {
    AI_ACTION,
    DOCUMENT_GENERATION,
    SUBMISSION_AUDIT,
    SYNC_OPERATION,
    ATTENDANCE_UPDATE,
    NOTIFICATION
}

@Serializable
data class ActivityItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val timestamp: String,
    val isSuccess: Boolean = true,
    val relatedEntityId: String? = null
)
