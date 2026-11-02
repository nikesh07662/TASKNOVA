package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val isGoogleConnected: Boolean = false,
    val isMicrosoftConnected: Boolean = false,
    val preferredAiModel: String = "gpt-4o",
    val attendanceTarget: Float = 75f
)
