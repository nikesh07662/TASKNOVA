package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}

@Serializable
data class ToolExecutionInfo(
    val toolName: String,
    val argumentsJson: String,
    val resultSummary: String? = null,
    val isExecuting: Boolean = false
)

@Serializable
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: String,
    val modelUsed: String? = null,
    val attachedFileNames: List<String> = emptyList(),
    val toolExecutions: List<ToolExecutionInfo> = emptyList(),
    val suggestedAction: String? = null,
    val relatedAssignmentId: String? = null
)
