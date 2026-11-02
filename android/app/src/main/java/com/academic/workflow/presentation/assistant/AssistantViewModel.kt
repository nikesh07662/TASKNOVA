package com.academic.workflow.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.data.preferences.UserPreferencesManager
import com.academic.workflow.data.repository.AiRepository
import com.academic.workflow.domain.model.ChatMessage
import com.academic.workflow.domain.model.MessageRole
import com.academic.workflow.domain.model.ToolExecutionInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val selectedModel: String = "gpt-4o",
    val isGenerating: Boolean = false,
    val attachedFiles: List<String> = emptyList(),
    val activeToolExecution: ToolExecutionInfo? = null,
    val errorMessage: String? = null
)

class AssistantViewModel(
    private val aiRepository: AiRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedModel = preferencesManager.preferredAiModel.first()
            _uiState.value = _uiState.value.copy(
                selectedModel = savedModel,
                messages = listOf(
                    ChatMessage(
                        id = "welcome",
                        role = MessageRole.ASSISTANT,
                        content = "Hello! I am your Academic Workflow Automation Copilot. You can ask me to prepare assignments, analyze reference documents, generate code or practical files, and review upcoming deadlines.\n\nNote: All external submissions require your explicit confirmation.",
                        timestamp = Instant.now().toString(),
                        modelUsed = savedModel
                    )
                )
            )
        }
    }

    fun selectModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        viewModelScope.launch {
            preferencesManager.setPreferredAiModel(model)
        }
    }

    fun attachFileName(fileName: String) {
        _uiState.value = _uiState.value.copy(
            attachedFiles = _uiState.value.attachedFiles + fileName
        )
    }

    fun removeAttachment(fileName: String) {
        _uiState.value = _uiState.value.copy(
            attachedFiles = _uiState.value.attachedFiles.filter { it != fileName }
        )
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank()) return

        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            role = MessageRole.USER,
            content = promptText,
            timestamp = Instant.now().toString(),
            attachedFileNames = _uiState.value.attachedFiles
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isGenerating = true,
            attachedFiles = emptyList(),
            errorMessage = null,
            activeToolExecution = ToolExecutionInfo(
                toolName = "AcademicContextQuery",
                argumentsJson = "{\"query\": \"$promptText\"}",
                isExecuting = true
            )
        )

        viewModelScope.launch {
            aiRepository.sendMessage(
                prompt = promptText,
                model = _uiState.value.selectedModel,
                attachedFileIds = emptyList()
            ).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isGenerating = true)
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + result.data,
                            isGenerating = false,
                            activeToolExecution = null
                        )
                    }
                    is NetworkResult.Error -> {
                        // Fallback response for offline or demo robustness
                        val fallbackResponse = ChatMessage(
                            id = "ai_${System.currentTimeMillis()}",
                            role = MessageRole.ASSISTANT,
                            content = "I've reviewed your request: \"$promptText\". I can help organize files, extract requirements, or draft the submission once backend connectivity is refreshed.",
                            timestamp = Instant.now().toString(),
                            modelUsed = _uiState.value.selectedModel
                        )
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + fallbackResponse,
                            isGenerating = false,
                            activeToolExecution = null,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun importChatExport(provider: String, file: File) {
        viewModelScope.launch {
            aiRepository.importExportHistory(provider, file).collect { result ->
                if (result is NetworkResult.Success) {
                    val count = result.data["importedConversations"] ?: "1"
                    val systemMsg = ChatMessage(
                        id = "import_${System.currentTimeMillis()}",
                        role = MessageRole.SYSTEM,
                        content = "Successfully imported $count conversations from $provider export into Universal Task Memory.",
                        timestamp = Instant.now().toString()
                    )
                    _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + systemMsg)
                }
            }
        }
    }
}
