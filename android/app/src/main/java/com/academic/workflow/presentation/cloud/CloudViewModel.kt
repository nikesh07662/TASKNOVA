package com.academic.workflow.presentation.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.data.repository.AiRepository
import com.academic.workflow.data.repository.CloudRepository
import com.academic.workflow.domain.model.CloudFile
import com.academic.workflow.domain.model.CloudStorageProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CloudUiState(
    val files: List<CloudFile> = emptyList(),
    val selectedProvider: CloudStorageProvider? = null,
    val isLoading: Boolean = false,
    val isGeneratingDoc: Boolean = false,
    val generatedDocMessage: String? = null,
    val errorMessage: String? = null
)

class CloudViewModel(
    private val cloudRepository: CloudRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudUiState())
    val uiState: StateFlow<CloudUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cloudRepository.allFiles.collect { filesList ->
                _uiState.value = _uiState.value.copy(files = filesList)
            }
        }
        syncFiles()
    }

    fun syncFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cloudRepository.syncCloudFiles().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun filterProvider(provider: CloudStorageProvider?) {
        _uiState.value = _uiState.value.copy(selectedProvider = provider)
    }

    fun generateAcademicDocument(
        title: String,
        subject: String,
        type: String,
        instructions: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingDoc = true, generatedDocMessage = null)
            aiRepository.generateDocument(
                title = title,
                subject = subject,
                type = type,
                instructions = instructions
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isGeneratingDoc = false,
                            generatedDocMessage = "Document \"${result.data.title}\" successfully generated and synchronized to Cloud!"
                        )
                        syncFiles()
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isGeneratingDoc = false,
                            errorMessage = result.message
                        )
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }
}
