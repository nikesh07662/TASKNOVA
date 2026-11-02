package com.academic.workflow.data.repository

import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.network.ChatApiRequest
import com.academic.workflow.core.network.DocumentGenerateRequest
import com.academic.workflow.core.network.DocumentGenerateResponse
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AiRepository(private val apiService: ApiService) {

    fun sendMessage(
        prompt: String,
        model: String,
        conversationId: String? = null,
        attachedFileIds: List<String> = emptyList()
    ): Flow<NetworkResult<ChatMessage>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.sendChatMessage(
                ChatApiRequest(
                    prompt = prompt,
                    model = model,
                    conversationId = conversationId,
                    attachedFileIds = attachedFileIds
                )
            )
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("AI Assistant error: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "AI Service unavailable"))
        }
    }

    fun generateDocument(
        title: String,
        subject: String,
        type: String,
        instructions: String,
        assignmentId: String? = null,
        referenceFileIds: List<String> = emptyList()
    ): Flow<NetworkResult<DocumentGenerateResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.generateDocument(
                DocumentGenerateRequest(
                    assignmentId = assignmentId,
                    title = title,
                    subject = subject,
                    documentType = type,
                    instructions = instructions,
                    referenceFileIds = referenceFileIds
                )
            )
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Document generation failed: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Generation service error"))
        }
    }

    fun importExportHistory(provider: String, file: File): Flow<NetworkResult<Map<String, String>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val providerBody = provider.toRequestBody("text/plain".toMediaTypeOrNull())
            val fileBody = file.asRequestBody("application/json".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, fileBody)
            
            val response = apiService.importChatHistory(providerBody, part)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Chat export import failed", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Import processing error"))
        }
    }
}
